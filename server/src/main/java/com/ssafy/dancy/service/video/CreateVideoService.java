package com.ssafy.dancy.service.video;

import com.ssafy.dancy.entity.User;
import com.ssafy.dancy.entity.Video;
import com.ssafy.dancy.entity.WrongPick;
import com.ssafy.dancy.exception.user.NotHavingPermissionException;
import com.ssafy.dancy.exception.video.VideoNotFoundException;
import com.ssafy.dancy.message.request.video.ConvertToPythonRequest;
import com.ssafy.dancy.message.request.video.ConvertVideoRequest;
import com.ssafy.dancy.message.response.video.*;
import com.ssafy.dancy.repository.video.VideoRepository;
import com.ssafy.dancy.repository.WrongPickRepository;
import com.ssafy.dancy.type.VideoType;
import com.ssafy.dancy.util.AlarmHandler;
import com.ssafy.dancy.util.AwsS3Util;
import com.ssafy.dancy.util.FileStoreUtil;
import com.ssafy.dancy.util.VideoProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreateVideoService {

    private final FileStoreUtil fileStoreUtil;
    private final VideoRepository videoRepository;
    private final WrongPickRepository wrongPickRepository;
    private final AwsS3Util awsS3Util;
    private final WebClient webClient;
    private final AlarmHandler alarmHandler;
    private final VideoProcessor videoProcessor;

    private static final String PRACTICE_VIDEO_TARGET = "video/prac";
    private static final String REFERENCE_VIDEO_TARGET = "video/gt";
    private static final String THUMBNAIL_IMAGE_TARGET = "thumbnailImage";
    private static final String CONVERT_COMPLETE_NAME = "convert_complete";

    private static final String S3_URL_PREFIX = "https://gumid210bucket.s3.ap-northeast-2.amazonaws.com/";

    @Value("${python.server}")
    private String pythonServerUrl;


    public List<VideoReferenceResponse> getReferenceVideoList(int limit, Long previousVideoId) {
        return videoRepository.findVideoReferenceList(limit, previousVideoId);
    }

    public UploadVideoResponse uploadReferenceVideo(User user, MultipartFile file){

        log.info("입력한 유저 : {}, 입력한 파일 이름 : {}", user.getNickname(), file.getOriginalFilename());

        String originalFilename = file.getOriginalFilename();
        String ext = fileStoreUtil.extractExt(originalFilename);
        String storeName = String.format("%s_%s_%s", getOriginalName(originalFilename, ext), "gt", user.getNickname());

        log.info("변환한 비디오 이름 : {}", storeName);

        return uploadVideo(storeName, user, ext, file, REFERENCE_VIDEO_TARGET, VideoType.REFERENCE);
    }

    public UploadVideoResponse uploadPracticeVideo(User user, MultipartFile file, Long referenceVideoId){

        log.info("입력한 유저 : {}, 입력한 파일 이름 : {}", user.getNickname(), file.getOriginalFilename());

        Video video = videoRepository.findByVideoId(referenceVideoId).orElseThrow(() ->
                new VideoNotFoundException("해당 레퍼런스 비디오를 찾을 수 없습니다."));

        String ext = fileStoreUtil.extractExt(file.getOriginalFilename());
        String uuid = UUID.randomUUID().toString();
        String[] videoNames = video.getVideoTitle().split("_");
        String storeName = String.format("%s_%s_%s_%s", videoNames[0], "prac", user.getNickname(), uuid);

        log.info("변환한 비디오 이름 : {}", storeName);

        return uploadVideo(storeName, user, ext, file, PRACTICE_VIDEO_TARGET, VideoType.PRACTICE);
    }

    public ConvertVideoResponse requestConvertToFlask(User user, ConvertVideoRequest request) {

        String reference = extractSimpleUrlFromFull(request.referenceVideoUrl());
        String practice = extractSimpleUrlFromFull(request.practiceVideoUrl());

        if(!awsS3Util.hasObjectInS3(reference) || !awsS3Util.hasObjectInS3(practice)){
            throw new VideoNotFoundException("레퍼런스나 연습 비디오가 존재하지 않습니다.");
        }

        log.info("변환에 들어가는 API : {}", pythonServerUrl);
        log.info("변환 진입 -> reference : {}, practice : {}", reference, practice);

        webClient.post()
                .uri(pythonServerUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(makeSimpleRequest(reference, practice))
                .retrieve()
                .bodyToMono(VideoConvertResponse.class)
                .doOnError(e -> sendErrorMessage(user, e))
                .subscribe((result) -> afterCompleteConvert(user, result));

        return ConvertVideoResponse.builder()
                .practiceVideoUrl(request.practiceVideoUrl())
                .referenceVideoUrl(request.referenceVideoUrl())
                .requestTime(LocalDateTime.now())
                .build();
    }

    public ConvertResultResponse getResultVideoInfo(User user, Long videoId){
        Video video = videoRepository.findByVideoId(videoId).orElseThrow(() ->
                new VideoNotFoundException("해당 영상 정보를 찾을 수 없습니다."));

        if(!video.getUser().equals(user)){
            throw new NotHavingPermissionException("해당 유저의 비디오가 아닙니다.");
        }

        List<WrongPick> wrongPickList = wrongPickRepository.findAllByVideo_VideoId(videoId);

        return ConvertResultResponse.builder()
                .videoUrl(video.getFullVideoUrl())
                .score(video.getScore())
                .wrongSections(convertToWrongSection(wrongPickList))
                .thumbnailImageUrl(video.getThumbnailImageUrl())
                .nickname(user.getNickname())
                .videoTitle(video.getVideoTitle())
                .build();
    }

    public void afterCompleteConvert(User user, VideoConvertResponse response){
        log.info("변환된 영상 : {}", response.totalUrl());
        log.info("썸네일 : {}", response.thumbnailImageUrl());
        log.info("측정 정확도 : {}", response.total_accuracy());
        log.info("결과 total url : {}", response.totalUrl());

        Video savedVideo = videoRepository.save(Video.builder()
                .user(user)
                .videoTitle(extractNameFromUrl(response.totalUrl()))
                .score(response.total_accuracy())
                .thumbnailImageUrl(S3_URL_PREFIX + response.thumbnailImageUrl())
                .videoType(VideoType.TOTAL)
                .fullVideoUrl(S3_URL_PREFIX + response.totalUrl())
                .build());

        List<WrongPick> pickList = new ArrayList<>();

        for (VideoWrongSection section : response.list()) {
            log.info("start : {}, end : {}, accuracy : {}", section.start(), section.end(), section.accuracy());

            pickList.add(WrongPick.builder()
                    .video(savedVideo)
                    .startTime(section.start())
                    .endTime(section.end())
                    .accuracy(section.accuracy())
                    .build());
        }

        wrongPickRepository.saveAll(pickList);
        alarmHandler.sendEventToUser(user.getUserId(), CONVERT_COMPLETE_NAME, savedVideo.getVideoId().toString());
    }

    private UploadVideoResponse uploadVideo(String storeName, User user, String ext, MultipartFile file,
                                            String target, VideoType videoType){

        String storeFilename = storeName + "." + ext;
        String referenceVideoUrl = fileStoreUtil.uploadVideoFileToS3(file, target, storeFilename);

        log.info("video upload : {}", referenceVideoUrl);
        String thumbnailImageName = "thumbnail_" + storeName + ".jpg";

        MultipartFile thumbnailImage = videoProcessor.captureThumbnailFromVideo(file, 60, thumbnailImageName);
        String thumbnailImageUrl = fileStoreUtil.uploadThumbnailImageToS3(thumbnailImage, THUMBNAIL_IMAGE_TARGET,
                thumbnailImageName);

        Video savedVideo = videoRepository.save(Video.builder()
                .user(user)
                .videoTitle(storeName)
                .fullVideoUrl(referenceVideoUrl)
                .thumbnailImageUrl(thumbnailImageUrl)
                .videoType(videoType)
                .build());

        return wrapUpVideoUrl(savedVideo.getVideoId(), referenceVideoUrl, thumbnailImageUrl);
    }

    private UploadVideoResponse wrapUpVideoUrl(Long videoId, String videoUrl, String thumbnailImageUrl){
        return UploadVideoResponse.builder()
                .videoId(videoId)
                .resultVideoUrl(videoUrl)
                .thumbnailImageUrl(thumbnailImageUrl)
                .build();
    }

    private String getOriginalName(String originalFilename, String ext){
        return originalFilename.substring(0, originalFilename.length() - ext.length() - 1);
    }

    private ConvertToPythonRequest makeSimpleRequest(String gtUrl, String pracUrl){

        return ConvertToPythonRequest.builder()
                .gtUrl(gtUrl)
                .pracUrl(pracUrl)
                .build();
    }
    private String extractSimpleUrlFromFull(String fullUrl){

        log.info("입력받은 full url : {}", fullUrl);
        int startIndex = fullUrl.indexOf("video/");
        return fullUrl.substring(startIndex);
    }

    private String extractNameFromUrl(String url){
        String[] split = url.split("/");
        return split[split.length - 1];
    }

    private List<VideoWrongSection> convertToWrongSection(List<WrongPick> list){
        List<VideoWrongSection> resultList = new ArrayList<>();

        for(WrongPick pick : list){
            resultList.add(VideoWrongSection.builder()
                            .start(pick.getStartTime())
                            .end(pick.getEndTime())
                            .accuracy(pick.getAccuracy())
                    .build());
        }
        return resultList;
    }

    private void sendErrorMessage(User user, Throwable e){
        alarmHandler.sendEventToUser(user.getUserId(), "convert_error", "파이썬 요청 실패");
    }
}
