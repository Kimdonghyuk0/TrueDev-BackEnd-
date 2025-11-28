package com.kdh.truedev.article.controller;

import com.kdh.truedev.article.dto.response.ArticleDetailRes;
import com.kdh.truedev.article.dto.response.ArticlePageRes;
import com.kdh.truedev.article.service.ArticleService;
import com.kdh.truedev.article.dto.request.ArticleReq;
import com.kdh.truedev.article.dto.response.ApiResponse;
import com.kdh.truedev.user.dto.request.AccountUpdateReq;
import com.kdh.truedev.user.support.AuthTokenResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static org.springframework.http.HttpStatus.*;

@Tag(name = "Article", description = "Article API")
@RestController
@RequestMapping
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService service;
    private final AuthTokenResolver authTokenResolver;



    // 특정 페이지 목록
    @Operation(summary = "특정 페이지 게시글 목록 불러오기")
    @GetMapping("/articles")
    public ResponseEntity<ApiResponse<ArticlePageRes>> getArticleList(@RequestParam(defaultValue = "1") int page) {
        if (page < 1)  return ResponseEntity.status(BAD_REQUEST).body(ApiResponse.error("invalid_request"));
        int size = 10;
        var articles = service.list(page, size);
        return ResponseEntity.ok(ApiResponse.ok("get_list_success",articles));
    }

    @Operation(summary = "내가 쓴 글 목록 조회")
    @GetMapping("/myArticles")
    public ResponseEntity<ApiResponse<ArticlePageRes>> getMyArticlesList(@RequestParam(defaultValue = "1") int page) {
        Long userId = authTokenResolver.requireUserId();
        if (page < 1) return ResponseEntity.status(BAD_REQUEST).body(ApiResponse.error("invalid_request"));
        int size = 3;
        var articles = service.list(page, size,userId);
        return ResponseEntity.ok(ApiResponse.ok("get_list_success",articles));
    }


    // 작성
    @Operation(summary = "글 작성")
    @PostMapping(value = "/articles", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ArticleDetailRes>> create(@Valid @RequestPart("article") ArticleReq.CreateArticleReq req,
                                                                @Parameter(
                                                                        description = "첨부 이미지 (선택)",
                                                                        content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE,
                                                                                schema = @Schema(type = "string", format = "binary"))
                                                                )
                                                                @RequestPart(value = "profileImage", required = false)
                                                                MultipartFile profileImage) {
        Long userId = authTokenResolver.requireUserId();
        var a = service.create(userId, req, profileImage);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("post_created_success", a));
    }


    // 상세
    @Operation(summary = "게시글 조회")
    @GetMapping("/articles/{article_id}")
    public ResponseEntity<ApiResponse<ArticleDetailRes>> detail(@PathVariable("article_id") Long articleId) {
        Long userId = authTokenResolver.resolveUserIdIfPresent();
        var article = service.detail(userId, articleId, true);
        if (article == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("not_found"));

        return ResponseEntity.ok(ApiResponse.ok("post_detail_success",article));
    }

    // 수정
    @Operation(summary = "게시글 수정")
    @PatchMapping(value = "/articles/{article_id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ArticleDetailRes>> edit(@PathVariable("article_id") Long id,
                                       @Valid @RequestPart("article") ArticleReq.EditArticleReq req,
                                                              @Parameter(
                                                                      description = "프로필 이미지 (선택)",
                                                                      content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE,
                                                                              schema = @Schema(type = "string", format = "binary"))
                                                              )
                                                                  @RequestPart(value = "profileImage", required = false)
                                                                  MultipartFile profileImage ) {
        try {
            Long userId = authTokenResolver.requireUserId();
            var editedArticle = service.edit(id, userId, req,profileImage);
            if (editedArticle == null)  return ResponseEntity.status(NOT_FOUND).body(ApiResponse.error("Edited_failed"));

            return ResponseEntity.ok(ApiResponse.ok("Edited_success",editedArticle));
        } catch (ArticleService.ForbiddenException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("FORBIDDEN - 게시글 수정 권한 없음"));
        }
    }

    // 삭제
    @Operation(summary = "게시글 삭제")
    @DeleteMapping("/articles/{article_id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable("article_id") Long articleId) {
       try{
           Long userId = authTokenResolver.requireUserId();
           boolean isDelete = service.delete(articleId,userId);
           if (!isDelete) return ResponseEntity.status(BAD_REQUEST).body(ApiResponse.error("Delete_failed"));
           return ResponseEntity.ok(ApiResponse.ok("Delete_Success"));
       } catch (ArticleService.ForbiddenException e){
           return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("FORBIDDEN - 게시글 삭제 권한 없음"));
       }

    }



    // 좋아요
    @Operation(summary = "좋아요")
    @PostMapping("/articles/{article_id}/likes")
    public ResponseEntity<ApiResponse<Void>> like(@PathVariable("article_id") Long article_id) {
        Long userId = authTokenResolver.requireUserId();
        boolean isAction = service.like(article_id, userId);
        if (!isAction) return ResponseEntity.status(CONFLICT).body(ApiResponse.error("이미 좋아요를 눌렀습니다."));
        return ResponseEntity.ok(ApiResponse.ok("like_success"));
    }

    //좋아요 취소

    @Operation(summary = "좋아요 취소")
    @DeleteMapping("/articles/{article_id}/likes")
    public ResponseEntity<ApiResponse<Void>> unlike(@PathVariable("article_id") Long article_id) {
        Long userId = authTokenResolver.requireUserId();
        boolean a = service.unlike(article_id, userId);
        if (!a) return ResponseEntity.status(NOT_FOUND).body(ApiResponse.error("unlike_failed"));
        return ResponseEntity.ok(ApiResponse.ok("unlike_success"));
    }

}
