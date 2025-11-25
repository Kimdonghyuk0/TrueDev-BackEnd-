package com.trueDev.trueDev.user.service;

import com.trueDev.trueDev.springSecurity.dto.TokenDto;
import com.trueDev.trueDev.user.dto.response.AccountUpdateRes;
import com.trueDev.trueDev.user.dto.response.LoginSuccess;
import com.trueDev.trueDev.user.dto.response.LoginUser;
import com.trueDev.trueDev.user.dto.request.UserReq;
import com.trueDev.trueDev.user.dto.request.AccountUpdateReq;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public interface UserService {
    void signup(UserReq dto);

    LoginUser get(Long id);

    LoginSuccess login(String email, String password); // 로그인 실패시 UnauthorizedException

    void logout(Long userId);

    boolean deleteAccount(Long userId);

    AccountUpdateRes updateAccount(Long userId, AccountUpdateReq req);

    void changePassword(Long userId,String currentPassword,String newPassword);

    TokenDto reissue(String refreshToken);

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    class UnauthorizedException extends RuntimeException {
        public UnauthorizedException() {
            super("unauthorized");

        }
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    class InvalidCredentialsException extends RuntimeException {
        public InvalidCredentialsException() {
            super("invalid_credentials"); // 아이디/비번 불일치
        }
    }

}
