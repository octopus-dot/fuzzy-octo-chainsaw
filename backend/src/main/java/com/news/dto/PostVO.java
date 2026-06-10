package com.news.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PostVO {
    private Long id;
    private String title;
    private String content;
    private String imageUrls;
    private Long categoryId;
    private String categoryName;
    private Long userId;
    private String username;
    private String nickname;
    private String avatarUrl;
    private String status;
    private String rejectReason;
    private Boolean isRecommended;
    private Boolean isFavorited;
    private Boolean isLiked;
    private Integer likeCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
