package com.trueDev.trueDev.article.service;

import com.trueDev.trueDev.article.dto.request.ArticleReq;
import com.trueDev.trueDev.article.dto.response.ArticleDetailRes;
import com.trueDev.trueDev.article.dto.response.ArticlePageRes;

public interface ArticleService {
    ArticlePageRes list(int page, int size);
    ArticleDetailRes create(Long userId, ArticleReq.CreateArticleReq req);
    ArticleDetailRes detail(Long userId, Long articleId, boolean increaseViews);
    ArticleDetailRes edit(Long articleId, Long userId, ArticleReq.EditArticleReq req) throws ForbiddenException;
    boolean delete(Long articleId,Long userId);

    //  좋아요/취소
    boolean like(Long articleId, Long userId);
    boolean unlike(Long articleId, Long userId);

    class ForbiddenException extends RuntimeException {}
}
