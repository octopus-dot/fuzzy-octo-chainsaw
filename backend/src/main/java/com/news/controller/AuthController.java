package com.news.controller;

import com.news.common.Result;
import com.news.dto.LoginRequest;
import com.news.dto.RegisterRequest;
import com.news.dto.UserVO;
import com.news.entity.User;
import com.news.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public Result<Map<String, String>> register(@Valid @RequestBody RegisterRequest request) {
        userService.register(request);
        return Result.ok(Map.of("message", "Registration successful"));
    }

    @PostMapping("/login")
    public Result<Map<String, String>> login(@Valid @RequestBody LoginRequest request) {
        String token = userService.login(request);
        return Result.ok(Map.of("token", token));
    }

    @GetMapping("/profile/{userId}")
    public Result<UserVO> getPublicProfile(@PathVariable Long userId) {
        User u = userService.getProfile(userId);
        UserVO vo = new UserVO();
        vo.setId(u.getId()); vo.setUsername(u.getUsername()); vo.setNickname(u.getNickname());
        vo.setAvatarUrl(u.getAvatarUrl()); vo.setBio(u.getBio());
        vo.setBackgroundUrl(u.getBackgroundUrl()); vo.setRole(u.getRole());
        vo.setCreateTime(u.getCreateTime());
        return Result.ok(vo);
    }
}
