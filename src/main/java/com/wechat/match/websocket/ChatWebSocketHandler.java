package com.wechat.match.websocket;

import com.alibaba.fastjson.JSON;
import com.wechat.match.dto.request.SendMessageRequest;
import com.wechat.match.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket聊天处理器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ChatService chatService;

    // 存储用户WebSocket会话
    private static final Map<Long, WebSocketSession> userSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = getUserIdFromSession(session);
        if (userId != null) {
            userSessions.put(userId, session);
            log.info("WebSocket连接建立，用户ID: {}, 会话ID: {}", userId, session.getId());
            // 发送离线消息
            sendOfflineMessages(userId, session);
        } else {
            log.warn("WebSocket连接建立，但未找到用户ID，会话ID: {}", session.getId());
            session.close(CloseStatus.NOT_ACCEPTABLE);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            Long userId = getUserIdFromSession(session);
            if (userId == null) {
                log.warn("未找到用户ID，关闭会话");
                session.close(CloseStatus.NOT_ACCEPTABLE);
                return;
            }

            String payload = message.getPayload();
            log.debug("收到WebSocket消息，用户ID: {}, 消息: {}", userId, payload);

            // 解析消息
            SendMessageRequest request = JSON.parseObject(payload, SendMessageRequest.class);
            if (request == null) {
                sendError(session, "消息格式错误");
                return;
            }

            // 验证发送者ID
            if (!userId.equals(request.getSenderId())) {
                sendError(session, "发送者ID不匹配");
                return;
            }

            // 发送消息
            boolean success = chatService.sendMessage(request);
            if (success) {
                // 如果接收者在线，实时推送消息
                Long receiverId = request.getReceiverId();
                WebSocketSession receiverSession = userSessions.get(receiverId);
                if (receiverSession != null && receiverSession.isOpen()) {
                    receiverSession.sendMessage(new TextMessage(JSON.toJSONString(request)));
                }

                // 发送成功响应
                sendSuccess(session, "消息发送成功");
            } else {
                sendError(session, "消息发送失败");
            }

        } catch (Exception e) {
            log.error("处理WebSocket消息失败", e);
            sendError(session, "服务器内部错误");
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long userId = getUserIdFromSession(session);
        if (userId != null) {
            userSessions.remove(userId);
            log.info("WebSocket连接关闭，用户ID: {}, 关闭状态: {}", userId, status);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket传输错误，会话ID: {}", session.getId(), exception);
        session.close(CloseStatus.SERVER_ERROR);
    }

    /**
     * 向指定用户发送消息
     */
    public boolean sendMessageToUser(Long userId, Object message) {
        WebSocketSession session = userSessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                String jsonMessage = JSON.toJSONString(message);
                session.sendMessage(new TextMessage(jsonMessage));
                return true;
            } catch (IOException e) {
                log.error("向用户发送WebSocket消息失败，用户ID: {}", userId, e);
                userSessions.remove(userId);
                return false;
            }
        }
        return false;
    }

    /**
     * 检查用户是否在线
     */
    public boolean isUserOnline(Long userId) {
        WebSocketSession session = userSessions.get(userId);
        return session != null && session.isOpen();
    }

    /**
     * 获取在线用户数量
     */
    public int getOnlineUserCount() {
        return userSessions.size();
    }

    // ========== 私有方法 ==========

    private Long getUserIdFromSession(WebSocketSession session) {
        try {
            Map<String, Object> attributes = session.getAttributes();
            if (attributes.containsKey("userId")) {
                return (Long) attributes.get("userId");
            }

            // 从查询参数中获取用户ID
            String query = session.getUri().getQuery();
            if (query != null && query.contains("userId=")) {
                String[] params = query.split("&");
                for (String param : params) {
                    if (param.startsWith("userId=")) {
                        String userIdStr = param.substring(7);
                        return Long.parseLong(userIdStr);
                    }
                }
            }

            return null;
        } catch (Exception e) {
            log.error("从WebSocket会话获取用户ID失败", e);
            return null;
        }
    }

    private void sendOfflineMessages(Long userId, WebSocketSession session) {
        try {
            // 获取离线消息
            // 这里可以调用chatService.getOfflineMessages(userId)
            // 简化处理，暂时不实现
        } catch (Exception e) {
            log.error("发送离线消息失败，用户ID: {}", userId, e);
        }
    }

    private void sendSuccess(WebSocketSession session, String message) throws IOException {
        Map<String, Object> response = Map.of(
                "type", "success",
                "message", message,
                "timestamp", System.currentTimeMillis()
        );
        session.sendMessage(new TextMessage(JSON.toJSONString(response)));
    }

    private void sendError(WebSocketSession session, String error) throws IOException {
        Map<String, Object> response = Map.of(
                "type", "error",
                "message", error,
                "timestamp", System.currentTimeMillis()
        );
        session.sendMessage(new TextMessage(JSON.toJSONString(response)));
    }
}