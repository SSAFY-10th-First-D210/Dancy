import time
import cv2
import mediapipe as mp
import numpy as np
import json
import os
import metric
import config

small_parts = {
    "left thigh": [1, 2],
    "left calf": [2, 3],
    "right thigh": [6, 7],
    "right calf": [7, 8],
    "left arm": [14, 15],
    "left forearm": [15, 16],
    "right arm": [19, 20],
    "right forearm": [20, 21],
    "body1": [6, 14],
    "body2": [1, 19],
    "total": [1, 1]}

small_name = list(small_parts.keys())


def compare_video(music_name, sync_frame):
    # compare_video 동작 체크
    print("compare_video 진입 ===")

    file_path_prac = f"./dataset/video/{music_name}_prac.mp4"
    pTime = 0

    mp_pose = mp.solutions.pose

    # 경로 설정
    key_path = "./dataset/json/"
    video_path = "./datast/target/"
    gt_path = f"{music_name}_gt.mp4"
    target_video = f"{music_name}_prac.mp4"

    # video 정보 저장 파일 생성
    os.makedirs(os.path.join(key_path, target_video), exist_ok=True)

    # video 불러오기 및 video 설정 저장
    # FRAME_WINDOW = st.image([])

    # cap = cv2.VideoCapture(os.path.join(video_path, target_video))
    cap = cv2.VideoCapture(f"./dataset/video/{music_name}_prac.mp4")  # 비디오 한장 캡쳐
    fourcc = cv2.VideoWriter_fourcc(*'mp4v')  # 비디오 형식 정하기
    fps = cap.get(cv2.CAP_PROP_FPS)  # 비디오 캡쳐 프레임 속도 가져오기

    # 비디오 정보
    video_inform = {
        'frame_width': int(cap.get(cv2.CAP_PROP_FRAME_WIDTH)),
        'frame_height': int(cap.get(cv2.CAP_PROP_FRAME_HEIGHT)),
        'video_fps': cap.get(cv2.CAP_PROP_FPS),
        'total_frame': int(cap.get(cv2.CAP_PROP_FRAME_COUNT))
    }
    # prac 영상의 info.json 을 쓰는 코드
    with open(os.path.join(key_path, target_video, f'_info.json'), "w") as f:
        json.dump(video_inform, f, indent='\t')

    # gt information 가져오기
    with open(os.path.join(key_path, gt_path, f'_info.json')) as json_file:
        gt_inform = json.load(json_file)  # frame_width, frame_height, video_fps, total_frame, #gt_bbox

    # imcolor = cv2.imread('colormap.jpg')

    prac_resize = (int(video_inform['frame_width'] * gt_inform['frame_height'] / video_inform['frame_height']),
                   gt_inform['frame_height'])
    gt_video = metric.VideoMetric(gt_inform['frame_width'], gt_inform['frame_height'])
    prac_video = metric.VideoMetric(prac_resize[0], prac_resize[1])

    # 비디오 체크
    print(str(prac_resize[0]) + "\n" + str(prac_resize[1]) + "\n" + str(fps))
    print(str(gt_inform['frame_width']) + "\n" + str(gt_inform['frame_height']) + "\n" + str(fps))
    print("======================================")

    # 비디오 정확도 이미지 관련
    # imcolorshape = imcolor.shape
    # imcolor = cv2.resize(imcolor, dsize=(int(imcolorshape[1] * gt_inform['frame_height'] / 2 / imcolorshape[0]), int(gt_inform['frame_height'] / 2)))

    # ==========================================분석
    # gt와 비교할 Frame 수 선정
    compare_frame = 15
    before_frame = 5
    match_frame = gt_inform['video_fps'] / video_inform['video_fps']  # 비디오 두 프레임이 다를 경우에 Sync를 맞춰줌
    threshold = 0.2
    threshold_cs = 0.8  # 변위 vector Cosine Similarity 평가 기준
    accept_frame = 5  # OK 로 평가하는 Frame 수
    sync_frame = sync_frame  # Sync를 위한 frame
    prac_temp = []
    eval_metric = ["normal"] * 10  # 시작 후 compare_frame+before_frame 동안 평가 진행 X
    eval_graph_y = [[] for _ in range(11)]
    eval_graph_x = []

    with mp_pose.Pose(min_detection_confidence=0.5, min_tracking_confidence=0.5) as pose:  # 미디어파이프 신뢰도 설정
        i = 0
        # 비디오 저장할 곳, 형식, 프레임,크기 설정
        if cap.isOpened():  # 연습영상이 열렸다면 실행
            ret, frame = cap.read()

            img_h,img_w,img_c = frame.shape

            out = cv2.VideoWriter("./dataset/result/" + music_name + "_prac_analyzed.mp4", fourcc, 30,
                                  (img_w,img_h))

            while ret is True:
                ret, frame = cap.read()

                if not ret: break
                if i >= gt_inform['total_frame'] - 1: break
                cap.grab()

                # get frame time and FPS
                # frame_time = cap.get(cv2.CAP_PROP_POS_MSEC)
                resize_frame = cv2.resize(frame, dsize=prac_resize, fx=1, fy=1, interpolation=cv2.INTER_LINEAR)

                # Recolor image to RGB
                image = cv2.cvtColor(resize_frame, cv2.COLOR_BGR2RGB)
                image.flags.writeable = False

                # Make detection
                results = pose.process(image)

                # Recolor back to BGR
                image.flags.writeable = True
                # image = cv2.cvtColor(image, cv2.COLOR_RGB2BGR)

                # Extract landmarks
                try:
                    landmarks = results.pose_landmarks.landmark
                except:
                    i = i + 1
                    continue
                # Get coordinate

                # save keypoints
                keypoints = config.make_keypoints(landmarks, mp_pose, video_inform)
                if len(prac_temp) > before_frame:
                    prac_temp = prac_temp[1:]
                prac_temp.append(keypoints)

                with open(os.path.join(key_path, gt_path,
                                       f'{max(int(i * match_frame + sync_frame), 0):0>4}.json')) as json_file:
                    gt_json = json.load(json_file)

                if i % (compare_frame) == 0:
                    s_p = max(int(i * match_frame + sync_frame) - compare_frame, before_frame)  # start point
                    e_p = min(int(i * match_frame + sync_frame) + compare_frame,
                              gt_inform['total_frame'] - 1)  # end point

                    # body part별로(왼다리, 오른다리, 왼팔, 오른팔, 몸통) normalize된 값 vector 추출
                    prac = prac_video.extract_vec_norm_by_small_part(keypoints)
                    prac_displace_prac = prac_video.extract_vec_norm_by_small_part_diff(prac_temp[0], keypoints)

                    total_eval = [[] for _ in range(len(prac) + 1)]
                    total_eval_diff = [[] for _ in range(len(prac))]
                    for j in range(s_p, e_p, 1):
                        with open(os.path.join(key_path, gt_path, f'{j:0>4}.json')) as json_file:
                            gt_temp = json.load(json_file)
                        with open(os.path.join(key_path, gt_path, f'{j - before_frame:0>4}.json')) as json_file:
                            displace_gt_temp = json.load(json_file)
                        gt = gt_video.extract_vec_norm_by_small_part(gt_temp)
                        gt_displace_prac = prac_video.extract_vec_norm_by_small_part_diff(displace_gt_temp, gt_temp)

                        s = 0
                        for part in range(len(prac)):
                            eval = metric.coco_oks(gt[part], prac[part], part) * (
                                        metric.cosine_similar(gt[part], prac[part]) / 2 + 0.5)
                            total_eval[part].append(eval)
                            total_eval_diff[part].append((gt_displace_prac[part], prac_displace_prac[
                                part]))  # metric.cosine_similar(gt_displace_prac[part], prac_displace_prac[part])/2+1)
                            s += eval
                        total_eval[-1].append(s / len(prac))  # 평균 계산!

                    eval_graph_y[-1].append(total_eval[-1][np.argmax(total_eval[-1])])  # 평균 계산
                    eval_graph_x.append(cap.get(cv2.CAP_PROP_POS_MSEC) / 1000)
                    eval_metric = []
                    for part in range(len(prac)):
                        eval_graph_y[part].append(total_eval[part][np.argmax(total_eval[part])])
                        best_point = np.argmax(total_eval[part])
                        worst_point = np.argmin(total_eval[part])
                        if total_eval[part][best_point] < threshold:  # threshold
                            eval_metric.append("NG")
                        else:
                            if np.linalg.norm(total_eval_diff[part][best_point][0]) < threshold or np.linalg.norm(
                                    total_eval_diff[part][best_point][1]) < threshold:
                                if best_point - compare_frame - accept_frame > 0:
                                    eval_metric.append(best_point)  # .append("fast")
                                elif best_point - compare_frame + accept_frame < 0:
                                    eval_metric.append(best_point)  # .append("slow")
                                else:
                                    eval_metric.append(best_point)  # .append("good")
                            else:
                                if metric.cosine_similar(total_eval_diff[part][best_point][0],
                                                         total_eval_diff[part][best_point][1]) > 0:
                                    if best_point - compare_frame - accept_frame > 0:
                                        eval_metric.append(best_point)  # .append("fast")
                                    elif best_point - compare_frame + accept_frame < 0:
                                        eval_metric.append(best_point)  # .append("slow")
                                    else:
                                        eval_metric.append(best_point)  # .append("good")
                                else:
                                    eval_metric.append("NG")

                array = (np.zeros((gt_inform['frame_height'], gt_inform['frame_width'], 3)) + 255).astype(np.uint8)
                prac_image = prac_video.visual_back_color(image, keypoints, eval_metric)
                gt_image = gt_video.visual_back_color(array, gt_json, eval_metric)

                # 두개의 이미지 하나는 스켈레톤, 하나는 연습영상에 스켈레톤 씌워진것을 가로로 병합하는 코드
                # image = cv2.hconcat([gt_image, prac_image])

                cTime = time.time()
                fps = 1 / (cTime - pTime)
                pTime = cTime

                # 비디오 정확도 이미지 관련
                # image[int(imcolor.shape[0] / 2):int(imcolor.shape[0] / 2) + imcolor.shape[0], 50:50 + imcolor.shape[1],
                # :] = imcolor

                # 각 이미지에 GT와 정확도 숫자를 표기하는 코드
                # cv2.putText(image, str(int(fps)), (70, 50), cv2.FONT_HERSHEY_PLAIN, 3, (0, 0, 0), 3)  # FPS 삽입
                # cv2.putText(image, 'GT', (gt_inform['frame_width'] // 2 - 25, 50), cv2.FONT_HERSHEY_PLAIN, 2, (0, 0, 0), 3)

                # 시간이지남에 따라 이미지 frame+1 하는 코드
                i = i + 1

                # cv2.imshow("Mediapipe Feed", image)
                #
                # FRAME_WINDOW.image(image) #streamlit에서 보여주는 코드
                out.write(prac_image)  # out을 위에 지정한 위치에 저장하는 코드

                # 저장 디버그 체크

        print("저장완료 되었습니다.")
        print("======================================")
        cap.release()
        out.release()
