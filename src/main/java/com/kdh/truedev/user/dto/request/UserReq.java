package com.kdh.truedev.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import org.springframework.web.multipart.MultipartFile;


@Schema(name = "UserReq", description = "사용자 응답")
public record UserReq(
        String email,
        String name,
        String password
) {}