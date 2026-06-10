package com.news.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserVO {
    private Long id;
    private String username;
    private String nickname;
    private String avatarUrl;
    private String bio;
    private String backgroundUrl;
    private String role;
    private LocalDateTime createTime;
}
