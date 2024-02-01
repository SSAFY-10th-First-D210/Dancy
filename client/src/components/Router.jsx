import React from "react";
import { Route, Routes } from "react-router-dom";
import Home from "../pages/Home";
import Practice from "../pages/Practice";
import Stage from "../pages/Stage";
import Profile from "../pages/Profile";
import Login from "./Login/LoginForm";
import SignUp from "../pages/SignUp";
import SearchResult from "../pages/SearchResult";
import VideoDetail from "../pages/VideoDetail";

export default function Router({ cardDetails }) {
  return (
    <>
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/practice" element={<Practice />} />
        <Route path="/stage" element={<Stage />} />
        <Route path="/profile/:user_id" element={<Profile />} />
        <Route path="/login" element={<Login />} />
        <Route path="/signup" element={<SignUp />} />
        <Route path="/detail/:video_id" element={<VideoDetail />} />
        {/* SearchResult 페이지의 라우트 추가 */}
        <Route path="/results" element={<SearchResult cardDetails={cardDetails} />} />
        {/* VideoDetail 페이지의 라우트 추가 */}
        <Route path="/detail" element={<VideoDetail />} />
      </Routes>
    </>
  )
}
