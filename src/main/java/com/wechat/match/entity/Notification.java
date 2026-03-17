package com.wechat.match.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 通知记录表实体类
 */
@Data
@TableName("notification")
public class Notification {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    private Integer type; // 1-新消息，2-匹配成功，3-微信交换，4-系统通知

    private String title;

    private String content;

    @TableField("related_id")
    private String relatedId;

    @TableField("is_sent")
    private Integer isSent; // 0-未发送，1-已发送

    @TableField("send_time")
    private LocalDateTime sendTime;

    @TableField("is_read")
    private Integer isRead; // 0-未读，1-已读

    @TableField("read_time")
    private LocalDateTime readTime;

    @TableField("expire_time")
    private LocalDateTime expireTime;

    @TableField("created_at")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}