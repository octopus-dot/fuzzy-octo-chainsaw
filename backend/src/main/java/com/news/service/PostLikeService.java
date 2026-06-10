package com.news.service;

import com.news.dto.PostLikeNotificationVO;
import java.util.List;

public interface PostLikeService {
    boolean toggleLike(Long userId, Long postId);
    int getLikeCount(Long postId);
    boolean isLiked(Long userId, Long postId);
    List<PostLikeNotificationVO> getNotifications(Long userId);
}
