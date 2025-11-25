package com.trueDev.trueDev.article.dto.response;


import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "게시글 목록 응답")
public record ArticlePageRes (
    List<ArticleSummaryRes> articles,
    int page,                   // 현재 몇 페이지인지 (1 페이지부터 시작임)
    int size,                   // 페이지당 글 갯수
    int totalPages,             // 전체 페이지 수
    long totalArticles,         // 전체 글 수
    boolean hasNext,            // 다음 페이지가 있는지
    boolean hasPrev             // 이전 페이지가 있는지
){}
