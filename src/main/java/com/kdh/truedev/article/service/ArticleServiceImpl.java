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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.Objects;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

    private final EntityManager em;
    private final ArticleRepository articleRepo;
    private final LikesRepository likesRepo;
    private final UserRepository userRepo;
    private final CommentRepository commentRepo;
    private final UserService userService;
    @Value("${factchecker.url:http://localhost:8001/fact-check}")
    private String factCheckerUrl;
    private final RestTemplate restTemplate = new RestTemplate();
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

    @Transactional
    @Override
    public ArticleDetailRes detail(Long userId,Long articleId, boolean increaseViews) {
        Article article = articleRepo.findById(articleId).orElse(null);
        if (article == null) return null;
        if (increaseViews) article.increaseView();
        article.setLikeCount(likesRepo.countByArticleId(articleId));
        article.setCommentCount(commentRepo.countByArticleIdAndCommentIsDeleteFalse(articleId));
        boolean likedByMe = userId != null && likesRepo.existsByArticleIdAndUserId(articleId,userId); //내가 좋아요를 눌렀는지
        boolean isAuthor = userId != null && articleRepo.existsByIdAndUserId(articleId,userId); //내가 게시글을 작성했는지
        return ArticleMapper.toArticleDetail(article,likedByMe,isAuthor); //DTO 반환
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
    public ArticleDetailRes verify(Long articleId, Long userId) throws ForbiddenException {
        Article article = articleRepo.findById(articleId).orElse(null);
        if (article == null) return null;
        if (!Objects.equals(article.getUser().getId(), userId)) throw new ForbiddenException();

        String text = "제목: " + article.getTitle() + "\n내용: " + article.getContent();
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    factCheckerUrl,
                    Map.of("text", text),
                    Map.class
            );
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map body = response.getBody();
                Object data = body.containsKey("isFact") ? body : body.get("data");
                Map<?, ?> parsed = (Map<?, ?>) data;
                boolean isFact = Boolean.TRUE.equals(parsed.get("isFact"));
                String aiComment = parsed.get("aiComment") != null ? parsed.get("aiComment").toString() : "";
                article.setVerified(isFact);
                article.setAiMessage(aiComment);
                article.setCheck(true);
            }
        } catch (Exception e) {
            // 실패 시 isCheck는 false 그대로 두고 메시지도 유지
        }
        boolean likedByMe = userId != null && likesRepo.existsByArticleIdAndUserId(articleId,userId);
        boolean isAuthor = userId != null && articleRepo.existsByIdAndUserId(articleId,userId);
        return ArticleMapper.toArticleDetail(article, likedByMe, isAuthor);
    }
    
    @Transactional
    @Override
    public boolean delete(Long articleId,Long userId) {
        Article aRef = em.getReference(Article.class, articleId);
        if (aRef == null) return false;
        if (!Objects.equals(aRef.getUser().getId(), userId)) throw new ForbiddenException();
        aRef.softDelete();
        return true;
    }

    @Transactional
    @Override
    public boolean like(Long articleId,Long userId) {
        if (likesRepo.existsByArticleIdAndUserId(articleId, userId)) {
            return false;
        }
        // 없으면 저장 후 true
        Article aRef = em.getReference(Article.class, articleId);
        User uRef    = em.getReference(User.class, userId);
        likesRepo.save(Likes.builder().article(aRef).user(uRef).build());
        aRef.setLikeCount(likesRepo.countByArticleId(articleId));
        return true;
    }
    @Transactional
    @Override
    public boolean unlike(Long articleId,Long userId) {
        if (!likesRepo.existsByArticleIdAndUserId(articleId, userId)) {
            return false;
        }

        // 있으면 삭제 후 true
        likesRepo.deleteByArticleIdAndUserId(articleId, userId);
        Article aRef = em.getReference(Article.class, articleId);
        aRef.setLikeCount(likesRepo.countByArticleId(articleId));
        return true;
    }

    @Transactional(readOnly = true)
    @Override
    public com.kdh.truedev.article.dto.response.ArticleStatRes stats() {
        long total = articleRepo.countByIsDeletedFalse();
        long verified = articleRepo.countVerifiedComputed();
        long pending = articleRepo.countPendingComputed();
        long failed = articleRepo.countFailedComputed();
        return new com.kdh.truedev.article.dto.response.ArticleStatRes(verified, pending, failed, total);
    }
}
