package com.kdh.truedev.user.controller;

import com.kdh.truedev.base.dto.response.ApiResponse;
import com.kdh.truedev.springSecurity.dto.TokenDto;
import com.kdh.truedev.user.dto.request.AccountUpdateReq;
import com.kdh.truedev.user.dto.request.LoginRequest;
import com.kdh.truedev.user.dto.request.UserReq;
import com.kdh.truedev.user.dto.response.AccountUpdateRes;
import com.kdh.truedev.user.dto.response.LoginSuccess;
import com.kdh.truedev.user.dto.response.LoginUser;
import com.kdh.truedev.user.service.UserService;
import com.kdh.truedev.user.support.AuthTokenResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@Tag(name = "User", description = "User API")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService service;
    private final AuthTokenResolver authTokenResolver;

    // 회원가입
    @Operation(summary = "회원가입")
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signup(@Valid @RequestPart("user")UserReq dto,
                                                    @RequestPart(value = "profileImage", required = false) MultipartFile profileImage) {
        service.signup(dto,profileImage);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("register_success", null));
    }

    // 회원 조회
    @Operation(summary = "회원 정보 조회")
    @GetMapping("/")
    public ResponseEntity<ApiResponse<LoginUser>> get() {
        Long userId = authTokenResolver.requireUserId();
        LoginUser user = service.get(userId);
        return ResponseEntity.ok(ApiResponse.ok("ok", user));
    }

    // 로그인
    @Operation(summary = "로그인")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginSuccess>> login(@Valid @RequestBody LoginRequest req) {

            LoginSuccess user = service.login(req.email(), req.password());

        return ResponseEntity.ok(ApiResponse.ok("login_success",user));
    }


    @Operation(summary = "로그아웃")
    @SecurityRequirement(name = "BearerAuth")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        Long userId = authTokenResolver.requireUserId();
        service.logout(userId);
        return ResponseEntity.ok(ApiResponse.ok("Logout_success", null));

    }
    //회원탈퇴
    @Operation(summary = "회원탈퇴")
    @SecurityRequirement(name = "BearerAuth")
    @DeleteMapping("/account")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(){
        Long userId = authTokenResolver.requireUserId();
        boolean isDeleted = service.deleteAccount(userId);
        if(!isDeleted)return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error("delete_failed"));
        return ResponseEntity.ok(ApiResponse.ok("delete_success",null));
    }

    // 회원 정보 변경
    @Operation(summary = "회원 정보 변경")
    @SecurityRequirement(name = "BearerAuth")
    @PatchMapping("/account")
    public ResponseEntity<ApiResponse<AccountUpdateRes>> updateAccount(@Valid @RequestBody AccountUpdateReq req) {
        Long userId = authTokenResolver.requireUserId();
        AccountUpdateRes updated = service.updateAccount(userId, req);
        if (updated == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("account_update_failed"));
        }
        return ResponseEntity.ok(ApiResponse.ok("account_update_success", updated));
    }

    // 비밀번호 변경
    @Operation(summary = "비밀번호 변경")
    @SecurityRequirement(name = "BearerAuth")
    @PatchMapping("/account/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(String currentPassword,String newPassword){
        Long userId = authTokenResolver.requireUserId();

        service.changePassword(userId,currentPassword,newPassword);
        return ResponseEntity.ok(ApiResponse.ok("password_change_success", null));

    }

    @Operation(summary = "토큰 재발급")
    @PostMapping("/token/refresh")
    public ResponseEntity<ApiResponse<TokenDto>> refreshToken(@RequestHeader("Refresh-Token") String refreshTokenHeader) {
        String refreshToken = authTokenResolver.resolveRefreshToken(refreshTokenHeader);
        TokenDto tokenDto = service.reissue(refreshToken);
        return ResponseEntity.ok(ApiResponse.ok("token_reissue_success", tokenDto));
    }
}
