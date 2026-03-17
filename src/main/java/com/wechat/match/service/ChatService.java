package com.wechat.match.service;

import com.wechat.match.dto.request.SendMessageRequest;
import com.wechat.match.entity.ChatMessage;

import java.util.List;

/**
 * 聊天服务接口
 */
public interface ChatService {

    /**
     * 发送消息
     */
    boolean sendMessage(SendMessageRequest request);

    /**
     * 获取聊天历史
     */
    List<ChatMessage> getChatHistory(String matchId, Integer pageNum, Integer pageSize);

    /**
     * 获取未读消息数量
     */
    int getUnreadMessageCount(Long userId);

    /**
     * 标记消息已读
     */
    boolean markMessagesAsRead(Long userId, String matchId);

    /**
     * 保存聊天消息
     */
    boolean saveMessage(ChatMessage chatMessage);

    /**
     * 获取离线消息
     */
    List<ChatMessage> getOfflineMessages(Long userId);

    /**
     * 清理过期聊天消息
     */
    int cleanupExpiredMessages();
}