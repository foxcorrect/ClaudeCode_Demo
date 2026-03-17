package com.wechat.match.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wechat.match.dto.response.UserInfoResponse;
import com.wechat.match.entity.User;
import com.wechat.match.mapper.UserMapper;
import com.wechat.match.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 用户服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    // Redis键前缀
    private static final String ONLINE_USER_KEY = "online:user:";
    private static final String USER_MATCH_COUNT_KEY = "user:match:count:";
    private static final String AVAILABLE_USER_KEY = "available:users";

    @Override
    public User getUserById(Long userId) {
        return userMapper.selectById(userId);
    }

    @Override
    public UserInfoResponse getUserInfoResponse(Long userId) {
        User user = getUserById(userId);
        if (user == null) {
            return null;
        }
        UserInfoResponse response = new UserInfoResponse();
        BeanUtils.copyProperties(user, response);
        return response;
    }

    @Override
    public boolean updateUser(User user) {
        user.setUpdatedAt(LocalDateTime.now());
        return userMapper.updateById(user) > 0;
    }

    @Override
    public boolean updateUserStatus(Long userId, Integer status) {
        User user = new User();
        user.setId(userId);
        user.setStatus(status);
        user.setUpdatedAt(LocalDateTime.now());
        return userMapper.updateById(user) > 0;
    }

    @Override
    public boolean updateMatchCount(Long userId, Integer matchCount) {
        User user = new User();
        user.setId(userId);
        user.setMatchCount(matchCount);
        user.setUpdatedAt(LocalDateTime.now());
        return userMapper.updateById(user) > 0;
    }

    @Override
    public void resetDailyMatchCount() {
        // 重置所有用户的每日匹配次数
        User updateUser = new User();
        updateUser.setMatchCount(0);
        updateUser.setUpdatedAt(LocalDateTime.now());

        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.gt(User::getMatchCount, 0);
        userMapper.update(updateUser, queryWrapper);

        log.info("每日匹配次数已重置");
    }

    @Override
    public List<User> getOnlineUsers() {
        // 从Redis获取在线用户ID列表
        // 这里简化实现，实际可以从Redis中获取
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getStatus, 1); // 正常状态用户
        queryWrapper.orderByDesc(User::getLastLoginTime);
        queryWrapper.last("LIMIT 100");
        return userMapper.selectList(queryWrapper);
    }

    @Override
    public List<User> getAvailableUsers() {
        // 获取可匹配用户（状态正常且不在匹配中）
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getStatus, 1); // 正常状态
        queryWrapper.ne(User::getStatus, 2); // 不在匹配中
        queryWrapper.orderByDesc(User::getLastLoginTime);
        queryWrapper.last("LIMIT 50");
        return userMapper.selectList(queryWrapper);
    }

    @Override
    public Page<User> getUsersByPage(Integer pageNum, Integer pageSize) {
        Page<User> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(User::getCreatedAt);
        return userMapper.selectPage(page, queryWrapper);
    }

    @Override
    public User getUserByOpenId(String openId) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getOpenId, openId);
        return userMapper.selectOne(queryWrapper);
    }

    @Override
    public List<User> searchUsers(String keyword) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(User::getNickname, keyword)
                .or().like(User::getCity, keyword)
                .or().like(User::getProvince, keyword);
        queryWrapper.orderByDesc(User::getLastLoginTime);
        queryWrapper.last("LIMIT 20");
        return userMapper.selectList(queryWrapper);
    }

    /**
     * 标记用户在线
     */
    public void markUserOnline(Long userId) {
        String key = ONLINE_USER_KEY + userId;
        redisTemplate.opsForValue().set(key, "online", 5, TimeUnit.MINUTES); // 5分钟过期
    }

    /**
     * 标记用户离线
     */
    public void markUserOffline(Long userId) {
        String key = ONLINE_USER_KEY + userId;
        redisTemplate.delete(key);
    }

    /**
     * 检查用户是否在线
     */
    public boolean isUserOnline(Long userId) {
        String key = ONLINE_USER_KEY + userId;
        return redisTemplate.hasKey(key);
    }

    /**
     * 增加用户匹配次数
     */
    public void incrementMatchCount(Long userId) {
        String key = USER_MATCH_COUNT_KEY + userId;
        redisTemplate.opsForValue().increment(key, 1);
        // 设置过期时间为当天结束
        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
        long ttl = java.time.Duration.between(LocalDateTime.now(), endOfDay).getSeconds();
        redisTemplate.expire(key, ttl, TimeUnit.SECONDS);
    }

    /**
     * 获取用户今日匹配次数
     */
    public Integer getTodayMatchCount(Long userId) {
        String key = USER_MATCH_COUNT_KEY + userId;
        Object count = redisTemplate.opsForValue().get(key);
        return count == null ? 0 : Integer.parseInt(count.toString());
    }
}