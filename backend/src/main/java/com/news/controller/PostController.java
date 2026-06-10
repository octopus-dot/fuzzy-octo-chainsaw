package com.news.controller;

import com.news.common.PageResult;
import com.news.common.Result;
import com.news.dto.PostVO;
import com.news.entity.Post;
import com.news.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping("/posts")
    public Result<Post> uploadPost(
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam Long categoryId,
            @RequestParam(required = false) List<MultipartFile> images,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.ok(postService.upload(userId, title, content, categoryId, images));
    }

    @GetMapping("/recommended")
    public Result<PageResult<PostVO>> getRecommended(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(postService.getRecommended(page, size));
    }

    @GetMapping("/posts/by-category/{categoryId}")
    public Result<PageResult<PostVO>> getByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(postService.getByCategory(categoryId, page, size));
    }

    @GetMapping("/posts/search")
    public Result<PageResult<PostVO>> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(postService.search(keyword, page, size));
    }

    @GetMapping("/posts/{id}")
    public Result<PostVO> getDetail(@PathVariable Long id, Authentication authentication) {
        Long currentUserId = authentication != null ? (Long) authentication.getPrincipal() : null;
        return Result.ok(postService.getDetail(id, currentUserId));
    }

    @DeleteMapping("/posts/{id}")
    public Result<Void> deletePost(@PathVariable Long id, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        postService.deletePost(userId, id);
        return Result.ok();
    }

    @DeleteMapping("/admin/posts/{id}")
    public Result<Void> adminDeletePost(@PathVariable Long id) {
        postService.adminDeletePost(id);
        return Result.ok();
    }

    @GetMapping("/user/{userId}/posts")
    public Result<PageResult<PostVO>> getUserApprovedPosts(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size) {
        return Result.ok(postService.getUserApprovedPosts(userId, page, size));
    }
}
