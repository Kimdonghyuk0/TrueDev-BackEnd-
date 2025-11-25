package com.trueDev.trueDev.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor @NoArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Builder
@EqualsAndHashCode(of = "id")

public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, length = 60)
    private String password;

    @Column(nullable = false, columnDefinition = "VARBINARY(255)")
    private String name;

    @Column
    private String profileImage;

    @Column
    @Builder.Default
    private Boolean isDelete = false;

    @Column(name = "user_created_at")
    @CreatedDate
    private LocalDateTime UserCreatedAt;

    @LastModifiedDate
    @Column(name = "user_edited_at")
    private LocalDateTime UserEditedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Authority authority = Authority.ROLE_USER;  // 기본값 유저로 설정

    public void softDelete(){this.isDelete = true;}

    public void setName(String name) {
        this.name = name;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public void setEmail(String email){
        this.email  = email;
    }

    public void setPassword(String password){
        this.password = password;
    }

    public Authority getAuthority() { return authority; }

}
