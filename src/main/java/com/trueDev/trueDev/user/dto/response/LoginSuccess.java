package com.trueDev.trueDev.user.dto.response;

import com.trueDev.trueDev.springSecurity.dto.TokenDto;
import io.swagger.v3.oas.annotations.media.Schema;

// 로그인 성공 응답
@Schema(name = "로그인 성공 응답")
public record LoginSuccess(TokenDto token, LoginUser user) { }