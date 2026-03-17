package com.wechat.match.service;

import com.wechat.match.entity.Notification;

import java.util.List;

/**
 * 通知服务接口
 */
public interface NotificationService {

    /**
     * 创建通知
     */
    boolean createNotification(Notification notification);

    /**
     * 发送通知
     */
    boolean sendNotification(Long notificationId);

    /**
     * 发送微信模板消息
     */
    boolean sendWeChatTemplateMessage(Long userId, String templateId, Object data);

    /**
     * 获取用户通知列表
     */
    List<Notification> getUserNotifications(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 获取未读通知数量
     */
    int getUnreadNotificationCount(Long userId);

    /**
     * 标记通知已读
     */
    boolean markNotificationAsRead(Long notificationId, Long userId);

    /**
     * 标记所有通知已读
     */
    boolean markAllNotificationsAsRead(Long userId);

    /**
     * 删除通知
     */
    boolean deleteNotification(Long notificationId, Long userId);

    /**
     * 清理过期通知
     */
    int cleanupExpiredNotifications();
}