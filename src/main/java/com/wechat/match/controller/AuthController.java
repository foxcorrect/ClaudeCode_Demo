package com.wechat.match.controller;

import com.wechat.match.dto.request.LoginRequest;
import com.wechat.match.dto.response.ApiResponse;
import com.wechat.match.dto.response.UserInfoResponse;
import com.wechat.match.service.WeChatAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final WeChatAuthService weChatAuthService;

    /**
     * 微信登录
     */
    @PostMapping("/login")
    public ApiResponse<String> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            String token = weChatAuthService.login(loginRequest);
            return ApiResponse.success("登录成功", token);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取用户信息
     */
    @GetMapping("/user-info")
    public ApiResponse<UserInfoResponse> getUserInfo(HttpServletRequest request) {
        try {
            String token = extractToken(request);
            if (token == null || !weChatAuthService.validateToken(token)) {
                return ApiResponse.unauthorized("未授权访问");
            }

            // 从token中获取用户ID
            // 这里需要解析token，为了简化，可以传递用户ID
            // 暂时需要前端传递用户ID
            return ApiResponse.error("需要用户ID参数");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取用户信息（通过用户ID）
     */
    @GetMapping("/user-info/{userId}")
    public ApiResponse<UserInfoResponse> getUserInfoById(@PathVariable Long userId) {
        try {
            UserInfoResponse userInfo = weChatAuthService.getUserInfo(userId);
            return ApiResponse.success(userInfo);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 刷新Token
     */
    @PostMapping("/refresh-token")
    public ApiResponse<String> refreshToken(@RequestHeader("Authorization") String token) {
        try {
            String newToken = weChatAuthService.refreshToken(token);
            return ApiResponse.success("Token刷新成功", newToken);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestHeader("Authorization") String token) {
        try {
            // 解析token获取用户ID
            // 这里需要解析token，暂时留空
            weChatAuthService.logout(null);
            return ApiResponse.success("退出成功", null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 从请求中提取Token
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}