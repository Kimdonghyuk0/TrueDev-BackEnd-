package com.kdh.truedev.article.repository;


import com.kdh.truedev.article.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface ArticleRepository extends JpaRepository<Article,Long> {
    @Query(
            value = """
            select a
            from Article a
            join fetch a.user
            where a.isDeleted = false
            """,
            countQuery = """
            select count(a)
            from Article a
            where a.isDeleted = false
            """
    )
    Page<Article> findAllByIsDeletedFalse(Pageable pageable);
    @Query(
            value = """
            select a
            from Article a
            join fetch a.user u
            where a.isDeleted = false
            and u.id = :userId
            """,
            countQuery = """
            select count(a)
            from Article a
            where a.isDeleted = false
            and a.user.id = :userId
            """
    )
    Page<Article> findMyArticleByIsDeletedFalse(@Param("userId") Long userId,
                                                Pageable pageable);
    boolean existsByIdAndUserId(Long ArticleId, Long UserId);

    @Modifying(clearAutomatically = true)
    @Query("update Article a set a.likeCount = a.likeCount + 1 where a.id = :id")
    int incrementLikeCount(@Param("id") Long id);

    @Modifying(clearAutomatically = true)
    @Query("update Article a set a.likeCount = a.likeCount - 1 where a.id = :id and a.likeCount > 0")
    int decrementLikeCount(@Param("id") Long id);

    @Modifying(clearAutomatically = true)
    @Query("update Article a set a.viewCount = a.viewCount + 1 where a.id = :id")
    int incrementViewCount(@Param("id") Long id);

    @Modifying(clearAutomatically = true)
    @Query("update Article a set a.commentCount = a.commentCount + 1 where a.id = :id")
    int incrementCommentCount(@Param("id") Long id);

    @Modifying(clearAutomatically = true)
    @Query("update Article a set a.commentCount = a.commentCount - 1 where a.id = :id")
    int decrementCommentCount(@Param("id") Long id);
}