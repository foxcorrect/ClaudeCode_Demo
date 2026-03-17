package com.wechat.match.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import java.util.HashMap;
import java.util.Map;

/**
 * 微信API工具类
 */
@Slf4j
@Component
public class WeChatApiUtil {

    @Value("${wechat.mini-program.app-id}")
    private String appId;

    @Value("${wechat.mini-program.app-secret}")
    private String appSecret;

    @Value("${wechat.mini-program.token-url}")
    private String tokenUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 通过code获取微信openid和session_key
     */
    public JSONObject code2Session(String code) {
        try {
            String url = tokenUrl + "?appid={appid}&secret={secret}&js_code={js_code}&grant_type=authorization_code";
            Map<String, String> params = new HashMap<>();
            params.put("appid", appId);
            params.put("secret", appSecret);
            params.put("js_code", code);

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class, params);
            String body = response.getBody();
            log.info("微信登录响应: {}", body);

            return JSON.parseObject(body);
        } catch (Exception e) {
            log.error("调用微信code2Session接口失败", e);
            return null;
        }
    }

    /**
     * 验证微信登录是否有效
     */
    public boolean validateWeChatLogin(String code) {
        JSONObject result = code2Session(code);
        if (result == null) {
            return false;
        }
        // 微信返回错误码
        if (result.containsKey("errcode")) {
            Integer errcode = result.getInteger("errcode");
            return errcode == null || errcode == 0;
        }
        return result.containsKey("openid");
    }

    /**
     * 获取openid
     */
    public String getOpenId(String code) {
        JSONObject result = code2Session(code);
        if (result == null || result.containsKey("errcode")) {
            return null;
        }
        return result.getString("openid");
    }

    /**
     * 获取session_key
     */
    public String getSessionKey(String code) {
        JSONObject result = code2Session(code);
        if (result == null || result.containsKey("errcode")) {
            return null;
        }
        return result.getString("session_key");
    }

    /**
     * 获取unionid（如果有）
     */
    public String getUnionId(String code) {
        JSONObject result = code2Session(code);
        if (result == null || result.containsKey("errcode")) {
            return null;
        }
        return result.getString("unionid");
    }
}