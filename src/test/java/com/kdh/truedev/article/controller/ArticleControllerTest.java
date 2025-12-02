package com.kdh.truedev.article.controller;

import com.kdh.truedev.article.dto.response.ArticleDetailRes;
import com.kdh.truedev.article.dto.response.ArticlePageRes;
import com.kdh.truedev.article.service.ArticleService;
import com.kdh.truedev.base.dto.response.AuthorRes;
import com.kdh.truedev.exception.ApiErrorHandler;
import com.kdh.truedev.user.support.AuthTokenResolver;
import com.kdh.truedev.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@WebMvcTest(ArticleController.class)
@Import(ApiErrorHandler.class)
@AutoConfigureMockMvc(addFilters = false) // 시큐리티 필터 우회
public class ArticleControllerTest {

    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    ArticleService articleService;      // 서비스 목킹
    @MockitoBean
    AuthTokenResolver authTokenResolver; // 토큰 파싱 목킹
    @MockitoBean
    JpaMetamodelMappingContext jpaMappingContext;

    MockMultipartFile articlePart(String title, String content) {
        return new MockMultipartFile("article", "", "application/json",
                String.format("{\"title\":\"%s\",\"content\":\"%s\"}", title, content).getBytes());
    }


    @Test
    void 내가_쓴_게시글_목록_조회_200() throws Exception{
        ArticlePageRes pageRes = new ArticlePageRes(
                List.of(), 1, 3, 2, 0, false, false);

        given(authTokenResolver.requireUserId()).willReturn(1L);

        given(articleService.list(anyInt(),anyInt(),anyLong())).
                willReturn(pageRes);

        mockMvc.perform(get("/myArticles").param("page","1"))
                .andExpectAll(status().isOk())
                .andExpect(jsonPath("$.message").value("get_list_success"));

        // 목 호출 검증
        verify(authTokenResolver).requireUserId();
        verify(articleService).list(eq(1), eq(3),  eq(1L));
        verifyNoMoreInteractions(articleService); //다른 메서드 호출이 있는지
    }

    @Test
    void 내가_쓴_게시글_목록_조회_400() throws Exception{
        given(authTokenResolver.requireUserId()).willReturn(1L);
        mockMvc.perform(get("/myArticles").param("page","0"))
                .andExpectAll(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("invalid_request"));
        verify(authTokenResolver).requireUserId();
        verifyNoInteractions(articleService); //서비스 호출 없어야 함
    }

    @Test
    void 게시글_목록_조회_200() throws Exception{
        ArticlePageRes pageRes = new ArticlePageRes(
                List.of(), 1, 10, 2, 0, false, false);

        given(articleService.list(anyInt(),anyInt())).
                willReturn(pageRes);

        mockMvc.perform(get("/articles").param("page","1"))
                .andExpectAll(status().isOk())
                .andExpect(jsonPath("$.message").value("get_list_success"));

        // 목 호출 검증
        verify(articleService).list(eq(1), eq(10));
        verifyNoMoreInteractions(articleService); //다른 메서드 호출이 있는지
    }

    @Test
    void 게시글_목록_조회_400() throws Exception{

        mockMvc.perform(get("/articles").param("page","0"))
                .andExpectAll(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("invalid_request"));

        verifyNoMoreInteractions(articleService); //다른 메서드 호출이 있는지
    }

    @Test
    void 글작성_이미지추가_201() throws Exception{
        // given
        MockMultipartFile imagePart = new MockMultipartFile(
                "profileImage", "pic.png", "image/png", "fake".getBytes()
        );

        ArticleDetailRes detail = new ArticleDetailRes(
                1L, "t", "c", 0, 0, 0,
                LocalDateTime.now(), LocalDateTime.now(),
                new AuthorRes("tester", null),
                false, true, null
        );
        given(authTokenResolver.requireUserId()).willReturn(1L);
        given(articleService.create(eq(1L), any(), any())).willReturn(detail);

        // when/then
        mockMvc.perform(multipart("/articles").file( articlePart("t","c")).file(imagePart))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("post_created_success"))
                .andExpect(jsonPath("$.data.title").value("t"));

