package com.wechat.match.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wechat.match.dto.response.ApiResponse;
import com.wechat.match.dto.response.UserInfoResponse;
import com.wechat.match.entity.User;
import com.wechat.match.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户管理控制器
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 获取用户信息
     */
    @GetMapping("/{userId}")
    public ApiResponse<UserInfoResponse> getUserInfo(@PathVariable Long userId) {
        try {
            UserInfoResponse userInfo = userService.getUserInfoResponse(userId);
            if (userInfo == null) {
                return ApiResponse.error("用户不存在");
            }
            return ApiResponse.success(userInfo);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/{userId}")
    public ApiResponse<Void> updateUser(@PathVariable Long userId, @RequestBody User user) {
        try {
            user.setId(userId);
            boolean success = userService.updateUser(user);
            if (success) {
                return ApiResponse.success("用户信息更新成功", null);
            } else {
                return ApiResponse.error("用户信息更新失败");
            }
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 更新用户状态
     */
    @PutMapping("/{userId}/status")
    public ApiResponse<Void> updateUserStatus(@PathVariable Long userId, @RequestParam Integer status) {
        try {
            boolean success = userService.updateUserStatus(userId, status);
            if (success) {
                return ApiResponse.success("用户状态更新成功", null);
            } else {
                return ApiResponse.error("用户状态更新失败");
            }
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取在线用户列表
     */
    @GetMapping("/online")
    public ApiResponse<List<User>> getOnlineUsers() {
        try {
            List<User> onlineUsers = userService.getOnlineUsers();
            return ApiResponse.success(onlineUsers);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取可匹配用户列表
     */
    @GetMapping("/available")
    public ApiResponse<List<User>> getAvailableUsers() {
        try {
            List<User> availableUsers = userService.getAvailableUsers();
            return ApiResponse.success(availableUsers);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 分页查询用户
     */
    @GetMapping("/page")
    public ApiResponse<Page<User>> getUsersByPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        try {
            Page<User> userPage = userService.getUsersByPage(pageNum, pageSize);
            return ApiResponse.success(userPage);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 搜索用户
     */
    @GetMapping("/search")
    public ApiResponse<List<User>> searchUsers(@RequestParam String keyword) {
        try {
            List<User> users = userService.searchUsers(keyword);
            return ApiResponse.success(users);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取用户统计信息
     */
    @GetMapping("/stats")
    public ApiResponse<Object> getUserStats() {
        try {
            // 这里可以返回用户统计信息
            // 暂时返回空对象
            return ApiResponse.success(new Object());
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}