package com.kdh.truedev.article.Likes.repository;

import com.kdh.truedev.article.Likes.entity.Likes;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikesRepository extends JpaRepository<Likes,Long> {
    boolean existsByArticleIdAndUserId(Long articleId, Long userId);
    int deleteByArticleIdAndUserId(Long articleId, Long userId);
    Integer countByArticleId(Long articleId);
}
