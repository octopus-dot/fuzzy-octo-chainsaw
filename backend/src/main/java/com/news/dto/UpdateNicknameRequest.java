package com.news.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateNicknameRequest {
    @NotBlank(message = "Nickname cannot be empty")
    private String nickname;
}
