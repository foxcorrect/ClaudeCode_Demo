package com.wechat.match.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 匹配结果响应DTO
 */
@Data
public class MatchResultResponse {
    private String matchId;
    private Long matchedUserId;
    private String matchedUserNickname;
    private String matchedUserAvatar;
    private Integer matchedUserGender;
    private LocalDateTime matchTime;
    private LocalDateTime expireTime;
    private Integer status; // 匹配状态
}