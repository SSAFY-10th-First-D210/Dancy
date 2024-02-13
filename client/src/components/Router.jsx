import React, { useEffect } from "react";
import { Route, Routes, useNavigate, Navigate, useLocation } from "react-router-dom"; // useNavigate import
import Home from "../pages/Home";
import Create from "../pages/Create";
import Practice from "../pages/Practice";
import Stage from "../pages/Stage/Stage";
import Profile from "../pages/Profile";
import Login from "./Login/LoginForm";
import SignUp from "../pages/SignUp";
import SearchResult from "../pages/SearchResult";
import VideoDetailPage from "../pages/VideoDetailPage";
import UserSetting from "../pages/UserSetting";
import FindMyPwd from "../pages/FindMyPwd";
import JoinForm from "./Join/JoinForm";
import JoinComplete from "./Join/JoinComplete";
import SendPin from "../pages/FindMyPwd";
import { loginState } from "../recoil/LoginState";
import { alarmOccuredState } from "../recoil/AlarmState";
import { useRecoilState, useRecoilValue } from "recoil";
import * as ssePolyfill from "event-source-polyfill";

export default function Router({ cardDetails, videoDetails }) {
  const isLoggedIn = useRecoilValue(loginState);
  const [isAlarmOccur, setIsAlarmOccur] = useRecoilState(alarmOccuredState);
  const navigate = useNavigate();
  const location = useLocation();

  useEffect(() => {
    // 로그인 상태가 아니고, 현재 페이지가 로그인 페이지나 회원가입 페이지가 아닐 경우 로그인 페이지로 이동하도록 이동
    if (
      !isLoggedIn &&
      location.pathname !== "/" &&
      location.pathname !== "/login" &&
      location.pathname !== "/signup" &&
      location.pathname !== "/signup/joinform" &&
      location.pathname !== "/signup/joincomplete" &&
      location.pathname !== "/findpassword"
    ) {
      navigate("/login");
    }
  }, [isLoggedIn, navigate, location]);

  useEffect(() => {
    let eventSource;
    if (isLoggedIn) {
      const fetchSse = async () => {
        try {
          const alarmConnectEndpoint = "http://i10d210.p.ssafy.io:8080/alarm/subscribe";
          eventSource = new ssePolyfill.EventSourcePolyfill(alarmConnectEndpoint, {
            headers: { "AUTH-TOKEN": `${localStorage.getItem("token")}` },
          });

          eventSource.addEventListener("heartbeat", function (event) {
            console.log("Latest news:", event.data);
          });

          eventSource.addEventListener("convert_complete", function (event) {
            console.log("result video id : ", event.data);
          });

          eventSource.addEventListener("notification", function (event) {
            console.log("notification : ", event.data);
            if (event.data) {
              setIsAlarmOccur(true); // 알람이 왔다고 알려줍니다.
            }
          });

          /* EVENTSOURCE ONERROR ------------------------------------------------------ */
          eventSource.onerror = async (event) => {
            if (!event.error.message.includes("No activity")) eventSource.close();
          };
        } catch (error) {}
      };
      fetchSse();
      return () => eventSource.close();
    } else {
      if (eventSource != undefined || eventSource != null) {
        eventSource.close();
      }
      eventSource = null;
    }
  }, [isLoggedIn]);

  return (
    <>
      <Routes>
        {isLoggedIn ? (
          <>
            <Route path="/" element={<Home />} />
            <Route path="/create" element={<Create />} />
            <Route path="/practice" element={<Practice />} />
            <Route path="/stage" element={<Stage />} />
            <Route path="/profile/:user_id" element={<Profile />} />
            <Route path="/detail/:articleId" element={<VideoDetailPage />} />
            <Route path="/results" element={<SearchResult cardDetails={cardDetails} />} />
            <Route path="/setting" element={<UserSetting />} />
            <Route path="*" element={<Navigate replace to="/" />} />
          </>
        ) : (
          <>
            <Route path="/" element={<Home />} />
            <Route path="/login" element={<Login />} />
            <Route path="/signup" element={<SignUp />} />
            <Route path="/signup/joinform" element={<JoinForm />} />
            <Route path="/signup/joincomplete" element={<JoinComplete />} />
            <Route path="/findpassword" element={<SendPin />} />
            {/* <Route path="*" element={<Login />} /> */}
          </>
        )}
      </Routes>
    </>
  );
}
