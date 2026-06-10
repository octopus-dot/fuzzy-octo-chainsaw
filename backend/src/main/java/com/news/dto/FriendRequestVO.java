package com.news.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FriendRequestVO {
    private Long id;
    private Long fromUserId;
    private String fromUserName;
    private String fromUserAvatar;
    private Long toUserId;
    private String status;
    private LocalDateTime createTime;
}
