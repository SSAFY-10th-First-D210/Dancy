package com.ssafy.dancy.controller;

import com.ssafy.dancy.entity.User;
import com.ssafy.dancy.message.annotation.user.Nickname;
import com.ssafy.dancy.message.request.user.ChangeProfileImageRequest;
import com.ssafy.dancy.message.request.user.IntroduceTextChangeRequest;
import com.ssafy.dancy.message.request.user.NicknameRequest;
import com.ssafy.dancy.message.request.user.SignUpRequest;
import com.ssafy.dancy.message.response.user.ChangeIntroduceResponse;
import com.ssafy.dancy.message.response.user.ChangedProfileImageResponse;
import com.ssafy.dancy.message.response.user.UpdatedUserResponse;
import com.ssafy.dancy.message.response.user.UserDetailInfoResponse;
import com.ssafy.dancy.service.user.UserService;
import com.ssafy.dancy.type.Role;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
@Validated
public class UserController {

    private final UserService userService;

    @PostMapping(value = "/signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UpdatedUserResponse signup(@Valid @ModelAttribute SignUpRequest request){
       return userService.signup(request, Set.of(Role.USER));
    }

    @GetMapping("/exists/{nickname}")
    public void checkDuplicateNickname(@Nickname @PathVariable String nickname){
        userService.checkDuplicateNickname(nickname);
    }

    @PutMapping("/nickname")
    public UpdatedUserResponse changeNickname(@AuthenticationPrincipal User user, @Valid @RequestBody NicknameRequest request){
        return userService.changeNickname(user, request.nickname());
    }

    @PutMapping("/introduce")
    public ChangeIntroduceResponse changeIntroduceText(@AuthenticationPrincipal User user,
                                                       @Valid @RequestBody IntroduceTextChangeRequest request){
        return userService.changeIntroduceText(user, request);
    }

    @GetMapping("/details")
    public UserDetailInfoResponse getOwnDetailInfo(@AuthenticationPrincipal User user){
        return userService.getOwnDetailInfo(user);
    }

    @PutMapping(value = "/profile_image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ChangedProfileImageResponse changeProfileImage(@AuthenticationPrincipal User user,
                                                          @Valid @ModelAttribute ChangeProfileImageRequest request){
        return userService.changeProfileImage(user, request);
    }
}
