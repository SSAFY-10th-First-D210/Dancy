import React, { useState, useRef, useEffect } from "react";
// import PlaybackRate from "./PlaybackRate";
import * as VP from "./VideoPlayer.style";


const VideoPlayer = React.memo(({ src }) => {
  const videoRef = useRef();
  // const [playbackRate, setPlaybackRate] = useState(1);
  // const [volume, setVolume] = useState(1);
  // const [isPlaying, setIsPlaying] = useState(false);
  // const [isMuted, setIsMuted] = useState(false);
  // const [isVolumeControlHovered, setIsVolumeControlHovered] = useState(false);
  // const [like, setLike] = useState(false);
  // const [save, setSave] = useState(false);
  // const [likeCount, setLikeCount] = useState(0);

  // const handlePlayPause = () => {
  //   if (videoRef.current.paused) {
  //     videoRef.current.play();
  //     setIsPlaying(true);
  //   } else {
  //     videoRef.current.pause();
  //     setIsPlaying(false);
  //   }
  // };

  // const handleVolumeChange = (e) => {
  //   setVolume(e.target.value);
  //   videoRef.current.volume = volume;
  // };

  // const handlePlaybackRateChange = (rate) => {
  //   setPlaybackRate(rate);
  //   videoRef.current.playbackRate = rate;
  // };

  // const handleFullScreen = () => {
  //   if (videoRef.current.requestFullscreen) {
  //     videoRef.current.requestFullscreen();
  //   }
  // };

  // const handleMute = () => {
  //   setIsMuted(!isMuted);
  //   if (!isMuted) {
  //     setVolume(0);
  //     videoRef.current.volume = 0;
  //   } else {
  //     setVolume(1);
  //     videoRef.current.volume = 1;
  //   }
  // };

  // const handleVolumeBtnMouseUp = () => {
  //   if (isMuted || isVolumeControlHovered) return;
  //   setIsVolumeControlHovered(false);
  // };

  const [index, setIndex] = useState(0);

  useEffect(() => {
    // 페이지가 처음 로드될 때 또는 의존성 배열의 값이 변경될 때 실행됨
    // 여기서는 페이지 로드 시에 실행되도록 설정
    setIndex(prevIndex => prevIndex + 1);
    console.log(index)
  }, []); // 빈 배열을 전달하여 페이지 로드 시에만 실행되도록 함

  return (
		<VP.VideoPlayerContainer>
			<video ref={videoRef} controls>
				<source key={index} src={src} type="video/mp4" />
			</video>
			{/* <ControlsWrapper>
				<Controls>
					<div>
						<PlayBtn onClick={handlePlayPause}>
							<img src={isPlaying ? "/src/assets/pause.png" : "/src/assets/play.png"} alt="재생/일시정지" />
						</PlayBtn>
						<VolumeControl isOpen={!isMuted && isVolumeControlHovered} onMouseEnter={() => setIsVolumeControlHovered(true)} onMouseLeave={() => setIsVolumeControlHovered(false)}>
							<button onClick={handleMute}>
								<img src={isMuted ? "/src/assets/mute.png" : "/src/assets/volume.png"} alt="볼륨" />
							</button>
							<input
								type="range"
								min="0"
								max="1"
								step="0.1"
								value={volume}
								onChange={handleVolumeChange}
							/>
						</VolumeControl>
					</div>
				</Controls>
				<VideoRightOptions>
					<PlaybackRate onChange={handlePlaybackRateChange} />
					<button onClick={handleFullScreen}>
						<img src="/src/assets/fullscreen.png" alt="전체화면" />
					</button>
				</VideoRightOptions>
			</ControlsWrapper> */}
		</VP.VideoPlayerContainer>
  );
});

export default VideoPlayer;
