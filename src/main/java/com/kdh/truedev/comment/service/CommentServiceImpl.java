package com.kdh.truedev.comment.service;

import com.kdh.truedev.article.entity.Article;
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

    @Transactional
    @Override
    public CommentRes createComment(Long articleId, Long userId, CommentReq.CreateCommentReq req) {
        Article articleRef = em.getReference(Article.class, articleId);
        User userRef = em.getReference(User.class, userId);
        Comment c = commentRepo.save(CommentMapper.toEntity(articleRef, userRef, req));
        articleRef.setCommentCount(commentRepo.countByArticleIdAndCommentIsDeleteFalse(articleId));
        return CommentMapper.toRes(c,userId);
    }
    @Transactional
    @Override
    public CommentRes editComment(Long articleId, Long commentId, Long userId, CommentReq.EditCommentReq req) {
        Comment c = commentRepo.findById(commentId).orElse(null);
        if (c == null || !c.getArticle().getId().equals(articleId)) return null;
        if (!c.getUser().getId().equals(userId)) return null;

        c.edit(req.content()); // 더티체킹으로 UPDATE
        return CommentMapper.toRes(c,userId);
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
        Comment c = commentRepo.findById(commentId).orElse(null);
        if (c == null || !c.getArticle().getId().equals(articleId)) return false;
        if (!c.getUser().getId().equals(userId)) return false;
        Article articleRef = em.getReference(Article.class, articleId);
        c.softDelete(); // 소프트 삭제
        articleRef.setCommentCount(commentRepo.countByArticleIdAndCommentIsDeleteFalse(articleId));
        return true;
    }
}
