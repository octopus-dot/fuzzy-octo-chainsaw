package com.news.controller;

import com.news.common.Result;
import com.news.dto.PostLikeNotificationVO;
import com.news.service.PostLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PostLikeController {

    private final PostLikeService postLikeService;

    @PostMapping("/posts/like/{postId}")
    public Result<Map<String, Object>> toggleLike(@PathVariable Long postId, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        boolean liked = postLikeService.toggleLike(userId, postId);
        return Result.ok(Map.of("liked", liked));
    }

    @GetMapping("/posts/like/{postId}")
    public Result<Map<String, Object>> getLikeStatus(@PathVariable Long postId, Authentication auth) {
        Long userId = auth != null ? (Long) auth.getPrincipal() : null;
        return Result.ok(Map.of(
            "isLiked", postLikeService.isLiked(userId, postId),
            "likeCount", postLikeService.getLikeCount(postId)
        ));
    }

    @GetMapping("/user/notifications")
    public Result<List<PostLikeNotificationVO>> getNotifications(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return Result.ok(postLikeService.getNotifications(userId));
    }
}
