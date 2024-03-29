import { useState, useEffect } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import SearchBar from "./SearchBar";
import Notification from "./Notification";
import LoadingConvert from "./LoadingConvert.jsx";
import * as N from "./NavigationBar.style";
import { loginState, userState } from "../../recoil/LoginState.js";
import { useRecoilValue, useRecoilState, useSetRecoilState } from "recoil";
import { logout } from "../../api/auth.js";
import { userDetails } from "../../api/user.js";
import { userInfo } from "../../api/myPage.js";
import {
  alarmOccuredState,
  alarmListState,
  convertAlarmState,
  startToConvertState,
} from "../../recoil/AlarmState";

export default function Navbar() {
  const [activeButton, setActiveButton] = useState("");
  const setFindUserInfo = useSetRecoilState(userState);
  const [isLoggedIn, setLoginState] = useRecoilState(loginState);
  const userDetailsInfo = useRecoilValue(userState);
  const navigate = useNavigate();
  const isLogin = useRecoilValue(loginState);
  const [alarmList, setAlarmList] = useRecoilState(alarmListState);
  const [isConverted, setIsConverted] = useRecoilState(convertAlarmState);
  const [convertStarted, setConvertStarted] = useRecoilState(startToConvertState); // Recoil 상태를 로컬 상태로 변경
  const startConverting = useRecoilValue(startToConvertState);

  const logoutHandler = () => {
    logout(setLoginState)
      .then((res) => {
        // Recoil 상태 초기화
        setFindUserInfo({
          email: "",
          nickname: "",
          birthDate: "",
          introduceText: "",
          gender: "",
          profileImageUrl: null,
        });
        setAlarmList({});
        setIsConverted(null);
        setConvertStarted(null);
        navigate("/");
      })
      .catch((err) => console.error(err));
  };

  //console.log("navuser", userDetailsInfo);
  // useEffect(() => {
  //   userInfo(userDetailsInfo.nickname)
  //     .then((res) => {
  //       //setUserDetail(res);
  //       setFindUserInfo({
  //         ...finduserInfo,
  //         profileImageUrl: res.profileImageUrl,
  //       });
  //       // console.log(res);
  //     })
  //     .catch((err) => {
  //       console.error(err);
  //       if (err.response.status === 404) {
  //         alert(err.response.data[0].message);
  //         navigate("/");
  //       }
  //     });
  // }, []);

  // window.addEventListener("unload", deleteToken)
  // function deleteToken() {
  // 	localStorage.removeItem("token")
  // 	localStorage.removeItem("localStorage")
  // }

  //console.log("convert을 시작했는지 " , convertStarted);

  return (
    <N.NavArea>
      <N.NavRed />
      <N.NavTextArea>
        <N.NavLeft>
          <N.NavLeftContainer>
            <Link to="/">
              <N.NavHome onClick={() => setActiveButton("Home")} $active={activeButton === "Home"}>
                Home
              </N.NavHome>
            </Link>
            <N.Square />
          </N.NavLeftContainer>
          <N.NavLeftContainer>
            <Link to="/create">
              <N.NavPractice
                onClick={() => setActiveButton("Create")}
                $active={activeButton === "Create"}
              >
                Practice
              </N.NavPractice>
            </Link>
            <N.Square />
          </N.NavLeftContainer>
          <N.NavLeftContainer>
            <Link to="/stage">
              <N.NavStage
                onClick={() => setActiveButton("Stage")}
                $active={activeButton === "Stage"}
              >
                Stage
              </N.NavStage>
              <N.Square />
            </Link>
          </N.NavLeftContainer>
          <N.NavLeftContainer>
            <Link to={`/profile/${userDetailsInfo.nickname}`}>
              <N.NavProfile
                onClick={() => setActiveButton("Profile")}
                $active={activeButton === "Profile"}
              >
                Profile
              </N.NavProfile>
            </Link>
            <N.Square />
          </N.NavLeftContainer>
        </N.NavLeft>
        <N.NavRight>
          {startConverting ? <LoadingConvert /> : null}
          <SearchBar />
          {isLogin ? (
            <N.AlertButton>
              <Notification />
            </N.AlertButton>
          ) : null}
          <N.NavLogin>
            {isLogin ? (
              <N.NavLoginWrapper>
                <Link to="/setting">
                  <N.NavUserProfileImage src={userDetailsInfo.profileImageUrl} />
                </Link>
                <N.NavProfileArea>
                  <Link to="/setting">
                    <N.NavUserName>{userDetailsInfo.nickname} 님</N.NavUserName>
                  </Link>
                  <N.NavLogout onClick={logoutHandler}>Logout</N.NavLogout>
                </N.NavProfileArea>
              </N.NavLoginWrapper>
            ) : (
              <N.NavLogoutWrapper>
                <N.NavSignUp>
                  <Link to="/signup">Join</Link>
                </N.NavSignUp>
                <N.NavLogin>
                  <Link to="/login">Login</Link>
                </N.NavLogin>
              </N.NavLogoutWrapper>
            )}
          </N.NavLogin>
        </N.NavRight>
      </N.NavTextArea>
    </N.NavArea>
  );
}
