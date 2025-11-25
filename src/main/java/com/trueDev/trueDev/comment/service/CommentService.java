package com.trueDev.trueDev.comment.service;

import com.trueDev.trueDev.comment.dto.request.CommentReq;
import com.trueDev.trueDev.comment.dto.response.CommentPageRes;
import com.trueDev.trueDev.comment.dto.response.CommentRes;

public interface CommentService {

    CommentRes createComment(Long articleId, Long userId, CommentReq.CreateCommentReq req);

    CommentRes editComment(Long articleId, Long commentId, Long userId, CommentReq.EditCommentReq req);

    CommentPageRes getCommentList(Long articleId, Long userId, int page, int size);

    boolean deleteComment(Long articleId, Long commentId, Long userId);
}
