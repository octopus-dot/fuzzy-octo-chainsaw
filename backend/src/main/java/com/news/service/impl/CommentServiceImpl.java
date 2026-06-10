package com.news.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.news.common.PageResult;
import com.news.dto.CommentVO;
import com.news.entity.Comment;
import com.news.entity.CommentLike;
import com.news.entity.Post;
import com.news.entity.User;
import com.news.mapper.CommentLikeMapper;
import com.news.mapper.CommentMapper;
import com.news.mapper.PostMapper;
import com.news.mapper.UserMapper;
import com.news.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final CommentLikeMapper commentLikeMapper;
    private final UserMapper userMapper;
    private final PostMapper postMapper;

    @Override
    public PageResult<CommentVO> getCommentsByPost(Long postId, int page, int size, Long currentUserId) {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getPostId, postId)
               .orderByDesc(Comment::getCreateTime);

        Page<Comment> commentPage = new Page<>(page, size);
        commentMapper.selectPage(commentPage, wrapper);

        List<CommentVO> vos = commentPage.getRecords().stream()
                .map(c -> toVO(c, currentUserId))
                .collect(Collectors.toList());

        return PageResult.of(commentPage.getTotal(), page, size, vos);
    }

    @Override
    @Transactional
    public CommentVO createComment(Long userId, Long postId, String content) {
        Post post = postMapper.selectById(postId);
        if (post == null || !"approved".equals(post.getStatus())) {
            throw new RuntimeException("Post not found or not approved");
        }

        if (content == null || content.trim().isEmpty()) {
            throw new RuntimeException("Comment content cannot be empty");
        }
        if (content.length() > 500) {
            throw new RuntimeException("Comment must not exceed 500 characters");
        }

        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setContent(content.trim());
        comment.setLikeCount(0);
        commentMapper.insert(comment);

        return toVO(comment, userId);
    }

    @Override
    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new RuntimeException("Comment not found");
        }
        if (!comment.getUserId().equals(userId)) {
            throw new RuntimeException("You can only delete your own comments");
        }

        LambdaQueryWrapper<CommentLike> likeWrapper = new LambdaQueryWrapper<>();
        likeWrapper.eq(CommentLike::getCommentId, commentId);
        commentLikeMapper.delete(likeWrapper);

        commentMapper.deleteById(commentId);
    }

    @Override
    @Transactional
    public boolean toggleLike(Long userId, Long commentId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new RuntimeException("Comment not found");
        }

        LambdaQueryWrapper<CommentLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CommentLike::getCommentId, commentId)
               .eq(CommentLike::getUserId, userId);
        CommentLike existing = commentLikeMapper.selectOne(wrapper);

        if (existing != null) {
            commentLikeMapper.deleteById(existing.getId());
            comment.setLikeCount(Math.max(0, comment.getLikeCount() - 1));
            commentMapper.updateById(comment);
            return false;
        } else {
            CommentLike like = new CommentLike();
            like.setCommentId(commentId);
            like.setUserId(userId);
            commentLikeMapper.insert(like);
            comment.setLikeCount(comment.getLikeCount() + 1);
            commentMapper.updateById(comment);
            return true;
        }
    }

    @Override
    public PageResult<CommentVO> getUserComments(Long userId, int page, int size) {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getUserId, userId)
               .orderByDesc(Comment::getCreateTime);

        Page<Comment> commentPage = new Page<>(page, size);
        commentMapper.selectPage(commentPage, wrapper);

        List<CommentVO> vos = commentPage.getRecords().stream()
                .map(c -> toVO(c, userId))
                .collect(Collectors.toList());

        return PageResult.of(commentPage.getTotal(), page, size, vos);
    }

    private CommentVO toVO(Comment comment, Long currentUserId) {
        CommentVO vo = new CommentVO();
        vo.setId(comment.getId());
        vo.setPostId(comment.getPostId());
        vo.setUserId(comment.getUserId());
        vo.setContent(comment.getContent());
        vo.setLikeCount(comment.getLikeCount());
        vo.setCreateTime(comment.getCreateTime());

        Post post = postMapper.selectById(comment.getPostId());
        if (post != null) {
            vo.setPostTitle(post.getTitle());
        }

        User user = userMapper.selectById(comment.getUserId());
        if (user != null) {
            vo.setNickname(user.getNickname() != null ? user.getNickname() : user.getUsername());
            vo.setAvatarUrl(user.getAvatarUrl());
        }

        if (currentUserId != null) {
            LambdaQueryWrapper<CommentLike> likeWrapper = new LambdaQueryWrapper<>();
            likeWrapper.eq(CommentLike::getCommentId, comment.getId())
                       .eq(CommentLike::getUserId, currentUserId);
            vo.setIsLiked(commentLikeMapper.selectCount(likeWrapper) > 0);
        } else {
            vo.setIsLiked(false);
        }

        return vo;
    }
}
