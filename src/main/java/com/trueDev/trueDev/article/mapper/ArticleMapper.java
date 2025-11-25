package com.trueDev.trueDev.article.mapper;

import com.trueDev.trueDev.article.dto.request.ArticleReq;
import com.trueDev.trueDev.article.dto.response.ArticleDetailRes;
import com.trueDev.trueDev.article.dto.response.ArticleSummaryRes;
import com.trueDev.trueDev.base.dto.response.AuthorRes;
import com.trueDev.trueDev.article.entity.Article;
import com.trueDev.trueDev.user.entity.User;

public class ArticleMapper {

    public static Article toEntity(User user, ArticleReq.CreateArticleReq req) {
        return Article.builder()
                .user(user)
                .title(req.title())
                .content(req.content())
                .build();
    }


    public static ArticleSummaryRes toSummary(Article a) {
        String authorName = a.getUser() != null ? a.getUser().getName() : "";
        String authorImg  = a.getUser() != null && a.getUser().getProfileImage() != null
                ? a.getUser().getProfileImage() : "";
        return new ArticleSummaryRes(
                a.getId(),
                a.getTitle(),
                a.getLikeCount(),
                a.getViewCount(),
                a.getCommentCount(),
                a.getArticleCreatedAt(),
                a.getArticleEditedAt(),
                new AuthorRes(authorName, authorImg)
        );
    }

    public static ArticleDetailRes toArticleDetail(Article a,boolean likedByMe,boolean isAuthor) {
        String authorName = a.getUser() != null ? a.getUser().getName() : null;
        String authorImg  = a.getUser() != null ? a.getUser().getProfileImage() : null;
        return new ArticleDetailRes(
                a.getId(),
                a.getTitle(),
                a.getContent(),
                a.getLikeCount(),
                a.getViewCount(),
                a.getCommentCount(),
                a.getArticleCreatedAt(),
                a.getArticleEditedAt(),
                new AuthorRes(authorName, authorImg),
                likedByMe,
                isAuthor
        );
    }

}