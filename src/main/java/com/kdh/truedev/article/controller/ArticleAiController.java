package com.kdh.truedev.article.controller;

import com.kdh.truedev.article.dto.response.ApiResponse;
import com.kdh.truedev.article.dto.response.ArticleDetailRes;
import com.kdh.truedev.article.dto.response.ArticleStatRes;
import com.kdh.truedev.article.service.ArticleService;
import com.kdh.truedev.user.support.AuthTokenResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Tag(name = "Article-AI", description = "게시글 AI 검증 및 통계 API")
@RestController
@RequestMapping
@RequiredArgsConstructor
public class ArticleAiController {

    private final ArticleService service;
    private final AuthTokenResolver authTokenResolver;

    @Operation(summary = "게시글 AI 검증")
    @PostMapping("/articles/{article_id}/verify")
    public ResponseEntity<ApiResponse<ArticleDetailRes>> verify(@PathVariable("article_id") Long id) {
        try {
            Long userId = authTokenResolver.requireUserId();
            var verified = service.verify(id, userId);
            if (verified == null) {
                return ResponseEntity.status(NOT_FOUND).body(ApiResponse.error("verify_failed"));
            }
            return ResponseEntity.ok(ApiResponse.ok("verify_success", verified));
        } catch (ArticleService.ForbiddenException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("FORBIDDEN - 게시글 검증 권한 없음"));
        }
    }

    @Operation(summary = "AI 검증 통계")
    @GetMapping("/articles/stats")
    public ResponseEntity<ApiResponse<ArticleStatRes>> stats() {
        var stat = service.stats();
        return ResponseEntity.ok(ApiResponse.ok("get_stats_success", stat));
    }
}
