package com.wechat.match.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户信息响应DTO
 */
@Data
public class UserInfoResponse {
    private Long id;
    private String openId;
    private String unionId;
    private String nickname;
    private String avatarUrl;
    private Integer gender;
    private String city;
    private String province;
    private String country;
    private String language;
    private LocalDateTime lastLoginTime;
    private Integer status;
    private Integer matchCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}