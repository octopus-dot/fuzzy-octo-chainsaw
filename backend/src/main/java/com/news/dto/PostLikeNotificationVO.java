package com.news.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PostLikeNotificationVO {
    private Long id;
    private Long postId;
    private String postTitle;
    private Long fromUserId;
    private String fromUserName;
    private String fromUserAvatar;
    private LocalDateTime createTime;
}
