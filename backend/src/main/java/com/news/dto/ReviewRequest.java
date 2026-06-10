package com.news.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReviewRequest {
    @NotNull(message = "Post ID cannot be empty")
    private Long postId;

    @NotBlank(message = "Action cannot be empty (approve or reject)")
    private String action;

    private String rejectReason;
}
