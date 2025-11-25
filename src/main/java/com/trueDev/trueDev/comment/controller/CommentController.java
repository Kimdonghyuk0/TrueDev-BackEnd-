package com.trueDev.trueDev.comment.controller;

import com.trueDev.trueDev.article.dto.response.ApiResponse;
import com.trueDev.trueDev.comment.dto.response.CommentPageRes;
import com.trueDev.trueDev.comment.dto.response.CommentRes;
import com.trueDev.trueDev.comment.service.CommentService;
import com.trueDev.trueDev.comment.dto.request.CommentReq;
import com.trueDev.trueDev.user.support.AuthTokenResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Tag(name = "Comment", description = "Comment API")
@RestController
@RequestMapping
@RequiredArgsConstructor
public class CommentController {
    private final CommentService service;
    private final AuthTokenResolver authTokenResolver;

    // 댓글 목록
    @Operation(summary = "특정 게시물의 특정 페이지 댓글 목록")
    @GetMapping("/articles/{article_id}/comments")
    public ResponseEntity<ApiResponse<CommentPageRes>> getCommentsList(@RequestParam(defaultValue = "1")int page,
                                                                       @PathVariable("article_id") Long articleId){
        Long userId = authTokenResolver.requireUserId();
        if (page < 1)  return ResponseEntity.status(BAD_REQUEST).body(ApiResponse.error("invalid_request"));
        int size = 10;
        var comments = service.getCommentList(articleId,userId,page,size);
        return ResponseEntity.ok(ApiResponse.ok("get_comment_list_success",comments));
    }

    // 댓글 작성
    @Operation(summary = "댓글 작성")
    @PostMapping("/articles/{article_id}/comments")
    public ResponseEntity<ApiResponse<CommentRes>> createComment(@PathVariable("article_id") Long articleId,
                                                @Valid @RequestBody CommentReq.CreateCommentReq req) {
        Long userId = authTokenResolver.requireUserId();
        var res = service.createComment(articleId, userId, req);
        if (res == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("comment_created_failed"));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("comment_created_success", res));
    }

    // 댓글 수정
    @Operation(summary = "댓글 수정")
    @PatchMapping("/articles/{article_id}/comments/{comment_id}")
    public ResponseEntity<ApiResponse<CommentRes>> editComment(@PathVariable("article_id") Long articleId,
                                                               @PathVariable("comment_id") Long commentId,
                                                               @Valid @RequestBody CommentReq.EditCommentReq req) {
        Long userId = authTokenResolver.requireUserId();
        var res = service.editComment(articleId, commentId, userId, req);
        if (res == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("comment_edited_failed"));
        return ResponseEntity.ok(ApiResponse.ok("comment_edited_success", res));
    }

    // 댓글 삭제
    @Operation(summary = "댓글 삭제")
    @DeleteMapping("/articles/{article_id}/comments/{comment_id}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(@PathVariable("article_id") Long articleId,
                                                @PathVariable("comment_id") Long commentId) {
        Long userId = authTokenResolver.requireUserId();
        boolean ok = service.deleteComment(articleId, commentId, userId);
        if (!ok) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error("delete_failed"));
        return ResponseEntity.ok(ApiResponse.ok("delete_success"));
    }
}
