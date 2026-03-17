package com.wechat.match.service;

import com.wechat.match.dto.response.MatchResultResponse;
import com.wechat.match.entity.MatchRecord;

import java.util.List;

/**
 * 匹配服务接口
 */
public interface MatchingService {

    /**
     * 用户加入匹配池
     */
    boolean joinMatchPool(Long userId);

    /**
     * 用户离开匹配池
     */
    boolean leaveMatchPool(Long userId);

    /**
     * 获取用户的匹配状态
     */
    Integer getMatchStatus(Long userId);

    /**
     * 执行匹配（为指定用户匹配5个对象）
     */
    List<MatchResultResponse> matchUsers(Long userId);

    /**
     * 获取用户的匹配记录
     */
    List<MatchRecord> getUserMatches(Long userId);

    /**
     * 获取活跃匹配记录
     */
    List<MatchRecord> getActiveMatches(Long userId);

    /**
     * 提交满意度反馈
     */
    boolean submitFeedback(Long userId, String matchId, Integer satisfaction);

    /**
     * 交换微信
     */
    boolean exchangeWeChat(Long userId1, Long userId2, String matchId);

    /**
     * 删除匹配（解除关系）
     */
    boolean deleteMatch(Long userId, String matchId);

    /**
     * 检查匹配是否过期
     */
    boolean isMatchExpired(String matchId);

    /**
     * 清理过期匹配
     */
    int cleanupExpiredMatches();

    /**
     * 获取匹配池大小
     */
    int getMatchPoolSize();

    /**
     * 重置匹配池
     */
    void resetMatchPool();
}