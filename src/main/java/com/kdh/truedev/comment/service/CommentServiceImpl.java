package com.kdh.truedev.comment.service;

import com.kdh.truedev.article.entity.Article;
import com.kdh.truedev.article.repository.ArticleRepository;
import com.kdh.truedev.comment.dto.response.CommentPageRes;
import com.kdh.truedev.comment.mapper.CommentMapper;
import com.kdh.truedev.comment.dto.request.CommentReq;
import com.kdh.truedev.comment.dto.response.CommentRes;
import com.kdh.truedev.comment.entity.Comment;
import com.kdh.truedev.comment.repository.CommentRepository;
import com.kdh.truedev.user.entity.User;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor

public class CommentServiceImpl implements CommentService {
    private final EntityManager em;
    private final CommentRepository commentRepo;
    private final ArticleRepository articleRepository;

    @Transactional
    @Override
    public CommentRes createComment(Long articleId, Long userId, CommentReq.CreateCommentReq req) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new IllegalArgumentException("not_found_article"));
        if (Boolean.TRUE.equals(article.getIsDeleted())) {
            throw new IllegalArgumentException("already_deleted_comment");
        };
        User userRef = em.getReference(User.class, userId); //userId는 시큐리티 컨텍스트에서 가져왔기에 검증된 값으로 봄
        Comment comment = commentRepo.save(CommentMapper.toEntity(article, userRef, req));
        int updated = articleRepository.incrementCommentCount(articleId); //영속성 컨텍스트에 있는 article객체는 detach됨
        if (updated == 0) throw new IllegalArgumentException("not_found_article");
        return CommentMapper.toRes(comment,userId);
    }

    @Transactional
    @Override
    public CommentRes editComment(Long articleId, Long commentId, Long userId, CommentReq.EditCommentReq req) {
        Comment comment = commentRepo.findById(commentId).orElseThrow(() -> new IllegalArgumentException("not_found_comment"));
        if (Boolean.TRUE.equals(comment.getCommentIsDelete())) {
            throw new IllegalArgumentException("already_deleted_comment");
        }
        if ( !comment.getArticle().getId().equals(articleId)) return null;
        if (!comment.getUser().getId().equals(userId)) return null;

        comment.edit(req.content()); // 더티체킹으로 UPDATE
        return CommentMapper.toRes(comment,userId);
    }

    @Transactional(readOnly = true)
    @Override
    public CommentPageRes getCommentList(Long articleId, Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "commentCreatedAt"));
        Page<Comment> result = commentRepo.findCommentsByArticleId(articleId,pageable);
        List<CommentRes> comments = result.getContent().stream().map(c->CommentMapper.toRes(c,userId)).toList();

        return new CommentPageRes(
                comments,
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
    public CommentPageRes getCommentList(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "commentCreatedAt"));
        Page<Comment> result = commentRepo.findMyCommentsByArticleId(userId,pageable);
        List<CommentRes> comments = result.getContent().stream().map(c->CommentMapper.toRes(c,userId)).toList();

        return new CommentPageRes(
                comments,
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
    public boolean deleteComment(Long articleId, Long commentId, Long userId) {
        Comment comment = commentRepo.findById(commentId).orElseThrow(() -> new IllegalArgumentException("not_found_comment"));
        if (Boolean.TRUE.equals(comment.getCommentIsDelete())) {
            throw new IllegalArgumentException("already_deleted_comment");
        }
        if ( !comment.getArticle().getId().equals(articleId)) return false;
        if (!comment.getUser().getId().equals(userId)) return false;
        int deleted = commentRepo.softDeleteIfNotDeleted(commentId);
        if (deleted == 0) {
            // 이미 누가 먼저 삭제함
            return false;
        }
        // 여기까지 왔다는 건 이번 요청이 진짜 처음 삭제한 요청이라는 뜻
        int updated = articleRepository.decrementCommentCount(articleId);
        if (updated == 0) throw new IllegalArgumentException("not_found_article");

        return true;
    }
}
