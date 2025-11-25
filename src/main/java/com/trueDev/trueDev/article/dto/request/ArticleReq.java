package com.trueDev.trueDev.article.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public class ArticleReq {
    @Schema(name = "글 생성 요청")
    public record CreateArticleReq(
            @NotBlank String title,
            @NotBlank String content
    ) {}
    @Schema(name = "글 수정 요청")
    public record EditArticleReq(
            String title,
            String content
    ) {}

}
