package com.news.service;

import com.news.dto.*;
import com.news.entity.User;

public interface UserService {
    User register(RegisterRequest request);
    String login(LoginRequest request);
    User getProfile(Long userId);
    User updateNickname(Long userId, UpdateNicknameRequest request);
    void updatePassword(Long userId, UpdatePasswordRequest request);
    User updateAvatar(Long userId, String avatarUrl);
    User updateBio(Long userId, String bio);
    User findByUsername(String username);
}
