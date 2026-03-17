package com.wechat.match.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wechat.match.dto.response.MatchResultResponse;
import com.wechat.match.entity.MatchRecord;
import com.wechat.match.entity.User;
import com.wechat.match.mapper.MatchRecordMapper;
import com.wechat.match.mapper.UserMapper;
import com.wechat.match.service.MatchingService;
import com.wechat.match.service.UserService;
import com.wechat.match.service.TagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 匹配服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MatchingServiceImpl implements MatchingService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final UserMapper userMapper;
    private final MatchRecordMapper matchRecordMapper;
    private final UserService userService;
    private final TagService tagService;

    // Redis键定义
    private static final String MATCH_POOL_KEY = "match:pool";
    private static final String USER_MATCHES_KEY = "user:matches:";
    private static final String MATCH_DETAIL_KEY = "match:detail:";
    private static final String MATCH_EXPIRE_KEY = "match:expire:";

    // 匹配配置
    private static final int MAX_CONCURRENT_MATCHES = 5;
    private static final int MATCH_TIMEOUT_SECONDS = 30;
    private static final int MATCH_EXPIRE_DAYS = 7;

    @Override
    public boolean joinMatchPool(Long userId) {
        try {
            // 检查用户是否存在
            User user = userMapper.selectById(userId);
            if (user == null) {
                log.error("用户不存在，用户ID: {}", userId);
                return false;
            }

            // 检查用户是否已经在匹配池中
            Double score = redisTemplate.opsForZSet().score(MATCH_POOL_KEY, userId.toString());
            if (score != null) {
                log.info("用户已在匹配池中，用户ID: {}", userId);
                return true;
            }

            // 将用户加入匹配池，score为加入时间戳
            long timestamp = System.currentTimeMillis();
            boolean added = redisTemplate.opsForZSet().add(MATCH_POOL_KEY, userId.toString(), timestamp);

            if (added) {
                log.info("用户加入匹配池成功，用户ID: {}", userId);
                // 更新用户状态为匹配中
                userService.updateUserStatus(userId, 2);
                return true;
            }

            return false;
        } catch (Exception e) {
            log.error("用户加入匹配池失败，用户ID: {}", userId, e);
            return false;
        }
    }

    @Override
    public boolean leaveMatchPool(Long userId) {
        try {
            // 从匹配池中移除用户
            Long removed = redisTemplate.opsForZSet().remove(MATCH_POOL_KEY, userId.toString());
            if (removed != null && removed > 0) {
                log.info("用户离开匹配池成功，用户ID: {}", userId);
                // 更新用户状态为正常
                userService.updateUserStatus(userId, 1);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("用户离开匹配池失败，用户ID: {}", userId, e);
            return false;
        }
    }

    @Override
    public Integer getMatchStatus(Long userId) {
        // 检查用户是否在匹配池中
        Double score = redisTemplate.opsForZSet().score(MATCH_POOL_KEY, userId.toString());
        if (score != null) {
            return 2; // 匹配中
        }

        // 检查用户是否有活跃匹配
        List<MatchRecord> activeMatches = getActiveMatches(userId);
        if (!activeMatches.isEmpty()) {
            return 3; // 聊天中
        }

        return 1; // 正常状态
    }

    @Override
    @Transactional
    public List<MatchResultResponse> matchUsers(Long userId) {
        List<MatchResultResponse> results = new ArrayList<>();

        try {
            // 1. 获取用户当前活跃匹配数
            List<MatchRecord> activeMatches = getActiveMatches(userId);
            int currentMatches = activeMatches.size();

            if (currentMatches >= MAX_CONCURRENT_MATCHES) {
                log.info("用户已达到最大匹配数，用户ID: {}", userId);
                return results;
            }

            // 2. 计算需要匹配的数量
            int needMatches = MAX_CONCURRENT_MATCHES - currentMatches;

            // 3. 从匹配池中获取其他用户
            Set<Object> poolMembers = redisTemplate.opsForZSet().range(MATCH_POOL_KEY, 0, -1);
            if (poolMembers == null || poolMembers.size() <= 1) {
                log.info("匹配池中没有其他用户，用户ID: {}", userId);
                return results;
            }

            // 4. 过滤掉自己和已经匹配的用户
            List<Long> candidateUserIds = poolMembers.stream()
                    .map(obj -> Long.parseLong(obj.toString()))
                    .filter(id -> !id.equals(userId))
                    .filter(id -> !isAlreadyMatched(userId, id))
                    .collect(Collectors.toList());

            // 5. 基于标签匹配选择用户
            List<Long> matchedUserIds = selectUsersByTagMatch(userId, candidateUserIds, needMatches);

            // 6. 为每个匹配创建记录
            for (Long matchedUserId : matchedUserIds) {
                MatchResultResponse result = createMatchRecord(userId, matchedUserId);
                if (result != null) {
                    results.add(result);
                }
            }

            // 7. 如果匹配成功，将用户从匹配池中移除
            if (!results.isEmpty()) {
                redisTemplate.opsForZSet().remove(MATCH_POOL_KEY, userId.toString());
                userService.updateUserStatus(userId, 1); // 更新为正常状态
            }

            log.info("用户匹配成功，用户ID: {}, 匹配到 {} 个用户", userId, results.size());
            return results;

        } catch (Exception e) {
            log.error("用户匹配失败，用户ID: {}", userId, e);
            throw new RuntimeException("匹配失败: " + e.getMessage());
        }
    }

    @Override
    public List<MatchRecord> getUserMatches(Long userId) {
        LambdaQueryWrapper<MatchRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MatchRecord::getUserId1, userId)
                .or().eq(MatchRecord::getUserId2, userId);
        queryWrapper.orderByDesc(MatchRecord::getMatchTime);
        return matchRecordMapper.selectList(queryWrapper);
    }

    @Override
    public List<MatchRecord> getActiveMatches(Long userId) {
        LambdaQueryWrapper<MatchRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.and(wrapper ->
                wrapper.eq(MatchRecord::getUserId1, userId)
                        .or().eq(MatchRecord::getUserId2, userId))
                .in(MatchRecord::getStatus, Arrays.asList(1, 2)); // 匹配中或聊天中
        queryWrapper.orderByDesc(MatchRecord::getMatchTime);
        return matchRecordMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional
    public boolean submitFeedback(Long userId, String matchId, Integer satisfaction) {
        try {
            MatchRecord matchRecord = getMatchRecord(matchId);
            if (matchRecord == null) {
                log.error("匹配记录不存在，matchId: {}", matchId);
                return false;
            }

            // 确定用户是user1还是user2
            boolean isUser1 = matchRecord.getUserId1().equals(userId);
            boolean isUser2 = matchRecord.getUserId2().equals(userId);

            if (!isUser1 && !isUser2) {
                log.error("用户不是该匹配的参与者，userId: {}, matchId: {}", userId, matchId);
                return false;
            }

            // 更新满意度
            if (isUser1) {
                matchRecord.setSatisfactionUser1(satisfaction);
            } else {
                matchRecord.setSatisfactionUser2(satisfaction);
            }

            // 如果双方都满意，更新状态为已满意
            if (matchRecord.getSatisfactionUser1() != null && matchRecord.getSatisfactionUser2() != null) {
                if (matchRecord.getSatisfactionUser1() == 1 && matchRecord.getSatisfactionUser2() == 1) {
                    matchRecord.setStatus(3); // 已满意
                } else {
                    matchRecord.setStatus(4); // 已删除（有不满意的一方）
                }
            }

            matchRecordMapper.updateById(matchRecord);
            log.info("满意度反馈提交成功，userId: {}, matchId: {}, satisfaction: {}", userId, matchId, satisfaction);
            return true;

        } catch (Exception e) {
            log.error("满意度反馈提交失败，userId: {}, matchId: {}", userId, matchId, e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean exchangeWeChat(Long userId1, Long userId2, String matchId) {
        try {
            MatchRecord matchRecord = getMatchRecord(matchId);
            if (matchRecord == null) {
                log.error("匹配记录不存在，matchId: {}", matchId);
                return false;
            }

            // 验证用户
            if (!matchRecord.getUserId1().equals(userId1) || !matchRecord.getUserId2().equals(userId2)) {
                log.error("用户不匹配该记录，userId1: {}, userId2: {}, matchId: {}", userId1, userId2, matchId);
                return false;
            }

            // 检查双方是否都满意
            if (matchRecord.getSatisfactionUser1() != 1 || matchRecord.getSatisfactionUser2() != 1) {
                log.error("双方未都满意，不能交换微信，matchId: {}", matchId);
                return false;
            }

            // 更新匹配记录
            matchRecord.setWechatExchanged(1);
            matchRecordMapper.updateById(matchRecord);

            log.info("微信交换成功，userId1: {}, userId2: {}, matchId: {}", userId1, userId2, matchId);
            return true;

        } catch (Exception e) {
            log.error("微信交换失败，userId1: {}, userId2: {}, matchId: {}", userId1, userId2, matchId, e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean deleteMatch(Long userId, String matchId) {
        try {
            MatchRecord matchRecord = getMatchRecord(matchId);
            if (matchRecord == null) {
                log.error("匹配记录不存在，matchId: {}", matchId);
                return false;
            }

            // 验证用户
            if (!matchRecord.getUserId1().equals(userId) && !matchRecord.getUserId2().equals(userId)) {
                log.error("用户不是该匹配的参与者，userId: {}, matchId: {}", userId, matchId);
                return false;
            }

            // 更新状态为已删除
            matchRecord.setStatus(4);
            matchRecordMapper.updateById(matchRecord);

            log.info("匹配删除成功，userId: {}, matchId: {}", userId, matchId);
            return true;

        } catch (Exception e) {
            log.error("匹配删除失败，userId: {}, matchId: {}", userId, matchId, e);
            return false;
        }
    }

    @Override
    public boolean isMatchExpired(String matchId) {
        MatchRecord matchRecord = getMatchRecord(matchId);
        if (matchRecord == null) {
            return true;
        }
        return matchRecord.getExpireTime().isBefore(LocalDateTime.now());
    }

    @Override
    @Transactional
    public int cleanupExpiredMatches() {
        try {
            LambdaQueryWrapper<MatchRecord> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.lt(MatchRecord::getExpireTime, LocalDateTime.now())
                    .in(MatchRecord::getStatus, Arrays.asList(1, 2, 3)); // 只清理未过期的

            List<MatchRecord> expiredMatches = matchRecordMapper.selectList(queryWrapper);
            int count = expiredMatches.size();

            // 批量更新状态为已过期
            for (MatchRecord match : expiredMatches) {
                match.setStatus(5);
                matchRecordMapper.updateById(match);
            }

            log.info("清理过期匹配完成，共清理 {} 条记录", count);
            return count;

        } catch (Exception e) {
            log.error("清理过期匹配失败", e);
            return 0;
        }
    }

    @Override
    public int getMatchPoolSize() {
        Long size = redisTemplate.opsForZSet().size(MATCH_POOL_KEY);
        return size != null ? size.intValue() : 0;
    }

    @Override
    public void resetMatchPool() {
        redisTemplate.delete(MATCH_POOL_KEY);
        log.info("匹配池已重置");
    }

    // ========== 私有方法 ==========

    private MatchResultResponse createMatchRecord(Long userId1, Long userId2) {
        try {
            // 生成匹配ID
            String matchId = UUID.randomUUID().toString().replace("-", "");

            // 创建匹配记录
            MatchRecord matchRecord = new MatchRecord();
            matchRecord.setMatchId(matchId);
            matchRecord.setUserId1(userId1);
            matchRecord.setUserId2(userId2);
            matchRecord.setMatchTime(LocalDateTime.now());
            matchRecord.setStatus(2); // 聊天中
            matchRecord.setExpireTime(LocalDateTime.now().plusDays(MATCH_EXPIRE_DAYS));

            matchRecordMapper.insert(matchRecord);

            // 获取匹配用户信息
            User matchedUser = userMapper.selectById(userId2);

            // 创建响应对象
            MatchResultResponse response = new MatchResultResponse();
            response.setMatchId(matchId);
            response.setMatchedUserId(userId2);
            if (matchedUser != null) {
                response.setMatchedUserNickname(matchedUser.getNickname());
                response.setMatchedUserAvatar(matchedUser.getAvatarUrl());
                response.setMatchedUserGender(matchedUser.getGender());
            }
            response.setMatchTime(matchRecord.getMatchTime());
            response.setExpireTime(matchRecord.getExpireTime());
            response.setStatus(matchRecord.getStatus());

            return response;

        } catch (Exception e) {
            log.error("创建匹配记录失败，userId1: {}, userId2: {}", userId1, userId2, e);
            return null;
        }
    }

    private boolean isAlreadyMatched(Long userId1, Long userId2) {
        LambdaQueryWrapper<MatchRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.and(wrapper ->
                wrapper.eq(MatchRecord::getUserId1, userId1)
                        .eq(MatchRecord::getUserId2, userId2)
                        .or()
                        .eq(MatchRecord::getUserId1, userId2)
                        .eq(MatchRecord::getUserId2, userId1))
                .in(MatchRecord::getStatus, Arrays.asList(1, 2, 3)); // 匹配中、聊天中、已满意
        return matchRecordMapper.selectCount(queryWrapper) > 0;
    }

    private MatchRecord getMatchRecord(String matchId) {
        LambdaQueryWrapper<MatchRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MatchRecord::getMatchId, matchId);
        return matchRecordMapper.selectOne(queryWrapper);
    }

    /**
     * 基于标签匹配选择用户
     * @param userId 当前用户ID
     * @param candidateUserIds 候选用户ID列表
     * @param needMatches 需要匹配的数量
     * @return 匹配的用户ID列表
     */
    private List<Long> selectUsersByTagMatch(Long userId, List<Long> candidateUserIds, int needMatches) {
        if (candidateUserIds.isEmpty() || needMatches <= 0) {
            return Collections.emptyList();
        }

        // 使用标签服务计算匹配分数
        Map<Long, Integer> matchScores = tagService.findUsersByTagMatch(userId, candidateUserIds);

        // 检查是否有非零分数
        boolean allZeroScores = matchScores.values().stream().allMatch(score -> score == 0);

        if (allZeroScores) {
            // 所有分数都为0（可能用户都没有标签），随机选择用户
            log.info("所有候选用户标签匹配分数为0，使用随机选择，用户ID: {}", userId);
            Collections.shuffle(candidateUserIds);
            return candidateUserIds.stream()
                    .limit(needMatches)
                    .collect(Collectors.toList());
        }

        // 按匹配分数降序排序
        List<Map.Entry<Long, Integer>> sortedEntries = matchScores.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .collect(Collectors.toList());

        // 选择前needMatches个用户
        return sortedEntries.stream()
                .limit(needMatches)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}