package com.kdh.truedev.article.service;

import com.kdh.truedev.article.dto.request.ArticleReq;
import com.kdh.truedev.article.dto.response.ArticleDetailRes;
import com.kdh.truedev.article.dto.response.ArticlePageRes;
import org.springframework.web.multipart.MultipartFile;

public interface ArticleService {
    ArticlePageRes list(int page, int size);
    ArticlePageRes list(int page, int size,long userId);
    ArticleDetailRes create(Long userId, ArticleReq.CreateArticleReq req, MultipartFile profileImage);
    ArticleDetailRes detail(Long userId, Long articleId);
    ArticleDetailRes edit(Long articleId, Long userId, ArticleReq.EditArticleReq req,MultipartFile profileImage) throws ForbiddenException;
    boolean delete(Long articleId,Long userId);
    void increaseViewCount(Long articleId);
    //  좋아요/취소
    boolean like(Long articleId, Long userId);
    boolean unlike(Long articleId, Long userId);

    class ForbiddenException extends RuntimeException {}
}
