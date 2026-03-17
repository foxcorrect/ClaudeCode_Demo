package com.wechat.match.controller;

import com.wechat.match.dto.request.SendMessageRequest;
import com.wechat.match.dto.response.ApiResponse;
import com.wechat.match.entity.ChatMessage;
import com.wechat.match.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 聊天控制器（HTTP备用通道）
 */
@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    /**
     * 发送消息
     */
    @PostMapping("/send")
    public ApiResponse<Void> sendMessage(@Valid @RequestBody SendMessageRequest request) {
        try {
            boolean success = chatService.sendMessage(request);
            if (success) {
                return ApiResponse.success("消息发送成功", null);
            } else {
                return ApiResponse.error("消息发送失败");
            }
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取聊天历史
     */
    @GetMapping("/history/{matchId}")
    public ApiResponse<List<ChatMessage>> getChatHistory(
            @PathVariable String matchId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        try {
            List<ChatMessage> messages = chatService.getChatHistory(matchId, pageNum, pageSize);
            return ApiResponse.success(messages);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取未读消息数量
     */
    @GetMapping("/unread-count")
    public ApiResponse<Integer> getUnreadMessageCount(@RequestParam Long userId) {
        try {
            int count = chatService.getUnreadMessageCount(userId);
            return ApiResponse.success(count);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 标记消息已读
     */
    @PostMapping("/mark-read")
    public ApiResponse<Void> markMessagesAsRead(
            @RequestParam Long userId,
            @RequestParam String matchId) {
        try {
            boolean success = chatService.markMessagesAsRead(userId, matchId);
            if (success) {
                return ApiResponse.success("消息标记已读成功", null);
            } else {
                return ApiResponse.error("消息标记已读失败");
            }
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取离线消息
     */
    @GetMapping("/offline")
    public ApiResponse<List<ChatMessage>> getOfflineMessages(@RequestParam Long userId) {
        try {
            List<ChatMessage> messages = chatService.getOfflineMessages(userId);
            return ApiResponse.success(messages);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 清理过期聊天消息
     */
    @PostMapping("/cleanup")
    public ApiResponse<Integer> cleanupExpiredMessages() {
        try {
            int count = chatService.cleanupExpiredMessages();
            return ApiResponse.success("清理完成，共清理 " + count + " 条聊天消息", count);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}