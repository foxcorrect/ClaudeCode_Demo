package com.wechat.match.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 匹配记录表实体类
 */
@Data
@TableName("match_record")
public class MatchRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("match_id")
    private String matchId;

    @TableField("user_id1")
    private Long userId1;

    @TableField("user_id2")
    private Long userId2;

    @TableField("match_time")
    private LocalDateTime matchTime;

    private Integer status; // 1-匹配中，2-聊天中，3-已满意，4-已删除，5-已过期

    @TableField("satisfaction_user1")
    private Integer satisfactionUser1; // 1-满意，0-不满意

    @TableField("satisfaction_user2")
    private Integer satisfactionUser2; // 1-满意，0-不满意

    @TableField("wechat_exchanged")
    private Integer wechatExchanged; // 0-否，1-是

    @TableField("expire_time")
    private LocalDateTime expireTime;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}