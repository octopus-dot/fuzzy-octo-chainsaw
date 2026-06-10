package com.news.controller;

import com.news.common.PageResult;
import com.news.common.Result;
import com.news.dto.CommentVO;
import com.news.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/comments/post/{postId}")
    public Result<PageResult<CommentVO>> getComments(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        Long currentUserId = authentication != null ? (Long) authentication.getPrincipal() : null;
        return Result.ok(commentService.getCommentsByPost(postId, page, size, currentUserId));
    }

    @PostMapping("/comments")
    public Result<CommentVO> createComment(@RequestBody Map<String, String> body,
                                           Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        Long postId = Long.valueOf(body.get("postId"));
        String content = body.get("content");
        return Result.ok(commentService.createComment(userId, postId, content));
    }

    @DeleteMapping("/comments/{commentId}")
    public Result<Void> deleteComment(@PathVariable Long commentId,
                                       Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        commentService.deleteComment(userId, commentId);
        return Result.ok();
    }

    @PostMapping("/comments/like/{commentId}")
    public Result<Map<String, Object>> toggleLike(@PathVariable Long commentId,
                                                    Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        boolean liked = commentService.toggleLike(userId, commentId);
        return Result.ok(Map.of("liked", liked));
    }
}
