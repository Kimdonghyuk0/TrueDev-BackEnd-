package com.trueDev.trueDev.article.Likes.entity;


import com.trueDev.trueDev.article.entity.Article;
import com.trueDev.trueDev.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;




@Entity
@Table(
        uniqueConstraints = @UniqueConstraint(name="uk_likes_user_article", columnNames={"user_id","article_id"})
)
@Getter
@EqualsAndHashCode(of = "id")
@EntityListeners(AuditingEntityListener.class)
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class Likes {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

    @Column(name = "liked_at", nullable = false)
    @CreatedDate
    private LocalDateTime likedAt;



}
