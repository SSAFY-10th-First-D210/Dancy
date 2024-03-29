package com.ssafy.dancy.service.article;

import com.ssafy.dancy.entity.Article;
import com.ssafy.dancy.entity.SavedArticle;
import com.ssafy.dancy.entity.User;
import com.ssafy.dancy.entity.Video;
import com.ssafy.dancy.exception.article.ArticleNotFoundException;
import com.ssafy.dancy.exception.article.ArticleNotOwnerException;
import com.ssafy.dancy.exception.user.NotHavingPermissionException;
import com.ssafy.dancy.exception.video.VideoNotFoundException;
import com.ssafy.dancy.message.request.article.ArticleModifyRequest;
import com.ssafy.dancy.message.request.article.ArticleWriteRequest;
import com.ssafy.dancy.message.response.article.ArticleDetailResponse;
import com.ssafy.dancy.message.response.article.ArticleSaveResponse;
import com.ssafy.dancy.message.response.article.ArticleSimpleResponse;
import com.ssafy.dancy.repository.ArticleSaveRepository;
import com.ssafy.dancy.repository.article.ArticleRepository;
import com.ssafy.dancy.repository.video.VideoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@RequiredArgsConstructor
@Slf4j
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final ArticleSaveRepository articleSaveRepository;
    private final VideoRepository videoRepository;

    public List<ArticleSimpleResponse> getStagePage(int limit, Long previousLastArticleId){
        return articleRepository.getStagePageInfo(limit, previousLastArticleId);
    }

    public ArticleDetailResponse getArticle(User user, long articleId) {
        return articleRepository.getArticleDetailInfo(user, articleId).orElseThrow(() ->
                new ArticleNotFoundException("게시물을 찾을 수 없습니다."));
    }

    public List<ArticleSimpleResponse> getArticleOfPerson(String nickname, int findCount, Long previousArticleId) {
        return articleRepository.getArticleSearchByNickname(nickname, findCount, previousArticleId);
    }

    public ArticleDetailResponse insertArticle(User user, ArticleWriteRequest request) {

        Video video = videoRepository.findByVideoId(request.videoId()).orElseThrow(() ->
                new VideoNotFoundException("해당 비디오가 존재하지 않습니다."));

        if(!video.getUser().equals(user)){
            throw new NotHavingPermissionException("해당 비디오에 대한 업로드 권한이 없습니다.");
        }

        Article article = Article.builder()
                .articleTitle(request.articleTitle())
                .articleContent(request.articleContent())
                .thumbnailImageUrl(video.getThumbnailImageUrl())
                .video(video)
                .user(user)
                .build();

        articleRepository.save(article);

        return makeArticleDetailResponse(article, user);

    }

    @Transactional
    public ArticleDetailResponse modifyArticle(User user, long articleId, ArticleModifyRequest dto) {

        Article article = articleRepository.findByArticleId(articleId).orElseThrow(() ->
                new ArticleNotFoundException("게시물을 찾을 수 없습니다."));

        if(!user.equals(article.getUser())){
            throw new ArticleNotOwnerException("게시물을 수정/삭제할 수 있는 권한이 없습니다.");
        }

        article.setArticleTitle(dto.articleTitle());
        article.setArticleContent(dto.articleContent());

        return makeArticleDetailResponse(article, user);
    }

    public Long deleteArticle(User user, long articleId) {

        Article article = articleRepository.findByArticleId(articleId).orElseThrow(() ->
                new ArticleNotFoundException("게시물을 찾을 수 없습니다."));

        if(!user.equals(article.getUser())){
            throw new ArticleNotOwnerException("게시물을 수정/삭제할 수 있는 권한이 없습니다.");
        }

        articleRepository.delete(article);

        return article.getArticleId();
    }

    public ArticleSaveResponse saveOrDeleteArticleForUser(User user, Long articleId) {
        Article article = articleRepository.findByArticleId(articleId).orElseThrow(() ->
                new ArticleNotFoundException("요청한 게시글을 찾을 수 없습니다."));

        SavedArticle savedInfo = SavedArticle.builder()
                .article(article)
                .user(user)
                .build();

        AtomicBoolean exist = new AtomicBoolean(true);
        articleSaveRepository.findByArticle_ArticleIdAndUser(articleId, user).ifPresentOrElse(
                result -> {
                    exist.set(false);
                    articleSaveRepository.delete(result);
                },
                () -> articleSaveRepository.save(savedInfo)
        );

        return ArticleSaveResponse.builder()
                .isSaved(exist.get())
                .articleId(articleId)
                .saveUserNickname(user.getNickname())
                .articleTitle(article.getArticleTitle())
                .articleAuthorNickname(article.getUser().getNickname())
                .build();
    }

    public List<ArticleSimpleResponse> getSavedArticleOfUser(String nickname, Integer findCount, Long previousArticleId) {
        return articleRepository.getArticleSavedByPerson(nickname, findCount, previousArticleId);
    }

    public ArticleDetailResponse makeArticleDetailResponse(Article article, User user){
        // 본인이 입력하고, 본인이 수정하는 데 사용되는 메소드이다.
        return ArticleDetailResponse.builder()
                .articleId(article.getArticleId())
                .articleTitle(article.getArticleTitle())
                .articleContent(article.getArticleContent())
                .articleLike(article.getArticleLike())
                .view(article.getView())
                .createdDate(article.getCreatedDate())
                .authorId(user.getUserId())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImageUrl())
                .isArticleLiked(false) // 일단, 자기가 좋아요했는데 수정하는 부분을 배제
                .isAuthorFollowed(false)
                .follower(user.getFollowerCount())
                .thumbnailImageUrl(article.getThumbnailImageUrl())
                .videoUrl(article.getVideo().getFullVideoUrl())
                .score(article.getVideo().getScore())
                .build();
    }


}
