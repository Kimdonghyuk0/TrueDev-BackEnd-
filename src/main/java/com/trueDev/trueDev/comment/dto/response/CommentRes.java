package com.trueDev.trueDev.comment.dto.response;

import com.trueDev.trueDev.base.dto.response.AuthorRes;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "댓글 응답")
public record CommentRes(
        Long id,
        Long postId,
        String content,
        LocalDateTime createdAt,
        LocalDateTime editedAt,
        AuthorRes author,
        boolean isAuthor //현재 유저가 작성한 댓글인지
) {}
