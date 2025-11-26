package com.kdh.truedev.comment.repository;

import com.kdh.truedev.comment.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.query.Param;


public interface CommentRepository extends JpaRepository<Comment,Long> {
    Integer countByArticleIdAndCommentIsDeleteFalse(Long articleId);

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

    @Query(
            value = """
        select c
        from Comment c
        join fetch c.user u
        join c.article a
        where c.commentIsDelete = false
          and a.isDeleted = false
          and u.id = :userId
        """,
            countQuery = """
        select count(c)
        from Comment c
        join c.user u
        join c.article a
        where c.commentIsDelete = false
          and a.isDeleted = false
          and u.id = :userId
        """
    )
    Page<Comment> findMyCommentsByArticleId(@Param("userId") Long userId, Pageable pageable);
}
