package com.news.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("report")
public class Report {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long postId;
    private Long reporterId;
    private String reason;
    private String description;
    private String status;
    private Long adminId;
    private String handleNote;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
