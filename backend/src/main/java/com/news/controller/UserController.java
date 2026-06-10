package com.news.controller;

import com.news.common.PageResult;
import com.news.common.Result;
import com.news.dto.CommentVO;
import com.news.dto.PostVO;
import com.news.dto.UpdateNicknameRequest;
import com.news.dto.UpdatePasswordRequest;
import com.news.entity.User;
import com.news.service.CommentService;
import com.news.service.FileStorageService;
import com.news.service.PostService;
import com.news.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final PostService postService;
    private final CommentService commentService;
    private final FileStorageService fileStorageService;

    @GetMapping("/profile")
    public Result<User> getProfile(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.ok(userService.getProfile(userId));
    }

    @PutMapping("/profile/nickname")
    public Result<User> updateNickname(@Valid @RequestBody UpdateNicknameRequest request,
                                        Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.ok(userService.updateNickname(userId, request));
    }

    @PutMapping("/profile/password")
    public Result<Void> updatePassword(@Valid @RequestBody UpdatePasswordRequest request,
                                        Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        userService.updatePassword(userId, request);
        return Result.ok();
    }

    @PostMapping("/profile/avatar")
    public Result<User> uploadAvatar(@RequestParam("file") MultipartFile file,
                                      Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        String avatarUrl = fileStorageService.storeAvatar(userId, file);
        return Result.ok(userService.updateAvatar(userId, avatarUrl));
    }

    @GetMapping("/posts")
    public Result<PageResult<PostVO>> getUserPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.ok(postService.getUserPosts(userId, page, size));
    }

    @GetMapping("/comments")
    public Result<PageResult<CommentVO>> getUserComments(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.ok(commentService.getUserComments(userId, page, size));
    }

    @PutMapping("/profile/bio")
    public Result<User> updateBio(@RequestBody Map<String, String> body, Authentication auth) {
        return Result.ok(userService.updateBio((Long) auth.getPrincipal(), body.get("bio")));
    }

}
