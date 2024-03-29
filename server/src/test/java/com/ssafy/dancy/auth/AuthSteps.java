package com.ssafy.dancy.auth;

import com.ssafy.dancy.message.request.auth.ChangePasswordRequest;
import com.ssafy.dancy.message.request.auth.FindPasswordRequest;
import com.ssafy.dancy.message.request.auth.LoginUserRequest;
import com.ssafy.dancy.message.request.user.SignUpRequest;
import com.ssafy.dancy.message.request.user.UserDeleteRequest;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
public class AuthSteps {

    public static final String email = "ndw8200@naver.com";
    public static final String opponentemail = "allmin9702@naver.com";
    public static final String otherEmail = "zetta_byte@naver.com";
    public static final String nickname = "dongw";
    public static final String opponentnickname = "maino";
    public static final String otherNickname = "whalesbob";
    public static final String password = "Test1122!";
    public static final String opponentpassword = "tlqkf1122!";
    public static final String newPassword = "Asdf1234!!";
    public static final String wrongPassword = "asdfasdf";
    public static final String birthDate = "2000-01-01";
    public static final String gender = "MALE";
    public static final String authType = "DANCY";




    public SignUpRequest 회원가입정보_생성(){

        return SignUpRequest.builder()
                .email(email)
                .nickname(nickname)
                .password(password)
                .gender(gender)
                .birthDate(birthDate)
                .authType(authType)
                .build();
    }


    public SignUpRequest 상대방회원가입정보_생성(){

        return SignUpRequest.builder()
                .email(opponentemail)
                .nickname(opponentnickname)
                .password(opponentpassword)
                .gender(gender)
                .birthDate(birthDate)
                .authType(authType)
                .build();
    }

    public SignUpRequest 삼자회원가입정보_생성(){
        return SignUpRequest.builder()
                .email(otherEmail)
                .nickname(otherNickname)
                .password(password)
                .gender(gender)
                .birthDate(birthDate)
                .authType(authType)
                .build();
    }

    public static LoginUserRequest 로그인요청생성(){
        return LoginUserRequest.builder()
                .email(email)
                .password(password)
                .build();
    }

    public static LoginUserRequest 상대방로그인_생성(){
        return LoginUserRequest.builder()
                .email(opponentemail)
                .password(opponentpassword)
                .build();
    }

    public static LoginUserRequest 로그인요청_잘못된정보생성(){
        return LoginUserRequest.builder()
                .email(email)
                .password(wrongPassword)
                .build();
    }

    public static ExtractableResponse<Response> 로그인요청(LoginUserRequest request){
        return RestAssured.given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .when()
                .post("/auth/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.OK.value())
                .log().all().extract();
    }

    public String 로그인액세스토큰정보(LoginUserRequest request){
        ExtractableResponse<Response> loginResponse = 로그인요청(request);
        return loginResponse.body().jsonPath().getString("accessToken");
    }

    public ChangePasswordRequest 비밀번호_변경_요청생성(){
        return ChangePasswordRequest.builder()
                .currentPassword(password)
                .newPassword(newPassword)
                .build();
    }

    public ChangePasswordRequest 비밀번호_변경_조건불만족(){
        return ChangePasswordRequest.builder()
                .currentPassword(password)
                .newPassword(wrongPassword)
                .build();
    }

    public ChangePasswordRequest 비밀번호_변경_비밀번호틀림(){
        return ChangePasswordRequest.builder()
                .currentPassword(wrongPassword)
                .newPassword(newPassword)
                .build();
    }

    public UserDeleteRequest 회원탈퇴_유저_비밀번호_입력(){
        return UserDeleteRequest.builder()
                .password(password)
                .build();
    }

    public UserDeleteRequest 회원탈퇴_유저_잘못된_비밀번호(){
        return UserDeleteRequest.builder()
                .password(newPassword)
                .build();
    }

    public FindPasswordRequest 비밀번호_찾기_새비밀번호_생성(){
        return FindPasswordRequest.builder()
                .newPassword(newPassword)
                .build();
    }

    public FindPasswordRequest 비밀번호_찾기_비밀번호형식_미준수(){
        return FindPasswordRequest.builder()
                .newPassword(wrongPassword)
                .build();
    }
}
