package com.wechat.match.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wechat.match.dto.request.SendMessageRequest;
import com.wechat.match.entity.ChatMessage;
import com.wechat.match.entity.MatchRecord;
import com.wechat.match.mapper.ChatMessageMapper;
import com.wechat.match.mapper.MatchRecordMapper;
import com.wechat.match.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 聊天服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatMessageMapper chatMessageMapper;
    private final MatchRecordMapper matchRecordMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    // Redis键定义
    private static final String OFFLINE_MESSAGES_KEY = "offline:messages:";
    private static final String UNREAD_COUNT_KEY = "unread:count:";
    private static final String CHAT_HISTORY_KEY = "chat:history:";

    @Override
    @Transactional
    public boolean sendMessage(SendMessageRequest request) {
        try {
            // 验证匹配是否存在且有效
            MatchRecord matchRecord = getMatchRecord(request.getMatchId());
            if (matchRecord == null || matchRecord.getStatus() != 2) {
                log.error("匹配不存在或不在聊天中，matchId: {}", request.getMatchId());
                return false;
            }

            // 验证发送者和接收者是否是匹配的参与者
            if (!isParticipant(matchRecord, request.getSenderId(), request.getReceiverId())) {
                log.error("用户不是匹配的参与者，senderId: {}, receiverId: {}, matchId: {}",
                        request.getSenderId(), request.getReceiverId(), request.getMatchId());
                return false;
            }

            // 创建聊天消息
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setMatchId(request.getMatchId());
            chatMessage.setSenderId(request.getSenderId());
            chatMessage.setReceiverId(request.getReceiverId());
            chatMessage.setMessageType(request.getMessageType());
            chatMessage.setContent(request.getContent());
            chatMessage.setSendTime(LocalDateTime.now());
            chatMessage.setIsRead(0); // 未读

            // 保存到数据库
            chatMessageMapper.insert(chatMessage);

            // 检查接收者是否在线（这里简化处理）
            boolean isReceiverOnline = checkUserOnline(request.getReceiverId());
            if (!isReceiverOnline) {
                // 存储离线消息
                String offlineKey = OFFLINE_MESSAGES_KEY + request.getReceiverId();
                redisTemplate.opsForList().rightPush(offlineKey, chatMessage);
                redisTemplate.expire(offlineKey, 7, TimeUnit.DAYS);
            }

            // 更新未读消息计数
            String unreadKey = UNREAD_COUNT_KEY + request.getReceiverId();
            redisTemplate.opsForValue().increment(unreadKey, 1);

            // 缓存最近消息
            cacheRecentMessage(request.getMatchId(), chatMessage);

            log.info("消息发送成功，senderId: {}, receiverId: {}, matchId: {}",
                    request.getSenderId(), request.getReceiverId(), request.getMatchId());
            return true;

        } catch (Exception e) {
            log.error("消息发送失败", e);
            return false;
        }
    }

    @Override
    public List<ChatMessage> getChatHistory(String matchId, Integer pageNum, Integer pageSize) {
        try {
            // 先从缓存查询
            String cacheKey = CHAT_HISTORY_KEY + matchId + ":" + pageNum + ":" + pageSize;
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                return (List<ChatMessage>) cached;
            }

            // 从数据库查询
            Page<ChatMessage> page = new Page<>(pageNum, pageSize);
            LambdaQueryWrapper<ChatMessage> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ChatMessage::getMatchId, matchId)
                    .orderByDesc(ChatMessage::getSendTime);

            Page<ChatMessage> resultPage = chatMessageMapper.selectPage(page, queryWrapper);
            List<ChatMessage> messages = resultPage.getRecords();

            // 缓存结果（5分钟）
            redisTemplate.opsForValue().set(cacheKey, messages, 5, TimeUnit.MINUTES);

            return messages;

        } catch (Exception e) {
            log.error("获取聊天历史失败，matchId: {}", matchId, e);
            throw new RuntimeException("获取聊天历史失败: " + e.getMessage());
        }
    }

    @Override
    public int getUnreadMessageCount(Long userId) {
        try {
            String unreadKey = UNREAD_COUNT_KEY + userId;
            Object count = redisTemplate.opsForValue().get(unreadKey);
            return count == null ? 0 : Integer.parseInt(count.toString());
        } catch (Exception e) {
            log.error("获取未读消息数量失败，userId: {}", userId, e);
            return 0;
        }
    }

    @Override
    @Transactional
    public boolean markMessagesAsRead(Long userId, String matchId) {
        try {
            // 更新数据库中的未读消息
            LambdaQueryWrapper<ChatMessage> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ChatMessage::getMatchId, matchId)
                    .eq(ChatMessage::getReceiverId, userId)
                    .eq(ChatMessage::getIsRead, 0);

            ChatMessage updateEntity = new ChatMessage();
            updateEntity.setIsRead(1);
            updateEntity.setReadTime(LocalDateTime.now());

            int updatedCount = chatMessageMapper.update(updateEntity, queryWrapper);

            // 更新Redis中的未读计数
            if (updatedCount > 0) {
                String unreadKey = UNREAD_COUNT_KEY + userId;
                Long currentCount = redisTemplate.opsForValue().decrement(unreadKey, updatedCount);
                if (currentCount != null && currentCount < 0) {
                    redisTemplate.delete(unreadKey);
                }
            }

            log.info("标记消息已读成功，userId: {}, matchId: {}, 更新数量: {}", userId, matchId, updatedCount);
            return updatedCount > 0;

        } catch (Exception e) {
            log.error("标记消息已读失败，userId: {}, matchId: {}", userId, matchId, e);
            return false;
        }
    }

    @Override
    public boolean saveMessage(ChatMessage chatMessage) {
        try {
            chatMessageMapper.insert(chatMessage);
            return true;
        } catch (Exception e) {
            log.error("保存聊天消息失败", e);
            return false;
        }
    }

    @Override
    public List<ChatMessage> getOfflineMessages(Long userId) {
        try {
            String offlineKey = OFFLINE_MESSAGES_KEY + userId;
            List<Object> messages = redisTemplate.opsForList().range(offlineKey, 0, -1);
            if (messages == null || messages.isEmpty()) {
                return List.of();
            }

            // 转换为ChatMessage列表
            List<ChatMessage> chatMessages = messages.stream()
                    .filter(obj -> obj instanceof ChatMessage)
                    .map(obj -> (ChatMessage) obj)
                    .toList();

            // 清空离线消息
            redisTemplate.delete(offlineKey);

            log.info("获取离线消息成功，userId: {}, 数量: {}", userId, chatMessages.size());
            return chatMessages;

        } catch (Exception e) {
            log.error("获取离线消息失败，userId: {}", userId, e);
            return List.of();
        }
    }

    @Override
    public int cleanupExpiredMessages() {
        try {
            // 清理7天前的聊天消息
            LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
            LambdaQueryWrapper<ChatMessage> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.lt(ChatMessage::getSendTime, sevenDaysAgo);

            List<ChatMessage> expiredMessages = chatMessageMapper.selectList(queryWrapper);
            int count = expiredMessages.size();

            // 批量删除
            for (ChatMessage message : expiredMessages) {
                chatMessageMapper.deleteById(message.getId());
            }

            log.info("清理过期聊天消息完成，共清理 {} 条记录", count);
            return count;

        } catch (Exception e) {
            log.error("清理过期聊天消息失败", e);
            return 0;
        }
    }

    // ========== 私有方法 ==========

    private MatchRecord getMatchRecord(String matchId) {
        LambdaQueryWrapper<MatchRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MatchRecord::getMatchId, matchId);
        return matchRecordMapper.selectOne(queryWrapper);
    }

    private boolean isParticipant(MatchRecord matchRecord, Long senderId, Long receiverId) {
        return (matchRecord.getUserId1().equals(senderId) && matchRecord.getUserId2().equals(receiverId)) ||
                (matchRecord.getUserId1().equals(receiverId) && matchRecord.getUserId2().equals(senderId));
    }

    private boolean checkUserOnline(Long userId) {
        // 这里简化实现，实际应该检查WebSocket连接状态
        String onlineKey = "online:user:" + userId;
        return redisTemplate.hasKey(onlineKey);
    }

    private void cacheRecentMessage(String matchId, ChatMessage message) {
        String cacheKey = "recent:message:" + matchId;
        redisTemplate.opsForValue().set(cacheKey, message, 1, TimeUnit.HOURS);
    }
}