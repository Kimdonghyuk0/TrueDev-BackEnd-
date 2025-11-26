package com.kdh.truedev.article.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "AI 검증 통계 응답")
public record ArticleStatRes(
        long verified,
        long pending,
        long failed,
        long total
) {}

