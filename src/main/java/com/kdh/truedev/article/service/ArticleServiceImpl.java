package com.kdh.truedev.article.service;


import com.kdh.truedev.article.dto.response.ArticleDetailRes;
import com.kdh.truedev.article.dto.response.ArticlePageRes;
import com.kdh.truedev.article.dto.response.ArticleSummaryRes;
import com.kdh.truedev.article.mapper.ArticleMapper;
import com.kdh.truedev.article.Likes.entity.Likes;
import com.kdh.truedev.article.Likes.repository.LikesRepository;
import com.kdh.truedev.article.dto.request.ArticleReq;
import com.kdh.truedev.article.entity.Article;
import com.kdh.truedev.article.repository.ArticleRepository;
import com.kdh.truedev.comment.repository.CommentRepository;
import com.kdh.truedev.user.entity.User;
import com.kdh.truedev.user.repository.UserRepository;
import com.kdh.truedev.user.service.UserService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;


@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

    private final ArticleRepository articleRepo;
    private final LikesRepository likesRepo;
    private final UserRepository userRepo;
    private final CommentRepository commentRepo;
    private final UserService userService;
    @Transactional(readOnly = true)
    @Override
    public ArticlePageRes list(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "articleCreatedAt"));
        Page<Article> result = articleRepo.findAllByIsDeletedFalse(pageable);
        List<ArticleSummaryRes> articleSummaryRes = result.getContent().stream()
                .map(ArticleMapper::toSummary)   // Article -> ArticleSummaryRes
                .toList();
        return new ArticlePageRes(
                articleSummaryRes,
                page,
                size,
                result.getTotalPages(),
                result.getTotalElements(),
                result.hasNext(),
                result.hasPrevious()
        );
    }

    @Transactional(readOnly = true)
    @Override
    public ArticlePageRes list(int page, int size, long userId) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "articleCreatedAt"));
        Page<Article> result = articleRepo.findMyArticleByIsDeletedFalse(userId,pageable);
        List<ArticleSummaryRes> articleSummaryRes = result.getContent().stream()
                .map(ArticleMapper::toSummary)   // Article -> ArticleSummaryRes
                .toList();
        return new ArticlePageRes(
                articleSummaryRes,
                page,
                size,
                result.getTotalPages(),
                result.getTotalElements(),
                result.hasNext(),
                result.hasPrevious()
        );
    }

    @Transactional
    @Override
    public ArticleDetailRes create(Long userId, ArticleReq.CreateArticleReq req, MultipartFile profileImage) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        String img_url = null;
        if(profileImage != null && !profileImage.isEmpty()){
            img_url = userService.uploadImage(profileImage);
        }
        Article article = articleRepo.save(ArticleMapper.toEntity(user, req,img_url));
        return ArticleMapper.toArticleDetail(article,false,true);
    }

    @Transactional(readOnly = true)
    @Override
    public ArticleDetailRes detail(Long userId,Long articleId ) {
        Article article = articleRepo.findById(articleId)
                .orElseThrow(() -> new IllegalArgumentException("not_found_article"));
        boolean likedByMe = userId != null && likesRepo.existsByArticleIdAndUserId(articleId,userId); //내가 좋아요를 눌렀는지
        boolean isAuthor = userId != null && articleRepo.existsByIdAndUserId(articleId,userId); //내가 게시글을 작성했는지

        //조회수의 경우 Mapper에서 +1 된 상태로 변환되도록 함(DB조회수 증가 로직은 비동기로 처리될 예정이므로 detail매소드는 읽기만)
        return ArticleMapper.toArticleDetail(article,likedByMe,isAuthor); //DTO 반환
    }

    @Transactional
    @Async
    @Override
    public void increaseViewCount(Long articleId) {
        try {
            int updated = articleRepo.incrementViewCount(articleId);
            if (updated == 0) {
                log.warn("increaseViewCount failed: article not found. articleId={}", articleId);
            }
        } catch (Exception e) {
            log.error("Failed to increase view count. articleId={}", articleId, e);
        }
    }

    @Transactional
    @Override
    public ArticleDetailRes edit(Long articleId, Long userId, ArticleReq.EditArticleReq req,MultipartFile profileImage) throws ForbiddenException {
        Article article = articleRepo.findById(articleId).orElse(null);
        if (article == null) return null;
        //본인이 작성한 글이 맞는지 확인
        if (!Objects.equals(article.getUser().getId(), userId)) throw new ForbiddenException();
        if (req.title() != null) article.setTitle(req.title());
        if (req.content() != null) article.setContent(req.content());
        // 이미지 수정
        if(profileImage != null && !profileImage.isEmpty())article.setImage(userService.uploadImage(profileImage));
        else article.setImage("");
        boolean likedByMe = likesRepo.existsByArticleIdAndUserId(articleId,userId); //내가 좋아요를 눌렀는지
        boolean isAuthor = userId != null && articleRepo.existsByIdAndUserId(articleId,userId); //내가 게시글을 작성했는지
        return ArticleMapper.toArticleDetail(article,likedByMe,isAuthor) ;
    }
    
    @Transactional
    @Override
    public boolean delete(Long articleId,Long userId) {
         return articleRepo.findById(userId)
                 .map(article -> {
                     if(!Objects.equals(userId,article.getUser().getId())){
                         throw new ForbiddenException();
                     }
                     article.softDelete();
                     return true;

                 })
                 .orElse(false) ;
    }

    @Transactional
    @Override
    public boolean like(Long articleId,Long userId) {
        // 중복 체크는 유니크 제약 예외로 처리
        Article article = articleRepo.findById(articleId)
                .orElseThrow(() -> new IllegalArgumentException("not_found_article"));
        if (Boolean.TRUE.equals(article.getIsDeleted())) {
            throw new IllegalArgumentException("not_found_article");
        }
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("not_found_user"));

        try {
            likesRepo.save(Likes.builder().article(article).user(user).build());
            // 1 증가 -> 원자적 UPDATE
            int updated = articleRepo.incrementLikeCount(articleId); //영속성 컨텍스트에 있는 article객체는 detach됨
            if (updated == 0) throw new IllegalArgumentException("not_found_article");
            return true;
        }catch (DataIntegrityViolationException e){ //이미 좋아요를 눌렀으면 유니크제약에 걸림
            return false;
        }
    }

    @Transactional
    @Override
    public boolean unlike(Long articleId,Long userId) {
        if (!likesRepo.existsByArticleIdAndUserId(articleId, userId)) { // 좋아요를 안누른 상태
            return false;
        }
        Article article = articleRepo.findById(articleId)
                .orElseThrow(() -> new IllegalArgumentException("not_found_article"));
        if (Boolean.TRUE.equals(article.getIsDeleted())) { //삭제됐는지
            throw new IllegalArgumentException("not_found_article");
        }
        userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("not_found_user"));

        likesRepo.deleteByArticleIdAndUserId(articleId,userId);
        // 1 감소 -> 원자적 UPDATE
        int updated = articleRepo.decrementLikeCount(articleId);
        if (updated == 0) throw new IllegalArgumentException("not_found_article");
        return true;
    }
}
