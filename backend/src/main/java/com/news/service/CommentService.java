package com.news.service;

import com.news.common.PageResult;
import com.news.dto.CommentVO;

public interface CommentService {
    PageResult<CommentVO> getCommentsByPost(Long postId, int page, int size, Long currentUserId);
    CommentVO createComment(Long userId, Long postId, String content);
    void deleteComment(Long userId, Long commentId);
    boolean toggleLike(Long userId, Long commentId);
    PageResult<CommentVO> getUserComments(Long userId, int page, int size);
}
