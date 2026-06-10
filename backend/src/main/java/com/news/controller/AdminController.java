package com.news.controller;

import com.news.common.PageResult;
import com.news.common.Result;
import com.news.dto.PostVO;
import com.news.dto.ReviewRequest;
import com.news.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final PostService postService;

    @GetMapping("/posts/pending")
    public Result<PageResult<PostVO>> getPending(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(postService.getPending(page, size));
    }

    @PostMapping("/posts/review")
    public Result<Void> review(@Valid @RequestBody ReviewRequest request) {
        postService.review(request.getPostId(), request.getAction(), request.getRejectReason());
        return Result.ok();
    }

    @PostMapping("/posts/recommend/{postId}")
    public Result<Void> toggleRecommend(@PathVariable Long postId) {
        postService.toggleRecommend(postId);
        return Result.ok();
    }
}
