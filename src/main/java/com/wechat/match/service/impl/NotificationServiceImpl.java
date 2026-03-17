package com.wechat.match.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wechat.match.entity.Notification;
import com.wechat.match.entity.User;
import com.wechat.match.mapper.NotificationMapper;
import com.wechat.match.mapper.UserMapper;
import com.wechat.match.service.NotificationService;
import com.wechat.match.websocket.ChatWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 通知服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;
    private final UserMapper userMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ChatWebSocketHandler chatWebSocketHandler;

    // Redis键定义
    private static final String UNREAD_NOTIFICATION_KEY = "unread:notification:";
    private static final String NOTIFICATION_QUEUE_KEY = "notification:queue";

    @Override
    @Transactional
    public boolean createNotification(Notification notification) {
        try {
            // 设置默认值
            if (notification.getIsSent() == null) {
                notification.setIsSent(0);
            }
            if (notification.getIsRead() == null) {
                notification.setIsRead(0);
            }
            if (notification.getExpireTime() == null) {
                notification.setExpireTime(LocalDateTime.now().plusDays(7));
            }

            notificationMapper.insert(notification);

            // 异步发送通知
            redisTemplate.opsForList().rightPush(NOTIFICATION_QUEUE_KEY, notification.getId());

            // 更新未读计数
            String unreadKey = UNREAD_NOTIFICATION_KEY + notification.getUserId();
            redisTemplate.opsForValue().increment(unreadKey, 1);

            log.info("创建通知成功，通知ID: {}, 用户ID: {}", notification.getId(), notification.getUserId());
            return true;

        } catch (Exception e) {
            log.error("创建通知失败", e);
            return false;
        }
    }

    @Override
    public boolean sendNotification(Long notificationId) {
        try {
            Notification notification = notificationMapper.selectById(notificationId);
            if (notification == null) {
                log.error("通知不存在，通知ID: {}", notificationId);
                return false;
            }

            // 检查通知是否已发送
            if (notification.getIsSent() == 1) {
                log.info("通知已发送，通知ID: {}", notificationId);
                return true;
            }

            // 1. 尝试通过WebSocket实时推送
            boolean wsSent = sendViaWebSocket(notification);

            // 2. 如果WebSocket推送失败，记录日志，稍后重试
            if (!wsSent) {
                log.warn("WebSocket推送失败，通知ID: {}, 用户ID: {}", notificationId, notification.getUserId());
                // 可以加入重试队列
            }

            // 3. 如果是重要通知，可以发送微信模板消息
            if (notification.getType() == 3) { // 微信交换通知
                sendWeChatTemplateMessage(notification.getUserId(), "exchange_wechat", notification);
            }

            // 更新通知状态
            notification.setIsSent(1);
            notification.setSendTime(LocalDateTime.now());
            notificationMapper.updateById(notification);

            log.info("通知发送成功，通知ID: {}, 用户ID: {}", notificationId, notification.getUserId());
            return true;

        } catch (Exception e) {
            log.error("发送通知失败，通知ID: {}", notificationId, e);
            return false;
        }
    }

    @Override
    public boolean sendWeChatTemplateMessage(Long userId, String templateId, Object data) {
        try {
            User user = userMapper.selectById(userId);
            if (user == null || user.getOpenId() == null) {
                log.error("用户不存在或没有OpenID，用户ID: {}", userId);
                return false;
            }

            // 这里应该调用微信API发送模板消息
            // 由于微信API需要appId和appSecret，这里只记录日志
            log.info("发送微信模板消息，用户ID: {}, OpenID: {}, 模板ID: {}, 数据: {}",
                    userId, user.getOpenId(), templateId, data);

            // 模拟发送成功
            return true;

        } catch (Exception e) {
            log.error("发送微信模板消息失败，用户ID: {}", userId, e);
            return false;
        }
    }

    @Override
    public List<Notification> getUserNotifications(Long userId, Integer pageNum, Integer pageSize) {
        try {
            Page<Notification> page = new Page<>(pageNum, pageSize);
            LambdaQueryWrapper<Notification> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Notification::getUserId, userId)
                    .orderByDesc(Notification::getCreatedAt);

            Page<Notification> resultPage = notificationMapper.selectPage(page, queryWrapper);
            return resultPage.getRecords();

        } catch (Exception e) {
            log.error("获取用户通知列表失败，用户ID: {}", userId, e);
            throw new RuntimeException("获取通知列表失败: " + e.getMessage());
        }
    }

    @Override
    public int getUnreadNotificationCount(Long userId) {
        try {
            String unreadKey = UNREAD_NOTIFICATION_KEY + userId;
            Object count = redisTemplate.opsForValue().get(unreadKey);
            return count == null ? 0 : Integer.parseInt(count.toString());
        } catch (Exception e) {
            log.error("获取未读通知数量失败，用户ID: {}", userId, e);
            return 0;
        }
    }

    @Override
    @Transactional
    public boolean markNotificationAsRead(Long notificationId, Long userId) {
        try {
            Notification notification = notificationMapper.selectById(notificationId);
            if (notification == null || !notification.getUserId().equals(userId)) {
                log.error("通知不存在或不属于该用户，通知ID: {}, 用户ID: {}", notificationId, userId);
                return false;
            }

            if (notification.getIsRead() == 1) {
                return true; // 已经已读
            }

            // 更新数据库
            notification.setIsRead(1);
            notification.setReadTime(LocalDateTime.now());
            notificationMapper.updateById(notification);

            // 更新Redis未读计数
            String unreadKey = UNREAD_NOTIFICATION_KEY + userId;
            Long currentCount = redisTemplate.opsForValue().decrement(unreadKey, 1);
            if (currentCount != null && currentCount < 0) {
                redisTemplate.delete(unreadKey);
            }

            log.info("标记通知已读成功，通知ID: {}, 用户ID: {}", notificationId, userId);
            return true;

        } catch (Exception e) {
            log.error("标记通知已读失败，通知ID: {}, 用户ID: {}", notificationId, userId, e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean markAllNotificationsAsRead(Long userId) {
        try {
            // 更新数据库
            LambdaQueryWrapper<Notification> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Notification::getUserId, userId)
                    .eq(Notification::getIsRead, 0);

            Notification updateEntity = new Notification();
            updateEntity.setIsRead(1);
            updateEntity.setReadTime(LocalDateTime.now());

            int updatedCount = notificationMapper.update(updateEntity, queryWrapper);

            // 更新Redis未读计数
            if (updatedCount > 0) {
                String unreadKey = UNREAD_NOTIFICATION_KEY + userId;
                redisTemplate.delete(unreadKey);
            }

            log.info("标记所有通知已读成功，用户ID: {}, 更新数量: {}", userId, updatedCount);
            return updatedCount > 0;

        } catch (Exception e) {
            log.error("标记所有通知已读失败，用户ID: {}", userId, e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean deleteNotification(Long notificationId, Long userId) {
        try {
            Notification notification = notificationMapper.selectById(notificationId);
            if (notification == null || !notification.getUserId().equals(userId)) {
                log.error("通知不存在或不属于该用户，通知ID: {}, 用户ID: {}", notificationId, userId);
                return false;
            }

            // 如果通知未读，需要更新未读计数
            if (notification.getIsRead() == 0) {
                String unreadKey = UNREAD_NOTIFICATION_KEY + userId;
                Long currentCount = redisTemplate.opsForValue().decrement(unreadKey, 1);
                if (currentCount != null && currentCount < 0) {
                    redisTemplate.delete(unreadKey);
                }
            }

            // 删除通知
            notificationMapper.deleteById(notificationId);

            log.info("删除通知成功，通知ID: {}, 用户ID: {}", notificationId, userId);
            return true;

        } catch (Exception e) {
            log.error("删除通知失败，通知ID: {}, 用户ID: {}", notificationId, userId, e);
            return false;
        }
    }

    @Override
    public int cleanupExpiredNotifications() {
        try {
            // 清理过期通知
            LocalDateTime now = LocalDateTime.now();
            LambdaQueryWrapper<Notification> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.lt(Notification::getExpireTime, now)
                    .eq(Notification::getIsRead, 1); // 只清理已读的过期通知

            List<Notification> expiredNotifications = notificationMapper.selectList(queryWrapper);
            int count = expiredNotifications.size();

            // 批量删除
            for (Notification notification : expiredNotifications) {
                notificationMapper.deleteById(notification.getId());
            }

            log.info("清理过期通知完成，共清理 {} 条记录", count);
            return count;

        } catch (Exception e) {
            log.error("清理过期通知失败", e);
            return 0;
        }
    }

    // ========== 私有方法 ==========

    private boolean sendViaWebSocket(Notification notification) {
        try {
            // 检查用户是否在线
            boolean isOnline = chatWebSocketHandler.isUserOnline(notification.getUserId());
            if (!isOnline) {
                return false;
            }

            // 构建WebSocket消息
            NotificationMessage message = new NotificationMessage(
                    notification.getId(),
                    notification.getType(),
                    notification.getTitle(),
                    notification.getContent(),
                    notification.getRelatedId(),
                    System.currentTimeMillis()
            );

            // 发送WebSocket消息
            return chatWebSocketHandler.sendMessageToUser(notification.getUserId(), message);

        } catch (Exception e) {
            log.error("WebSocket推送通知失败，通知ID: {}", notification.getId(), e);
            return false;
        }
    }

    // 内部消息类
    private static class NotificationMessage {
        private final Long id;
        private final Integer type;
        private final String title;
        private final String content;
        private final String relatedId;
        private final Long timestamp;

        public NotificationMessage(Long id, Integer type, String title, String content, String relatedId, Long timestamp) {
            this.id = id;
            this.type = type;
            this.title = title;
            this.content = content;
            this.relatedId = relatedId;
            this.timestamp = timestamp;
        }

        // getters
        public Long getId() { return id; }
        public Integer getType() { return type; }
        public String getTitle() { return title; }
        public String getContent() { return content; }
        public String getRelatedId() { return relatedId; }
        public Long getTimestamp() { return timestamp; }
    }
}