package com.kdh.truedev.article.dto.response;

import com.kdh.truedev.base.dto.response.AuthorRes;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "게시글 상세 응답")
public record ArticleDetailRes(
        Long postId,
        String title,
        String content,
        int likeCount,
        int viewCount,
        int commentCount,
        LocalDateTime createdAt,
        LocalDateTime editedAt,
        AuthorRes author,
        boolean likedByMe,
        boolean isAuthor, //현재 유저가 작성한 글인지
        String image, // 업로드한 이미지 (추후에 리스트로 변경 예정)
        String aiMessage, // AI 피드백 메시지
        boolean isVerified, // AI 검증 통과 여부
        boolean isCheck // AI 검증을 했는지 (false면 fastApi서버 오류 -> 추후 재시도)
) {}
