package com.trueDev.trueDev.comment.repository;

import com.trueDev.trueDev.comment.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentRepository extends JpaRepository<Comment,Long> {
    Integer countByArticleId(Long articleId);

    @Query(
            value = """
            select c
            from Comment c
            join fetch c.user u
            join c.article a
            where a.id = :articleId
              and c.commentIsDelete = false
              and a.isDeleted = false
            """,
            countQuery = """
            select count(c)
            from Comment c
            join c.article a
            where a.id = :articleId
              and c.commentIsDelete = false
              and a.isDeleted = false
            """
    )
    Page<Comment> findCommentsByArticleId(@Param("articleId") Long articleId, Pageable pageable);
}
