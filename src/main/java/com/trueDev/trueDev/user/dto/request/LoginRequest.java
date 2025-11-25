package com.trueDev.trueDev.user.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
@Schema(name = "UserLoginReq", description = "로그인 요청")
public record LoginRequest(
        @Email @NotBlank
        @Schema(example = "user@example.com")
        String email,

        @NotBlank @Size(min = 4, max = 18)
        @Schema(accessMode = Schema.AccessMode.WRITE_ONLY)
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        String password
) {}