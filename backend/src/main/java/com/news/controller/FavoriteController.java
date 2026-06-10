package com.news.controller;

import com.news.common.PageResult;
import com.news.common.Result;
import com.news.dto.PostVO;
import com.news.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping("/{postId}")
    public Result<Void> addFavorite(@PathVariable Long postId, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        favoriteService.addFavorite(userId, postId);
        return Result.ok();
    }

    @DeleteMapping("/{postId}")
    public Result<Void> removeFavorite(@PathVariable Long postId, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        favoriteService.removeFavorite(userId, postId);
        return Result.ok();
    }

    @GetMapping
    public Result<PageResult<PostVO>> getFavorites(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.ok(favoriteService.getUserFavorites(userId, page, size));
    }
}
