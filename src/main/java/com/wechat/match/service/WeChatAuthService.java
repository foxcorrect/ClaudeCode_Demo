package com.wechat.match.service;

import com.wechat.match.dto.request.LoginRequest;
import com.wechat.match.dto.response.UserInfoResponse;

/**
 * 微信认证服务接口
 */
public interface WeChatAuthService {

    /**
     * 微信登录
     */
    String login(LoginRequest loginRequest);

    /**
     * 获取用户信息
     */
    UserInfoResponse getUserInfo(Long userId);

    /**
     * 更新用户信息
     */
    boolean updateUserInfo(Long userId, String encryptedData, String iv);

    /**
     * 刷新token
     */
    String refreshToken(String oldToken);

    /**
     * 验证token
     */
    boolean validateToken(String token);

    /**
     * 退出登录
     */
    void logout(Long userId);
}