package com.wechat.match.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wechat.match.dto.request.LoginRequest;
import com.wechat.match.dto.response.UserInfoResponse;
import com.wechat.match.entity.User;
import com.wechat.match.mapper.UserMapper;
import com.wechat.match.service.WeChatAuthService;
import com.wechat.match.util.JwtTokenUtil;
import com.wechat.match.util.WeChatApiUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 微信认证服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WeChatAuthServiceImpl implements WeChatAuthService {

    private final UserMapper userMapper;
    private final WeChatApiUtil weChatApiUtil;
    private final JwtTokenUtil jwtTokenUtil;

    @Override
    @Transactional
    public String login(LoginRequest loginRequest) {
        String code = loginRequest.getCode();

        // 1. 调用微信API获取openid
        String openId = weChatApiUtil.getOpenId(code);
        if (openId == null) {
            throw new RuntimeException("微信登录失败，无效的code");
        }

        // 2. 查询用户是否存在
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getOpenId, openId);
        User user = userMapper.selectOne(queryWrapper);

        if (user == null) {
            // 3. 新用户注册
            user = new User();
            user.setOpenId(openId);
            user.setUnionId(weChatApiUtil.getUnionId(code));
            user.setStatus(1); // 正常状态
            user.setMatchCount(0);
            user.setLastLoginTime(LocalDateTime.now());

            // 如果有加密数据，可以解密获取用户信息
            if (loginRequest.getEncryptedData() != null && loginRequest.getIv() != null) {
                // 这里可以添加解密逻辑获取昵称、头像等信息
                // 暂时留空，后续可以扩展
            }

            userMapper.insert(user);
            log.info("新用户注册成功，用户ID: {}", user.getId());
        } else {
            // 4. 老用户更新最后登录时间
            user.setLastLoginTime(LocalDateTime.now());
            userMapper.updateById(user);
            log.info("用户登录成功，用户ID: {}", user.getId());
        }

        // 5. 生成JWT Token
        return jwtTokenUtil.generateToken(user.getId(), openId);
    }

    @Override
    public UserInfoResponse getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        UserInfoResponse response = new UserInfoResponse();
        BeanUtils.copyProperties(user, response);
        return response;
    }

    @Override
    public boolean updateUserInfo(Long userId, String encryptedData, String iv) {
        // 这里可以实现用户信息更新逻辑
        // 解密encryptedData获取用户信息
        // 暂时返回true
        return true;
    }

    @Override
    public String refreshToken(String oldToken) {
        if (!jwtTokenUtil.validateToken(oldToken)) {
            throw new RuntimeException("无效的Token");
        }

        Long userId = jwtTokenUtil.getUserIdFromToken(oldToken);
        String openId = jwtTokenUtil.getOpenIdFromToken(oldToken);

        if (userId == null || openId == null) {
            throw new RuntimeException("Token解析失败");
        }

        // 验证用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null || !openId.equals(user.getOpenId())) {
            throw new RuntimeException("用户不存在或Token无效");
        }

        // 生成新Token
        return jwtTokenUtil.generateToken(userId, openId);
    }

    @Override
    public boolean validateToken(String token) {
        if (!jwtTokenUtil.validateToken(token)) {
            return false;
        }

        Long userId = jwtTokenUtil.getUserIdFromToken(token);
        String openId = jwtTokenUtil.getOpenIdFromToken(token);

        if (userId == null || openId == null) {
            return false;
        }

        User user = userMapper.selectById(userId);
        return user != null && openId.equals(user.getOpenId());
    }

    @Override
    public void logout(Long userId) {
        // 这里可以实现登出逻辑，比如将Token加入黑名单
        // 暂时只记录日志
        log.info("用户登出，用户ID: {}", userId);
    }
}