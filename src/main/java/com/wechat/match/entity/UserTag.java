package com.wechat.match.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户标签关联表实体类
 */
@Data
@TableName("user_tag")
public class UserTag {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("tag_id")
    private Long tagId;

    private Integer weight; // 1-低，2-中，3-高

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    // 关联的标签信息（非数据库字段，用于查询）
    @TableField(exist = false)
    private Tag tag;
}