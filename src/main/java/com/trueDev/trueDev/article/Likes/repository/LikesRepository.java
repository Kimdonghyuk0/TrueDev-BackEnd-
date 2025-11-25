package com.trueDev.trueDev.article.Likes.repository;

import com.trueDev.trueDev.article.Likes.entity.Likes;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikesRepository extends JpaRepository<Likes,Long> {
    boolean existsByArticleIdAndUserId(Long articleId, Long userId);
    void deleteByArticleIdAndUserId(Long articleId, Long userId);
    Integer countByArticleId(Long articleId);
}
