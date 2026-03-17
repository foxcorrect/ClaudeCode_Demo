package com.wechat.match.scheduler;

import com.wechat.match.service.ChatService;
import com.wechat.match.service.MatchingService;
import com.wechat.match.service.NotificationService;
import com.wechat.match.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 数据清理调度器
 * 负责定时清理过期数据
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataCleanupScheduler {

    private final MatchingService matchingService;
    private final ChatService chatService;
    private final NotificationService notificationService;
    private final UserService userService;

    /**
     * 每天凌晨3点执行数据清理
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanupExpiredData() {
        log.info("开始执行数据清理任务...");

        try {
            // 1. 清理过期匹配记录
            int expiredMatches = matchingService.cleanupExpiredMatches();
            log.info("清理过期匹配记录完成，共清理 {} 条", expiredMatches);

            // 2. 清理过期聊天消息
            int expiredMessages = chatService.cleanupExpiredMessages();
            log.info("清理过期聊天消息完成，共清理 {} 条", expiredMessages);

            // 3. 清理过期通知
            int expiredNotifications = notificationService.cleanupExpiredNotifications();
            log.info("清理过期通知完成，共清理 {} 条", expiredNotifications);

            // 4. 重置用户每日匹配次数
            userService.resetDailyMatchCount();
            log.info("用户每日匹配次数已重置");

            log.info("数据清理任务执行完成");

        } catch (Exception e) {
            log.error("数据清理任务执行失败", e);
        }
    }

    /**
     * 每小时执行一次用户状态维护
     */
    @Scheduled(cron = "0 0 */1 * * ?")
    public void maintainUserStatus() {
        log.info("开始执行用户状态维护任务...");

        try {
            // 这里可以添加用户状态维护逻辑
            // 例如：将长时间在线的用户标记为离线
            log.info("用户状态维护任务执行完成");

        } catch (Exception e) {
            log.error("用户状态维护任务执行失败", e);
        }
    }

    /**
     * 每30分钟执行一次匹配池维护
     */
    @Scheduled(cron = "0 */30 * * * ?")
    public void maintainMatchPool() {
        log.info("开始执行匹配池维护任务...");

        try {
            // 清理匹配池中超时的用户
            // 这里可以添加匹配池维护逻辑
            log.info("匹配池维护任务执行完成");

        } catch (Exception e) {
            log.error("匹配池维护任务执行失败", e);
        }
    }
}