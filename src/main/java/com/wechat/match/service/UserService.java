package com.wechat.match.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wechat.match.dto.response.UserInfoResponse;
import com.wechat.match.entity.User;

import java.util.List;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 获取用户信息
     */
    User getUserById(Long userId);

    /**
     * 获取用户信息（响应DTO）
     */
    UserInfoResponse getUserInfoResponse(Long userId);

    /**
     * 更新用户信息
     */
    boolean updateUser(User user);

    /**
     * 更新用户状态
     */
    boolean updateUserStatus(Long userId, Integer status);

    /**
     * 更新用户匹配次数
     */
    boolean updateMatchCount(Long userId, Integer matchCount);

    /**
     * 重置每日匹配次数
     */
    void resetDailyMatchCount();

    /**
     * 获取在线用户列表
     */
    List<User> getOnlineUsers();

    /**
     * 获取可匹配用户列表
     */
    List<User> getAvailableUsers();

    /**
     * 分页查询用户
     */
    Page<User> getUsersByPage(Integer pageNum, Integer pageSize);

    /**
     * 根据OpenID查询用户
     */
    User getUserByOpenId(String openId);

    /**
     * 搜索用户
     */
    List<User> searchUsers(String keyword);
}