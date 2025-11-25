package com.trueDev.trueDev.user.dto.request;


import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "UserSignupReq", description = "회원가입 요청")
public record SignupReq(
        @Email @NotBlank
        @Schema(example = "user@example.com")
        String email,

        @NotBlank @Size(min = 8, max = 72)
        @Schema(example = "P@ssw0rd!")
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        String password,

        @NotBlank @Size(min = 2, max = 20)
        String name,

        String profileImage
) {}