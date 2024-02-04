package com.ssafy.dancy.controller;

import com.ssafy.dancy.entity.User;
import com.ssafy.dancy.message.response.FollowResponse;
import com.ssafy.dancy.service.follow.FollowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/follow")
public class FollowController {

    private final FollowService followService;

    @GetMapping("/get-followings/{nickname}")
    public List<FollowResponse> getUserListFollowing(@PathVariable String nickname){
        return followService.listFollowings(nickname);
    }

    @GetMapping("/get-followers/{nickname}")
    public List<FollowResponse> getUserListFollower(@PathVariable String nickname){
        return followService.listFollowers(nickname);
    }

    @PostMapping("/request-follow")
    public String follow(@AuthenticationPrincipal User user, @RequestBody String toNickname){
        return followService.follow(user,toNickname);
    }

    @DeleteMapping("/request-unfollow")
    public String unFollow(@AuthenticationPrincipal User user, @RequestBody String toNickname){
        return followService.unFollow(user,toNickname);
    }
}
