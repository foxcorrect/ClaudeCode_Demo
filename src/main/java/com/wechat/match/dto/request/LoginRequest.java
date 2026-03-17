package com.wechat.match.dto.request;

import lombok.Data;
import javax.validation.constraints.NotBlank;

/**
 * 微信登录请求DTO
 */
@Data
public class LoginRequest {

    @NotBlank(message = "微信code不能为空")
    private String code;

    private String encryptedData;
    private String iv;
}