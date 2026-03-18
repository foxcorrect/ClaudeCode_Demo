package com.wechat.match.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 聊天消息表实体类
 */
@Data
@TableName("chat_message")
public class ChatMessage {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("match_id")
    private String matchId;

    @TableField("sender_id")
    private Long senderId;

    @TableField("receiver_id")
    private Long receiverId;

    @TableField("message_type")
    private Integer messageType; // 1-文本，2-图片，3-语音

    private String content;

    @TableField("is_read")
    private Integer isRead; // 0-未读，1-已读

    @TableField("read_time")
    private LocalDateTime readTime;

    @TableField("send_time")
    private LocalDateTime sendTime;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}