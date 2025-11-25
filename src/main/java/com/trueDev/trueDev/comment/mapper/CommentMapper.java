package com.trueDev.trueDev.comment.mapper;

import com.trueDev.trueDev.article.entity.Article;
import com.trueDev.trueDev.base.dto.response.AuthorRes;
import com.trueDev.trueDev.comment.dto.request.CommentReq;
import com.trueDev.trueDev.comment.dto.response.CommentRes;
import com.trueDev.trueDev.comment.entity.Comment;
import com.trueDev.trueDev.user.entity.User;

public class CommentMapper {
    public static Comment toEntity(Article article, User user, CommentReq.CreateCommentReq req) {
        return Comment.builder()
                .article(article)
                .user(user)
                .content(req.content())
                .build();
    }


    public static CommentRes toRes(Comment c,Long userId){
        String userName = (c.getUser() != null) ? c.getUser().getName() : null;
        String userImg  = (c.getUser() != null) ? c.getUser().getProfileImage() : null;
        boolean isAuthor = c.getUser() != null
                && c.getUser().getId().equals(userId);
        return new CommentRes(
                c.getId(),
                c.getArticle() != null ? c.getArticle().getId() : null,
                c.getContent(),
                c.getCommentCreatedAt(),
                c.getCommentEditedAt(),
                new AuthorRes(userName,userImg),
                isAuthor //현재 유저가 작성한 글인지
        );
    }
}
