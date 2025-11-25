package com.trueDev.trueDev.base.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "게시글 작성자 응답")
public record AuthorRes(String userName, String profileImage) {}