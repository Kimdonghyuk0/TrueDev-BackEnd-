package com.kdh.truedev.article.service;

import com.kdh.truedev.article.entity.Article;
import com.kdh.truedev.article.repository.ArticleRepository;
import com.kdh.truedev.article.Likes.repository.LikesRepository;
import com.kdh.truedev.comment.repository.CommentRepository;
import com.kdh.truedev.user.entity.User;
import com.kdh.truedev.user.repository.UserRepository;
import com.kdh.truedev.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {

    @Mock EntityManager em;
    @Mock ArticleRepository articleRepo;
    @Mock LikesRepository likesRepo;
    @Mock UserRepository userRepo;
    @Mock CommentRepository commentRepo;
    @Mock UserService userService;

    @InjectMocks ArticleServiceImpl articleService;

    Article baseArticle;
    User baseUser;

    @BeforeEach
    void setUp() {
        baseUser = User.builder().id(1L).name("tester").build();
        baseArticle = Article.builder()
                .id(10L)
                .title("title")
                .content("content")
                .user(baseUser)
                .likeCount(0)
                .viewCount(0)
                .commentCount(0)
                .articleCreatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void 리스트_조회_페이징정보_반영() {
        int page = 2;
        int size = 5;
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "articleCreatedAt"));
        Article a1 = Article.builder()
                .id(1L)
                .title("title1")
                .content("content1")
                .articleCreatedAt(LocalDateTime.now())
                .user(User.builder().name("user1").profileImage("img1").build())
                .build();
        Article a2 = Article.builder()
                .id(2L)
                .title("title2")
                .content("content2")
                .articleCreatedAt(LocalDateTime.now())
                .user(User.builder().name("user2").profileImage("img2").build())
                .build();
        Page<Article> pageResult = new PageImpl<>(List.of(a1, a2), pageable, 12);
        when(articleRepo.findAllByIsDeletedFalse(pageable)).thenReturn(pageResult);

        var res = articleService.list(page, size);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(articleRepo).findAllByIsDeletedFalse(captor.capture());
        assertThat(captor.getValue().getPageNumber()).isEqualTo(1);
        assertThat(captor.getValue().getPageSize()).isEqualTo(size);

        assertThat(res.page()).isEqualTo(page);
        assertThat(res.size()).isEqualTo(size);
        assertThat(res.totalPages()).isEqualTo(pageResult.getTotalPages());
        assertThat(res.totalArticles()).isEqualTo(pageResult.getTotalElements());
        assertThat(res.hasNext()).isTrue();
        assertThat(res.hasPrev()).isTrue();
        assertThat(res.articles()).hasSize(2);
        assertThat(res.articles().get(0).title()).isEqualTo("title1");
        assertThat(res.articles().get(1).author().userName()).isEqualTo("user2");
    }

    @Test
    void 내가쓴글_조회_페이징정보_반영() {
        int page = 1;
        int size = 3;
        long userId = 99L;
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "articleCreatedAt"));
        Article a1 = Article.builder()
                .id(10L)
                .title("my title")
                .content("my content")
                .articleCreatedAt(LocalDateTime.now())
                .user(User.builder().id(userId).name("me").build())
                .build();
        Page<Article> pageResult = new PageImpl<>(List.of(a1), pageable, 1);
        when(articleRepo.findMyArticleByIsDeletedFalse(userId, pageable)).thenReturn(pageResult);

        var res = articleService.list(page, size, userId);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(articleRepo).findMyArticleByIsDeletedFalse(org.mockito.Mockito.eq(userId), captor.capture());
        assertThat(captor.getValue().getPageNumber()).isEqualTo(0);
        assertThat(captor.getValue().getPageSize()).isEqualTo(size);

        assertThat(res.page()).isEqualTo(page);
        assertThat(res.size()).isEqualTo(size);
        assertThat(res.totalPages()).isEqualTo(1);
        assertThat(res.totalArticles()).isEqualTo(1);
        assertThat(res.hasNext()).isFalse();
        assertThat(res.hasPrev()).isFalse();
        assertThat(res.articles()).hasSize(1);
      //  assertThat(res.articles().get(0).id()).isEqualTo(10L);
        assertThat(res.articles().get(0).author().userName()).isEqualTo("me");
    }

    @Test
    void 좋아요_성공시_카운트증가() {
        when(articleRepo.findById(10L)).thenReturn(java.util.Optional.of(baseArticle));
        when(userRepo.findById(1L)).thenReturn(java.util.Optional.of(baseUser));
        when(articleRepo.incrementLikeCount(10L)).thenReturn(1);

        boolean result = articleService.like(10L, 1L);

        assertThat(result).isTrue();
        verify(articleRepo).findById(10L);
        verify(userRepo).findById(1L);
        verify(likesRepo).save(org.mockito.ArgumentMatchers.any());
        verify(articleRepo).incrementLikeCount(10L);
    }

    @Test
    void 좋아요_중복이면_false() {
        when(articleRepo.findById(10L)).thenReturn(java.util.Optional.of(baseArticle));
        when(userRepo.findById(1L)).thenReturn(java.util.Optional.of(baseUser));
        // 중복 저장 시 예외
        org.mockito.Mockito.doThrow(new org.springframework.dao.DataIntegrityViolationException("dup"))
                .when(likesRepo).save(org.mockito.ArgumentMatchers.any());

        boolean result = articleService.like(10L, 1L);

        assertThat(result).isFalse();
        verify(articleRepo).findById(10L);
        verify(userRepo).findById(1L);
        verify(likesRepo).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void 좋아요취소_성공시_카운트감소() {
        when(likesRepo.existsByArticleIdAndUserId(10L, 1L)).thenReturn(true);
        when(articleRepo.findById(10L)).thenReturn(java.util.Optional.of(baseArticle));
        when(userRepo.findById(1L)).thenReturn(java.util.Optional.of(baseUser));
        when(articleRepo.decrementLikeCount(10L)).thenReturn(1);

        boolean result = articleService.unlike(10L, 1L);

        assertThat(result).isTrue();
        verify(likesRepo).existsByArticleIdAndUserId(10L, 1L);
        verify(likesRepo).deleteByArticleIdAndUserId(10L, 1L);
        verify(articleRepo).decrementLikeCount(10L);
    }

    @Test
    void 좋아요취소_안눌렀으면_false() {
        when(likesRepo.existsByArticleIdAndUserId(10L, 1L)).thenReturn(false);

        boolean result = articleService.unlike(10L, 1L);

        assertThat(result).isFalse();
        verify(likesRepo).existsByArticleIdAndUserId(10L, 1L);
        verifyNoInteractions(articleRepo);
        verifyNoInteractions(userRepo);
    }
}
