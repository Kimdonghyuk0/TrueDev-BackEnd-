package com.trueDev.trueDev.article.dto.response;


import com.trueDev.trueDev.base.dto.response.AuthorRes;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "게시글 목록 응답에 사용할 Summary DTO")
public record ArticleSummaryRes(
        Long postId,
        String title,
        Integer likeCount,
        Integer viewCount,
        Integer commentCount,
        LocalDateTime createdAt,
        LocalDateTime editedAt,
        AuthorRes author
) {}
