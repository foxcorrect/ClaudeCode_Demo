package com.wechat.match.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 微信关系表实体类
 */
@Data
@TableName("wechat_relation")
public class WeChatRelation {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("match_id")
    private String matchId;

    @TableField("user_id1")
    private Long userId1;

    @TableField("user_id2")
    private Long userId2;

    @TableField("wechat_id1")
    private String wechatId1;

    @TableField("wechat_id2")
    private String wechatId2;

    @TableField("exchange_time")
    private LocalDateTime exchangeTime;

    private Integer status; // 1-有效，0-已删除

    @TableField("created_at")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}