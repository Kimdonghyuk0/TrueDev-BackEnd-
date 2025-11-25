package com.trueDev.trueDev.comment.dto.response;

import java.util.List;

public record CommentPageRes(
        List<CommentRes> comments,
        int page,                   // 현재 몇 페이지인지 (1 페이지부터 시작임)
        int size,                   // 페이지당 글 갯수
        int totalPages,             // 전체 페이지 수
        long totalArticles,         // 전체 글 수
        boolean hasNext,            // 다음 페이지가 있는지
        boolean hasPrev             // 이전 페이지가 있는지
) {
}
