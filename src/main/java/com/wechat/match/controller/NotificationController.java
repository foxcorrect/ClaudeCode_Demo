package com.wechat.match.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wechat.match.dto.response.ApiResponse;
import com.wechat.match.entity.Notification;
import com.wechat.match.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 通知控制器
 */
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 获取用户通知列表
     */
    @GetMapping
    public ApiResponse<List<Notification>> getUserNotifications(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        try {
            List<Notification> notifications = notificationService.getUserNotifications(userId, pageNum, pageSize);
            return ApiResponse.success(notifications);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取未读通知数量
     */
    @GetMapping("/unread-count")
    public ApiResponse<Integer> getUnreadNotificationCount(@RequestParam Long userId) {
        try {
            int count = notificationService.getUnreadNotificationCount(userId);
            return ApiResponse.success(count);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 标记通知已读
     */
    @PostMapping("/{notificationId}/read")
    public ApiResponse<Void> markNotificationAsRead(
            @PathVariable Long notificationId,
            @RequestParam Long userId) {
        try {
            boolean success = notificationService.markNotificationAsRead(notificationId, userId);
            if (success) {
                return ApiResponse.success("通知标记已读成功", null);
            } else {
                return ApiResponse.error("通知标记已读失败");
            }
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 标记所有通知已读
     */
    @PostMapping("/mark-all-read")
    public ApiResponse<Void> markAllNotificationsAsRead(@RequestParam Long userId) {
        try {
            boolean success = notificationService.markAllNotificationsAsRead(userId);
            if (success) {
                return ApiResponse.success("所有通知标记已读成功", null);
            } else {
                return ApiResponse.error("所有通知标记已读失败");
            }
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 删除通知
     */
    @DeleteMapping("/{notificationId}")
    public ApiResponse<Void> deleteNotification(
            @PathVariable Long notificationId,
            @RequestParam Long userId) {
        try {
            boolean success = notificationService.deleteNotification(notificationId, userId);
            if (success) {
                return ApiResponse.success("通知删除成功", null);
            } else {
                return ApiResponse.error("通知删除失败");
            }
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 清理过期通知
     */
    @PostMapping("/cleanup")
    public ApiResponse<Integer> cleanupExpiredNotifications() {
        try {
            int count = notificationService.cleanupExpiredNotifications();
            return ApiResponse.success("清理完成，共清理 " + count + " 条通知", count);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 创建测试通知
     */
    @PostMapping("/test")
    public ApiResponse<Void> createTestNotification(@RequestParam Long userId) {
        try {
            Notification notification = new Notification();
            notification.setUserId(userId);
            notification.setType(1); // 新消息
            notification.setTitle("测试通知");
            notification.setContent("这是一个测试通知");
            notification.setRelatedId("test_123");

            boolean success = notificationService.createNotification(notification);
            if (success) {
                return ApiResponse.success("测试通知创建成功", null);
            } else {
                return ApiResponse.error("测试通知创建失败");
            }
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}