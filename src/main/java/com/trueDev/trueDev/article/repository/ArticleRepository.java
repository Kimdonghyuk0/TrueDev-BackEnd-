package com.trueDev.trueDev.article.repository;


import com.trueDev.trueDev.article.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


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
    boolean existsByIdAndUserId(Long ArticleId, Long UserId);
}