package com.trueDev.trueDev.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;


@Schema(name = "UserRes", description = "사용자 응답")
public record UserReq(
        Long id,
        String email,
        String name,
        String password,
        @Nullable String profileImage
) {}