package com.news.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.news.common.PageResult;
import com.news.dto.PostVO;
import com.news.entity.Favorite;
import com.news.entity.Post;
import com.news.mapper.FavoriteMapper;
import com.news.mapper.PostMapper;
import com.news.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteMapper favoriteMapper;
    private final PostMapper postMapper;

    @Override
    @Transactional
    public void addFavorite(Long userId, Long postId) {
        Post post = postMapper.selectById(postId);
        if (post == null || !"approved".equals(post.getStatus())) {
            throw new RuntimeException("Post not found or not approved");
        }

        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId)
               .eq(Favorite::getPostId, postId);
        if (favoriteMapper.selectCount(wrapper) > 0) {
            return;
        }

        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setPostId(postId);
        favoriteMapper.insert(favorite);
    }

    @Override
    @Transactional
    public void removeFavorite(Long userId, Long postId) {
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId)
               .eq(Favorite::getPostId, postId);
        favoriteMapper.delete(wrapper);
    }

    @Override
    public PageResult<PostVO> getUserFavorites(Long userId, int page, int size) {
        LambdaQueryWrapper<Favorite> favWrapper = new LambdaQueryWrapper<>();
        favWrapper.eq(Favorite::getUserId, userId)
                  .orderByDesc(Favorite::getCreateTime);

        Page<Favorite> favPage = new Page<>(page, size);
        favoriteMapper.selectPage(favPage, favWrapper);

        List<PostVO> vos = favPage.getRecords().stream()
                .map(fav -> {
                    Post post = postMapper.selectById(fav.getPostId());
                    if (post == null) return null;
                    PostVO vo = new PostVO();
                    vo.setId(post.getId());
                    vo.setTitle(post.getTitle());
                    vo.setContent(post.getContent());
                    vo.setImageUrls(post.getImageUrls());
                    vo.setCategoryId(post.getCategoryId());
                    vo.setUserId(post.getUserId());
                    vo.setStatus(post.getStatus());
                    vo.setIsRecommended(post.getIsRecommended());
                    vo.setCreateTime(post.getCreateTime());
                    vo.setUpdateTime(post.getUpdateTime());
                    vo.setIsFavorited(true);
                    return vo;
                })
                .filter(vo -> vo != null)
                .collect(Collectors.toList());

        return PageResult.of(favPage.getTotal(), page, size, vos);
    }

    @Override
    public boolean isFavorited(Long userId, Long postId) {
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId)
               .eq(Favorite::getPostId, postId);
        return favoriteMapper.selectCount(wrapper) > 0;
    }
}
