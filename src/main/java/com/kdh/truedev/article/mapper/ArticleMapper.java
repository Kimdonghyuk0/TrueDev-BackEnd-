package com.kdh.truedev.article.mapper;

import com.kdh.truedev.article.dto.request.ArticleReq;
import com.kdh.truedev.article.dto.response.ArticleDetailRes;
import com.kdh.truedev.article.dto.response.ArticleSummaryRes;
import com.kdh.truedev.base.dto.response.AuthorRes;
import com.kdh.truedev.article.entity.Article;
import com.kdh.truedev.user.entity.User;

public class ArticleMapper {

    public static Article toEntity(User user, ArticleReq.CreateArticleReq req,String img_url) {
        return Article.builder()
                .user(user)
                .title(req.title())
                .content(req.content())
                .image(img_url)
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
                new AuthorRes(authorName, authorImg),
                a.getImage()
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
                a.getViewCount()+1,
                a.getCommentCount(),
                a.getArticleCreatedAt(),
                a.getArticleEditedAt(),
                new AuthorRes(authorName, authorImg),
                likedByMe,
                isAuthor,
                a.getImage()
        );
    }

}