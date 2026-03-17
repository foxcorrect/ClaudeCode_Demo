package com.wechat.match.dto.request;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 发送消息请求DTO
 */
@Data
public class SendMessageRequest {

    @NotBlank(message = "匹配ID不能为空")
    private String matchId;

    @NotNull(message = "发送者ID不能为空")
    private Long senderId;

    @NotNull(message = "接收者ID不能为空")
    private Long receiverId;

    @NotNull(message = "消息类型不能为空")
    private Integer messageType; // 1-文本，2-图片，3-语音

    @NotBlank(message = "消息内容不能为空")
    private String content;
}