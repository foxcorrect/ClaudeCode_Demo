package com.wechat.match.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wechat.match.entity.Notification;
import org.apache.ibatis.annotations.Mapper;

/**
 * 通知记录表 Mapper 接口
 */
@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {
}