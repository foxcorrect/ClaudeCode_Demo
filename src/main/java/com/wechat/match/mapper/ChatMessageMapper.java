package com.wechat.match.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wechat.match.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;

/**
 * 聊天消息表 Mapper 接口
 */
@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {
}