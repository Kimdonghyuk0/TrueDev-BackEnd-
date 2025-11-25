package com.trueDev.trueDev.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;

@Schema(name = "AccountUpdateReq", description = "회원 정보 수정 요청")
public record AccountUpdateReq(
        @Nullable String name,
        @Nullable String email,
        @Nullable String profileImage
) {}
