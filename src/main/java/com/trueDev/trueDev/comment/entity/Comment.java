package com.trueDev.trueDev.comment.entity;


import com.trueDev.trueDev.article.entity.Article;
import com.trueDev.trueDev.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;


@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@EqualsAndHashCode(of = "id")
@Entity
@EntityListeners(AuditingEntityListener.class)

public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 500)
    private String content;

    @CreatedDate
    @Column(nullable = false)
    private LocalDateTime commentCreatedAt;

    @LastModifiedDate
    @Column
    private LocalDateTime commentEditedAt;

    @Builder.Default
    @Column
    private Boolean commentIsDelete = false;

    public void edit(String newContent) { this.content = newContent; }
    public void softDelete() { this.commentIsDelete = true; }
}
