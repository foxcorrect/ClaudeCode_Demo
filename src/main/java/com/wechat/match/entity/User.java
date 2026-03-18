package com.wechat.match.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户表实体类
 */
@Data
@TableName("user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("open_id")
    private String openId;

    @TableField("union_id")
    private String unionId;

    private String nickname;

    @TableField("avatar_url")
    private String avatarUrl;

    private Integer gender;

    private String city;

    private String province;

    private String country;

    private String language;

    @TableField("last_login_time")
    private LocalDateTime lastLoginTime;

    private Integer status; // 0-禁用，1-正常，2-匹配中

    @TableField("match_count")
    private Integer matchCount;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}