package com.kdh.truedev.user.controller;


import com.kdh.truedev.article.controller.ArticleController;
import com.kdh.truedev.article.dto.response.ArticleDetailRes;
import com.kdh.truedev.article.dto.response.ArticlePageRes;
import com.kdh.truedev.article.service.ArticleService;
import com.kdh.truedev.base.dto.response.AuthorRes;
import com.kdh.truedev.exception.ApiErrorHandler;
import com.kdh.truedev.user.support.AuthTokenResolver;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
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

import static org.assertj.core.api.BDDAssertions.and;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
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
      mockMvc.perform(get("/myArticles").param("page","0"))
                .andExpectAll(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("invalid_request"));
        given(authTokenResolver.requireUserId()).willReturn(1L);
        verify(authTokenResolver).requireUserId();
        verifyNoMoreInteractions(articleService); //서비스 호출 없어야 함
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
    void 글작성_201() throws Exception{
        // given
        MockMultipartFile articlePart = new MockMultipartFile(
                "article", "", "application/json",
                """
                {"title":"t","content":"c"}
                """.getBytes()
        );
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
        mockMvc.perform(multipart("/articles").file(articlePart).file(imagePart))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("post_created_success"))
                .andExpect(jsonPath("$.data.title").value("t"));

        verify(articleService).create(eq(1L), any(), any());
        verifyNoMoreInteractions(articleService);
    }



}
