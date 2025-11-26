package com.kdh.truedev.article.repository;


import com.kdh.truedev.article.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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

    long countByIsDeletedFalse();

    @Query("select count(a) from Article a where a.isDeleted = false and (a.isVerified = true or (a.isCheck = true and (a.aiMessage is null or trim(a.aiMessage) = '')))")
    long countVerifiedComputed();

    @Query("select count(a) from Article a where a.isDeleted = false and a.isCheck = false")
    long countPendingComputed();

    @Query("select count(a) from Article a where a.isDeleted = false and a.isCheck = true and a.isVerified = false and (a.aiMessage is not null and trim(a.aiMessage) <> '')")
    long countFailedComputed();
}
