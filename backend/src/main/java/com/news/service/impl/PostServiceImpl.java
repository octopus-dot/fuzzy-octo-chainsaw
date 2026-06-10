package com.news.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.news.common.PageResult;
import com.news.dto.PostVO;
import com.news.entity.Category;
import com.news.entity.Favorite;
import com.news.entity.Post;
import com.news.entity.User;
import com.news.mapper.CategoryMapper;
import com.news.mapper.FavoriteMapper;
import com.news.mapper.PostMapper;
import com.news.mapper.UserMapper;
import com.news.service.FileStorageService;
import com.news.service.PostLikeService;
import com.news.service.PostService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostMapper postMapper;
    private final UserMapper userMapper;
    private final CategoryMapper categoryMapper;
    private final FavoriteMapper favoriteMapper;
    private final FileStorageService fileStorageService;
    private final ObjectMapper objectMapper;
    private final PostLikeService postLikeService;

    @Override
    @Transactional
    public Post upload(Long userId, String title, String content, Long categoryId, List<MultipartFile> images) {
        Category category = categoryMapper.selectById(categoryId);
        if (category == null) {
            throw new RuntimeException("Category not found");
        }

        Post post = new Post();
        post.setTitle(title);
        post.setContent(content);
        post.setCategoryId(categoryId);
        post.setUserId(userId);
        post.setStatus("pending");
        post.setIsRecommended(false);

        if (images != null && !images.isEmpty()) {
            List<String> imageUrls = fileStorageService.storeFiles(images);
            try {
                post.setImageUrls(objectMapper.writeValueAsString(imageUrls));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to process image URLs");
            }
        }

        postMapper.insert(post);
        return post;
    }

    @Override
    public PageResult<PostVO> getUserPosts(Long userId, int page, int size) {
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getUserId, userId)
               .orderByDesc(Post::getCreateTime);

        Page<Post> postPage = new Page<>(page, size);
        postMapper.selectPage(postPage, wrapper);

        List<PostVO> vos = postPage.getRecords().stream()
                .map(p -> toVO(p, userId))
                .collect(Collectors.toList());

        return PageResult.of(postPage.getTotal(), page, size, vos);
    }

    @Override
    public PageResult<PostVO> getRecommended(int page, int size) {
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getIsRecommended, true)
               .eq(Post::getStatus, "approved")
               .orderByDesc(Post::getUpdateTime);

        Page<Post> postPage = new Page<>(page, size);
        postMapper.selectPage(postPage, wrapper);

        List<PostVO> vos = postPage.getRecords().stream()
                .map(p -> toVO(p, null))
                .collect(Collectors.toList());

        return PageResult.of(postPage.getTotal(), page, size, vos);
    }

    @Override
    public PageResult<PostVO> getByCategory(Long categoryId, int page, int size) {
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getCategoryId, categoryId)
               .eq(Post::getStatus, "approved")
               .orderByDesc(Post::getCreateTime);

        Page<Post> postPage = new Page<>(page, size);
        postMapper.selectPage(postPage, wrapper);

        List<PostVO> vos = postPage.getRecords().stream()
                .map(p -> toVO(p, null))
                .collect(Collectors.toList());

        return PageResult.of(postPage.getTotal(), page, size, vos);
    }

    @Override
    public PageResult<PostVO> search(String keyword, int page, int size) {
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getStatus, "approved")
               .and(w -> w.like(Post::getTitle, keyword).or().like(Post::getContent, keyword))
               .orderByDesc(Post::getCreateTime);

        Page<Post> postPage = new Page<>(page, size);
        postMapper.selectPage(postPage, wrapper);

        List<PostVO> vos = postPage.getRecords().stream()
                .map(p -> toVO(p, null))
                .collect(Collectors.toList());

        return PageResult.of(postPage.getTotal(), page, size, vos);
    }

    @Override
    public PostVO getDetail(Long postId, Long currentUserId) {
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new RuntimeException("Post not found");
        }
        return toVO(post, currentUserId);
    }

    @Override
    public PageResult<PostVO> getPending(int page, int size) {
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getStatus, "pending")
               .orderByAsc(Post::getCreateTime);

        Page<Post> postPage = new Page<>(page, size);
        postMapper.selectPage(postPage, wrapper);

        List<PostVO> vos = postPage.getRecords().stream()
                .map(p -> toVO(p, null))
                .collect(Collectors.toList());

        return PageResult.of(postPage.getTotal(), page, size, vos);
    }

    @Override
    @Transactional
    public void review(Long postId, String action, String rejectReason) {
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new RuntimeException("Post not found");
        }
        if (!"pending".equals(post.getStatus())) {
            throw new RuntimeException("Post has already been reviewed");
        }

        if ("approve".equals(action)) {
            post.setStatus("approved");
            post.setIsRecommended(false);
            post.setRejectReason(null);
        } else if ("reject".equals(action)) {
            if (rejectReason == null || rejectReason.trim().isEmpty()) {
                throw new RuntimeException("Reject reason is required");
            }
            post.setStatus("rejected");
            post.setRejectReason(rejectReason);
            post.setIsRecommended(false);
        } else {
            throw new RuntimeException("Invalid action. Must be 'approve' or 'reject'");
        }

        postMapper.updateById(post);
    }

    @Override
    @Transactional
    public void toggleRecommend(Long postId) {
        Post post = postMapper.selectById(postId);
        if (post == null) throw new RuntimeException("Post not found");
        if ("pending".equals(post.getStatus())) {
            post.setStatus("approved");
            post.setRejectReason(null);
        } else if (!"approved".equals(post.getStatus())) {
            throw new RuntimeException("Only approved or pending posts can be recommended");
        }
        post.setIsRecommended(!Boolean.TRUE.equals(post.getIsRecommended()));
        postMapper.updateById(post);
    }

    @Override
    @Transactional
    public void deletePost(Long userId, Long postId) {
        Post post = postMapper.selectById(postId);
        if (post == null) throw new RuntimeException("Post not found");
        if (!post.getUserId().equals(userId)) throw new RuntimeException("You can only delete your own posts");
        postMapper.deleteById(postId);
    }

    @Override
    @Transactional
    public void adminDeletePost(Long postId) {
        Post post = postMapper.selectById(postId);
        if (post == null) throw new RuntimeException("Post not found");
        postMapper.deleteById(postId);
    }

    @Override
    public PageResult<PostVO> getUserApprovedPosts(Long userId, int page, int size) {
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getUserId, userId).eq(Post::getStatus, "approved")
               .orderByDesc(Post::getCreateTime);
        Page<Post> postPage = new Page<>(page, size);
        postMapper.selectPage(postPage, wrapper);
        List<PostVO> vos = postPage.getRecords().stream()
                .map(p -> toVO(p, userId)).collect(Collectors.toList());
        return PageResult.of(postPage.getTotal(), page, size, vos);
    }

    private PostVO toVO(Post post, Long currentUserId) {
        PostVO vo = new PostVO();
        vo.setId(post.getId());
        vo.setTitle(post.getTitle());
        vo.setContent(post.getContent());
        vo.setImageUrls(post.getImageUrls());
        vo.setCategoryId(post.getCategoryId());
        vo.setUserId(post.getUserId());
        vo.setStatus(post.getStatus());
        vo.setRejectReason(post.getRejectReason());
        vo.setIsRecommended(post.getIsRecommended());
        vo.setCreateTime(post.getCreateTime());
        vo.setUpdateTime(post.getUpdateTime());

        Category category = categoryMapper.selectById(post.getCategoryId());
        if (category != null) {
            vo.setCategoryName(category.getName());
        }

        User user = userMapper.selectById(post.getUserId());
        if (user != null) {
            vo.setUsername(user.getUsername());
            vo.setNickname(user.getNickname());
            vo.setAvatarUrl(user.getAvatarUrl());
        }

        if (currentUserId != null) {
            LambdaQueryWrapper<Favorite> favWrapper = new LambdaQueryWrapper<>();
            favWrapper.eq(Favorite::getUserId, currentUserId)
                      .eq(Favorite::getPostId, post.getId());
            vo.setIsFavorited(favoriteMapper.selectCount(favWrapper) > 0);
        } else {
            vo.setIsFavorited(false);
        }

        // Like info
        vo.setLikeCount(postLikeService.getLikeCount(post.getId()));
        vo.setIsLiked(postLikeService.isLiked(currentUserId, post.getId()));

        return vo;
    }
}
