package com.news.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.news.dto.PostLikeNotificationVO;
import com.news.entity.Post;
import com.news.entity.PostLike;
import com.news.entity.User;
import com.news.mapper.PostLikeMapper;
import com.news.mapper.PostMapper;
import com.news.mapper.UserMapper;
import com.news.service.PostLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostLikeServiceImpl implements PostLikeService {

    private final PostLikeMapper postLikeMapper;
    private final PostMapper postMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public boolean toggleLike(Long userId, Long postId) {
        LambdaQueryWrapper<PostLike> w = new LambdaQueryWrapper<>();
        w.eq(PostLike::getUserId, userId).eq(PostLike::getPostId, postId);
        PostLike existing = postLikeMapper.selectOne(w);
        if (existing != null) {
            postLikeMapper.deleteById(existing.getId());
            return false;
        } else {
            PostLike like = new PostLike();
            like.setUserId(userId); like.setPostId(postId);
            postLikeMapper.insert(like);
            return true;
        }
    }

    @Override
    public int getLikeCount(Long postId) {
        LambdaQueryWrapper<PostLike> w = new LambdaQueryWrapper<>();
        w.eq(PostLike::getPostId, postId);
        return postLikeMapper.selectCount(w).intValue();
    }

    @Override
    public boolean isLiked(Long userId, Long postId) {
        if (userId == null) return false;
        LambdaQueryWrapper<PostLike> w = new LambdaQueryWrapper<>();
        w.eq(PostLike::getUserId, userId).eq(PostLike::getPostId, postId);
        return postLikeMapper.selectCount(w) > 0;
    }

    @Override
    public List<PostLikeNotificationVO> getNotifications(Long userId) {
        // Get all posts by this user, then find who liked them
        LambdaQueryWrapper<Post> postW = new LambdaQueryWrapper<>();
        postW.eq(Post::getUserId, userId);
        List<Long> postIds = postMapper.selectList(postW).stream().map(Post::getId).collect(Collectors.toList());
        if (postIds.isEmpty()) return List.of();

        LambdaQueryWrapper<PostLike> likeW = new LambdaQueryWrapper<>();
        likeW.in(PostLike::getPostId, postIds).orderByDesc(PostLike::getCreateTime);
        return postLikeMapper.selectList(likeW).stream().map(like -> {
            PostLikeNotificationVO vo = new PostLikeNotificationVO();
            vo.setId(like.getId()); vo.setPostId(like.getPostId()); vo.setFromUserId(like.getUserId());
            vo.setCreateTime(like.getCreateTime());
            Post p = postMapper.selectById(like.getPostId());
            if (p != null) vo.setPostTitle(p.getTitle());
            User u = userMapper.selectById(like.getUserId());
            if (u != null) { vo.setFromUserName(u.getNickname() != null ? u.getNickname() : u.getUsername()); vo.setFromUserAvatar(u.getAvatarUrl()); }
            return vo;
        }).collect(Collectors.toList());
    }
}
