package com.trueDev.trueDev.comment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public class CommentReq {
    @Schema(name = "댓글 생성 요청")
    public record CreateCommentReq(
            @NotBlank String content
    ) {}
    @Schema(name = "댓글 수정 요청")
    public record EditCommentReq(
            @NotBlank String content
    ) {}
}