        verify(authTokenResolver).requireUserId();
        verify(articleService).create(eq(1L), any(), any());
        verifyNoMoreInteractions(articleService);
    }


    @Test
    void 글작성_이미지없음_201() throws Exception{
        // given
        ArticleDetailRes detail = new ArticleDetailRes(
                1L, "t", "c", 0, 0, 0,
                LocalDateTime.now(), LocalDateTime.now(),
                new AuthorRes("tester", null),
                false, true, null
        );
        given(authTokenResolver.requireUserId()).willReturn(1L);
        given(articleService.create(eq(1L), any(), any())).willReturn(detail);

        // when/then
        mockMvc.perform(multipart("/articles").file(articlePart("t","c")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("post_created_success"))
                .andExpect(jsonPath("$.data.title").value("t"));

        verify(authTokenResolver).requireUserId();
        verify(articleService).create(eq(1L), any(), isNull());
        verifyNoMoreInteractions(articleService);
    }

    @Test
    void 글작성_유효성실패_400() throws Exception{
        // given
        given(authTokenResolver.requireUserId()).willReturn(1L);

        // when/then
        mockMvc.perform(multipart("/articles").file(articlePart("","")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("invalid_request"));

        verifyNoInteractions(authTokenResolver);
        verifyNoInteractions(articleService);
    }

    @Test
    void 글작성_인증실패_401() throws Exception{
        given(authTokenResolver.requireUserId()).willThrow(new UserService.UnauthorizedException());

        mockMvc.perform(multipart("/articles").file(articlePart("t","c")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("unauthorized"));

        verify(authTokenResolver).requireUserId();
        verifyNoInteractions(articleService);
    }


    @Test
    void 게시글_조회_200() throws Exception {
        given(authTokenResolver.resolveUserIdIfPresent()).willReturn(1L);
        ArticleDetailRes detail = new ArticleDetailRes(
                1L, "t", "c", 0, 0, 0,
                LocalDateTime.now(), LocalDateTime.now(),
                new AuthorRes("tester", null),
                false, true, null
        );
        given(articleService.detail(eq(1L), eq(1L))).willReturn(detail);

        mockMvc.perform(get("/articles/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("post_detail_success"))
                .andExpect(jsonPath("$.data.title").value("t"));

        verify(authTokenResolver).resolveUserIdIfPresent();
        verify(articleService).detail(eq(1L), eq(1L));
        verifyNoMoreInteractions(articleService);
    }

    @Test
    void 게시글_조회_404() throws Exception {
        given(authTokenResolver.resolveUserIdIfPresent()).willReturn(1L);
        given(articleService.detail(anyLong(), anyLong())).willReturn(null);

        mockMvc.perform(get("/articles/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("not_found"));


        verify(articleService).detail(eq(1L), eq(99L));
        verifyNoMoreInteractions(articleService);
        verify(authTokenResolver).resolveUserIdIfPresent();
    }

    @Test
    void 게시글_수정_이미지없음_200() throws Exception{
        given(authTokenResolver.requireUserId()).willReturn(1L);
        ArticleDetailRes edited = new ArticleDetailRes(
                10L,                       // postId
                "new",                     // title
                "new",                     // content
                5,                         // likeCount
                12,                        // viewCount
                3,                         // commentCount
                LocalDateTime.now(),       // createdAt
                LocalDateTime.now(),       // editedAt
                new AuthorRes("tester", null), // author
                true,                      // likedByMe
                true,                      // isAuthor
                null                       // image
        );
        given(articleService.edit(eq(10L), eq(1L), any(), isNull())).willReturn(edited);

        mockMvc.perform(
                        multipart("/articles/{id}", 10L)
                                .file(articlePart("new", "new"))
                                .with(req -> { req.setMethod("PATCH"); return req; })
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Edited_success"))
                .andExpect(jsonPath("$.data.title").value("new"));

        verify(articleService).edit(eq(10L), eq(1L), any(), isNull());
        verifyNoMoreInteractions(articleService);
        verify(authTokenResolver).requireUserId();
    }

    @Test
    void 게시글_수정_이미지있음_200() throws Exception{

        MockMultipartFile image = new MockMultipartFile("profileImage","pic.png","image/png","x".getBytes());
        given(authTokenResolver.requireUserId()).willReturn(1L);
        ArticleDetailRes edited = new ArticleDetailRes(
                10L,                       // postId
                "new",                     // title
                "new",                     // content
                5,                         // likeCount
                12,                        // viewCount
                3,                         // commentCount
                LocalDateTime.now(),       // createdAt
                LocalDateTime.now(),       // editedAt
                new AuthorRes("tester", null), // author
                true,                      // likedByMe
                true,                      // isAuthor
                null                       // image
        );
        given(articleService.edit(eq(10L), eq(1L), any(), any())).willReturn(edited);

        mockMvc.perform(
                        multipart("/articles/{id}", 10L)
                                .file(articlePart("new", "new")).file(image)
                                .with(req -> { req.setMethod("PATCH"); return req; })
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Edited_success"))
                .andExpect(jsonPath("$.data.title").value("new"));

        verify(articleService).edit(eq(10L), eq(1L), any(), any());
        verifyNoMoreInteractions(articleService);
        verify(authTokenResolver).requireUserId();
    }

    @Test
    void 게시글_수정_유효성실패_404() throws Exception {
        MockMultipartFile invalidArticle = new MockMultipartFile(
                "article", "", "application/json",
                """
                {"title":"","content":""}
                """.getBytes()
        );

        mockMvc.perform(
                        multipart("/articles/{id}", 10L)
                                .file(invalidArticle)
                                .with(req -> { req.setMethod("PATCH"); return req; })
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("invalid_request"));

        verifyNoInteractions(authTokenResolver);
        verifyNoInteractions(articleService);
    }
    @Test
    void 게시글수정_인증없음_401() throws Exception {
        given(authTokenResolver.requireUserId()).willThrow(new UserService.UnauthorizedException());

        mockMvc.perform(
                        multipart("/articles/{id}", 10L)
                                .file(articlePart("title","content"))
                                .with(req -> { req.setMethod("PATCH"); return req; })
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("unauthorized"));

        verify(authTokenResolver).requireUserId();
        verifyNoInteractions(articleService);
    }

    @Test
    void 게시글_수정_권한없음_403() throws Exception {
        given(authTokenResolver.requireUserId()).willReturn(1L);
        given(articleService.edit(anyLong(), anyLong(), any(), any()))
                .willThrow(new ArticleService.ForbiddenException());

        mockMvc.perform(
                        multipart("/articles/{id}", 10L)
                                .file(articlePart("title","content"))
                                .with(req -> { req.setMethod("PATCH"); return req; })
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("forbidden"));

        verify(authTokenResolver).requireUserId();
        verify(articleService).edit(eq(10L), eq(1L), any(), any());
        verifyNoMoreInteractions(articleService);
    }

    @Test
    void 게시글_수정_IllegalArgument_400() throws Exception {
        given(authTokenResolver.requireUserId()).willReturn(1L);
        given(articleService.edit(anyLong(), anyLong(), any(), any()))
                .willThrow(new IllegalArgumentException("invalid_request"));

        mockMvc.perform(
                        multipart("/articles/{id}", 10L)
                                .file(articlePart("title","content"))
                                .with(req -> { req.setMethod("PATCH"); return req; })
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("invalid_request"));

        verify(authTokenResolver).requireUserId();
        verify(articleService).edit(eq(10L), eq(1L), any(), any());
        verifyNoMoreInteractions(articleService);
    }


    @Test
    void 게시글_삭제_200() throws Exception {
        given(authTokenResolver.requireUserId()).willReturn(1L);
        given(articleService.delete(eq(10L), eq(1L))).willReturn(true);

        mockMvc.perform(delete("/articles/{id}", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Delete_Success"));

        verify(authTokenResolver).requireUserId();
        verify(articleService).delete(eq(10L), eq(1L));
        verifyNoMoreInteractions(articleService);
    }

    @Test
    void 게시글_삭제_실패_400() throws Exception {
        given(authTokenResolver.requireUserId()).willReturn(1L);
        given(articleService.delete(anyLong(), anyLong())).willReturn(false);

        mockMvc.perform(delete("/articles/{id}", 10L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Delete_failed"));

        verify(authTokenResolver).requireUserId();
        verify(articleService).delete(eq(10L), eq(1L));
        verifyNoMoreInteractions(articleService);
    }

    @Test
    void 게시글_삭제_권한없음_403() throws Exception {
        given(authTokenResolver.requireUserId()).willReturn(1L);
        given(articleService.delete(anyLong(), anyLong())).willThrow(new ArticleService.ForbiddenException());

        mockMvc.perform(delete("/articles/{id}", 10L))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("forbidden"));

        verify(authTokenResolver).requireUserId();
        verify(articleService).delete(eq(10L), eq(1L));
        verifyNoMoreInteractions(articleService);
    }

    @Test
    void 게시글_삭제_IllegalArgument_400() throws Exception {
        given(authTokenResolver.requireUserId()).willReturn(1L);
        given(articleService.delete(anyLong(), anyLong())).willThrow(new IllegalArgumentException("invalid_request"));

        mockMvc.perform(delete("/articles/{id}", 10L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("invalid_request"));

        verify(authTokenResolver).requireUserId();
        verify(articleService).delete(eq(10L), eq(1L));
        verifyNoMoreInteractions(articleService);
    }

    @Test
    void 게시글_삭제_인증없음_401() throws Exception {
        given(authTokenResolver.requireUserId()).willThrow(new UserService.UnauthorizedException());

        mockMvc.perform(delete("/articles/{id}", 10L))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("unauthorized"));

        verify(authTokenResolver).requireUserId();
        verifyNoInteractions(articleService);
    }

    @Test
    void 좋아요_성공_200() throws Exception {
        given(authTokenResolver.requireUserId()).willReturn(1L);
        given(articleService.like(eq(5L), eq(1L))).willReturn(true);

        mockMvc.perform(post("/articles/{id}/likes", 5L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("like_success"));

        verify(authTokenResolver).requireUserId();
        verify(articleService).like(eq(5L), eq(1L));
        verifyNoMoreInteractions(articleService);
    }

    @Test
    void 좋아요_중복_409() throws Exception {
        given(authTokenResolver.requireUserId()).willReturn(1L);
        given(articleService.like(anyLong(), anyLong())).willReturn(false);

        mockMvc.perform(post("/articles/{id}/likes", 5L))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("이미 좋아요를 눌렀습니다."));

        verify(authTokenResolver).requireUserId();
        verify(articleService).like(eq(5L), eq(1L));
        verifyNoMoreInteractions(articleService);
    }

    @Test
    void 좋아요_인증없음_401() throws Exception {
        given(authTokenResolver.requireUserId()).willThrow(new UserService.UnauthorizedException());

        mockMvc.perform(post("/articles/{id}/likes", 5L))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("unauthorized"));

        verify(authTokenResolver).requireUserId();
        verifyNoInteractions(articleService);
    }

    @Test
    void 좋아요취소_성공_200() throws Exception {
        given(authTokenResolver.requireUserId()).willReturn(1L);
        given(articleService.unlike(eq(5L), eq(1L))).willReturn(true);

        mockMvc.perform(delete("/articles/{id}/likes", 5L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("unlike_success"));

        verify(authTokenResolver).requireUserId();
        verify(articleService).unlike(eq(5L), eq(1L));
        verifyNoMoreInteractions(articleService);
    }

    @Test
    void 좋아요취소_대상없음_404() throws Exception {
        given(authTokenResolver.requireUserId()).willReturn(1L);
        given(articleService.unlike(anyLong(), anyLong())).willReturn(false);

        mockMvc.perform(delete("/articles/{id}/likes", 5L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("unlike_failed"));

        verify(authTokenResolver).requireUserId();
        verify(articleService).unlike(eq(5L), eq(1L));
        verifyNoMoreInteractions(articleService);
    }

    @Test
    void 좋아요취소_인증없음_401() throws Exception {
        given(authTokenResolver.requireUserId()).willThrow(new UserService.UnauthorizedException());

        mockMvc.perform(delete("/articles/{id}/likes", 5L))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("unauthorized"));

        verify(authTokenResolver).requireUserId();
        verifyNoInteractions(articleService);
    }

    @Test
    void 좋아요_IllegalArgument_400() throws Exception {
        given(authTokenResolver.requireUserId()).willReturn(1L);
        given(articleService.like(anyLong(), anyLong()))
                .willThrow(new IllegalArgumentException("invalid_request"));

        mockMvc.perform(post("/articles/{id}/likes", 5L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("invalid_request"));

        verify(authTokenResolver).requireUserId();
        verify(articleService).like(eq(5L), eq(1L));
        verifyNoMoreInteractions(articleService);
    }

    @Test
    void 좋아요취소_IllegalArgument_400() throws Exception {
        given(authTokenResolver.requireUserId()).willReturn(1L);
        given(articleService.unlike(anyLong(), anyLong()))
                .willThrow(new IllegalArgumentException("invalid_request"));

        mockMvc.perform(delete("/articles/{id}/likes", 5L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("invalid_request"));

        verify(authTokenResolver).requireUserId();
        verify(articleService).unlike(eq(5L), eq(1L));
        verifyNoMoreInteractions(articleService);
    }

}
