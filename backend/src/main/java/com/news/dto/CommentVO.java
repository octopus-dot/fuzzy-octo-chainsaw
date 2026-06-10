package com.news.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CommentVO {
    private Long id;
    private Long postId;
    private String postTitle;
    private Long userId;
    private String nickname;
    private String avatarUrl;
    private String content;
    private Integer likeCount;
    private Boolean isLiked;
    private LocalDateTime createTime;
}
