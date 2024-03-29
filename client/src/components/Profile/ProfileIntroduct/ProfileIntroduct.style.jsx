import styled from "styled-components";
import ProfileImgDefault from "../../../assets/profileIntro/ProfileImgDefault.png";
import settingBtn from "../../../assets/profileIntro/settingBtn.png";

export const ProfileIntroBox = styled.div`
  width: 320px;
  height: 760px;
  border: 1px solid black;
  border-radius: 10px;
  background-color: #fbf3ed;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 33px;
`;

export const Wrap = styled.div`
  align-self: center;
`;

export const ProfileRound = styled.div`
  width: 166px;
  height: 166px;
  border: 1px solid black;
  border-radius: 50%;
  display: flex;
  justify-content: center;
`;

export const ProfileImg = styled.img.attrs(({ src }) => ({
  src: src || ProfileImgDefault,
  alt: "logo",
}))`
  width: 150px;
  height: 150px;
  border-radius: 50%;
  align-self: center;
  object-fit: cover;
`;

export const UserWrap = styled.div`
  display: flex;
  align-items: center;
  column-gap: 15px;
`;

export const UserName = styled.div`
  font-family: "NYJ Gothic";
  font-weight: bold;
  font-size: 30px;
`;

export const SettingBtn = styled.button`
  width: 24px;
  height: 24px;
  background-image: url(${settingBtn});
  background-size: cover;
`;

export const FollowBox = styled.div`
  display: flex;
  align-items: center;
  gap: 31px;
  cursor: pointer;
`;

export const FollowTitle = styled.div`
  font-family: "NYJ Gothic";
  font-weight: bold;
  font-size: 18px;
  text-align: center;
`;

export const FollowNum = styled.div`
  font-family: "NYJ Gothic";
  font-weight: 400;
  font-size: 16px;
  text-align: center;
`;

export const FollowBtn = styled.button`
  width: 160px;
  height: 50px;
  border: 1px solid black;
  border-radius: 30px;
  background-color: #aabbff;
  font-family: "NYJ Gothic";
  font-weight: bold;
  font-size: 16px;
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
`;

export const Hr = styled.hr`
  border-color: #252525;
  width: 210px;
`;

export const IntroTxtBox = styled.div`
  display: flex;
  flex-direction: column;
  gap: 12px;
`;

export const IntroTitle = styled.div`
  font-family: "NanumSquareRound";
  font-size: 20px;
  font-weight: bold;
`;

export const IntroTxt = styled.div`
  width: 200px;
  font-family: "NanumSquareRound";
  font-size: 18px;
  white-space: pre-line;
`;
