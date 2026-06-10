package com.news.service;

import com.news.common.PageResult;
import com.news.dto.PostVO;

public interface FavoriteService {
    void addFavorite(Long userId, Long postId);
    void removeFavorite(Long userId, Long postId);
    PageResult<PostVO> getUserFavorites(Long userId, int page, int size);
    boolean isFavorited(Long userId, Long postId);
}
