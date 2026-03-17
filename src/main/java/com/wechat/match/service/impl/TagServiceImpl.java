package com.wechat.match.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wechat.match.entity.Tag;
import com.wechat.match.entity.UserTag;
import com.wechat.match.mapper.TagMapper;
import com.wechat.match.mapper.UserTagMapper;
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
 * 标签服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagMapper tagMapper;
    private final UserTagMapper userTagMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    // Redis键前缀
    private static final String USER_TAGS_KEY = "user:tags:";
    private static final String TAG_MATCH_SCORE_KEY = "tag:match:score:";

    // ========== 标签管理 ==========

    @Override
    public List<Tag> getAllTags() {
        LambdaQueryWrapper<Tag> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Tag::getStatus, 1)
                .orderByAsc(Tag::getType)
                .orderByAsc(Tag::getCategory)
                .orderByAsc(Tag::getName);
        return tagMapper.selectList(queryWrapper);
    }

    @Override
    public List<Tag> getTagsByCategory(String category) {
        LambdaQueryWrapper<Tag> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Tag::getStatus, 1)
                .eq(Tag::getCategory, category)
                .orderByAsc(Tag::getName);
        return tagMapper.selectList(queryWrapper);
    }

    @Override
    public List<Tag> getTagsByType(Integer type) {
        LambdaQueryWrapper<Tag> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Tag::getStatus, 1)
                .eq(Tag::getType, type)
                .orderByAsc(Tag::getCategory)
                .orderByAsc(Tag::getName);
        return tagMapper.selectList(queryWrapper);
    }

    @Override
    public List<Tag> searchTags(String keyword) {
        LambdaQueryWrapper<Tag> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Tag::getStatus, 1)
                .and(wrapper -> wrapper.like(Tag::getName, keyword)
                        .or().like(Tag::getDescription, keyword)
                        .or().like(Tag::getCategory, keyword))
                .orderByAsc(Tag::getType)
                .orderByAsc(Tag::getName);
        return tagMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional
    public boolean createTag(Tag tag) {
        try {
            tag.setStatus(1);
            tag.setCreatedAt(LocalDateTime.now());
            tag.setUpdatedAt(LocalDateTime.now());
            tagMapper.insert(tag);
            log.info("创建标签成功，标签ID: {}, 名称: {}", tag.getId(), tag.getName());
            return true;
        } catch (Exception e) {
            log.error("创建标签失败，名称: {}", tag.getName(), e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean updateTag(Tag tag) {
        try {
            tag.setUpdatedAt(LocalDateTime.now());
            tagMapper.updateById(tag);
            log.info("更新标签成功，标签ID: {}", tag.getId());
            return true;
        } catch (Exception e) {
            log.error("更新标签失败，标签ID: {}", tag.getId(), e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean deleteTag(Long tagId) {
        try {
            // 检查是否有用户使用该标签
            LambdaQueryWrapper<UserTag> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(UserTag::getTagId, tagId);
            long userCount = userTagMapper.selectCount(queryWrapper);
            if (userCount > 0) {
                log.error("无法删除标签，仍有 {} 个用户使用该标签，标签ID: {}", userCount, tagId);
                return false;
            }

            // 软删除：更新状态为禁用
            Tag tag = new Tag();
            tag.setId(tagId);
            tag.setStatus(0);
            tag.setUpdatedAt(LocalDateTime.now());
            tagMapper.updateById(tag);

            log.info("删除标签成功，标签ID: {}", tagId);
            return true;
        } catch (Exception e) {
            log.error("删除标签失败，标签ID: {}", tagId, e);
            return false;
        }
    }

    // ========== 用户标签管理 ==========

    @Override
    public List<UserTag> getUserTags(Long userId) {
        // 先从缓存查询
        String cacheKey = USER_TAGS_KEY + userId;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return (List<UserTag>) cached;
        }

        // 从数据库查询
        List<UserTag> userTags = userTagMapper.selectUserTagsWithDetail(userId);

        // 缓存结果（30分钟）
        redisTemplate.opsForValue().set(cacheKey, userTags, 30, TimeUnit.MINUTES);

        return userTags;
    }

    @Override
    public List<Long> getUserTagIds(Long userId) {
        return userTagMapper.selectTagIdsByUserId(userId);
    }

    @Override
    @Transactional
    public boolean addUserTag(Long userId, Long tagId, Integer weight) {
        try {
            // 检查标签是否存在且可用
            Tag tag = tagMapper.selectById(tagId);
            if (tag == null || tag.getStatus() != 1) {
                log.error("标签不存在或已禁用，标签ID: {}", tagId);
                return false;
            }

            // 检查是否已添加
            LambdaQueryWrapper<UserTag> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(UserTag::getUserId, userId)
                    .eq(UserTag::getTagId, tagId);
            UserTag existing = userTagMapper.selectOne(queryWrapper);
            if (existing != null) {
                // 已存在，更新权重
                existing.setWeight(weight);
                userTagMapper.updateById(existing);
            } else {
                // 创建新的用户标签
                UserTag userTag = new UserTag();
                userTag.setUserId(userId);
                userTag.setTagId(tagId);
                userTag.setWeight(weight != null ? weight : 1);
                userTag.setCreatedAt(LocalDateTime.now());
                userTagMapper.insert(userTag);
            }

            // 清除缓存
            clearUserTagsCache(userId);

            log.info("添加用户标签成功，用户ID: {}, 标签ID: {}, 权重: {}", userId, tagId, weight);
            return true;

        } catch (Exception e) {
            log.error("添加用户标签失败，用户ID: {}, 标签ID: {}", userId, tagId, e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean batchAddUserTags(Long userId, List<Long> tagIds, Integer weight) {
        try {
            for (Long tagId : tagIds) {
                addUserTag(userId, tagId, weight);
            }
            log.info("批量添加用户标签成功，用户ID: {}, 标签数量: {}", userId, tagIds.size());
            return true;
        } catch (Exception e) {
            log.error("批量添加用户标签失败，用户ID: {}", userId, e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean removeUserTag(Long userId, Long tagId) {
        try {
            LambdaQueryWrapper<UserTag> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(UserTag::getUserId, userId)
                    .eq(UserTag::getTagId, tagId);

            int deleted = userTagMapper.delete(queryWrapper);
            if (deleted > 0) {
                // 清除缓存
                clearUserTagsCache(userId);
                log.info("移除用户标签成功，用户ID: {}, 标签ID: {}", userId, tagId);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("移除用户标签失败，用户ID: {}, 标签ID: {}", userId, tagId, e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean clearUserTags(Long userId) {
        try {
            LambdaQueryWrapper<UserTag> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(UserTag::getUserId, userId);

            int deleted = userTagMapper.delete(queryWrapper);
            if (deleted > 0) {
                // 清除缓存
                clearUserTagsCache(userId);
                log.info("清空用户标签成功，用户ID: {}, 清理数量: {}", userId, deleted);
            }
            return true;
        } catch (Exception e) {
            log.error("清空用户标签失败，用户ID: {}", userId, e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean updateUserTagWeight(Long userId, Long tagId, Integer weight) {
        try {
            LambdaQueryWrapper<UserTag> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(UserTag::getUserId, userId)
                    .eq(UserTag::getTagId, tagId);

            UserTag userTag = userTagMapper.selectOne(queryWrapper);
            if (userTag == null) {
                log.error("用户标签不存在，用户ID: {}, 标签ID: {}", userId, tagId);
                return false;
            }

            userTag.setWeight(weight);
            userTagMapper.updateById(userTag);

            // 清除缓存
            clearUserTagsCache(userId);

            log.info("更新用户标签权重成功，用户ID: {}, 标签ID: {}, 权重: {}", userId, tagId, weight);
            return true;
        } catch (Exception e) {
            log.error("更新用户标签权重失败，用户ID: {}, 标签ID: {}", userId, tagId, e);
            return false;
        }
    }

    // ========== 标签匹配计算 ==========

    @Override
    public int calculateTagMatchScore(Long userId1, Long userId2) {
        // 先从缓存查询
        String cacheKey = TAG_MATCH_SCORE_KEY + userId1 + ":" + userId2;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return (int) cached;
        }

        // 获取两个用户的标签ID列表
        List<Long> tags1 = getUserTagIds(userId1);
        List<Long> tags2 = getUserTagIds(userId2);

        // 计算匹配度
        int score = calculateMatchScore(tags1, tags2);

        // 缓存结果（10分钟）
        redisTemplate.opsForValue().set(cacheKey, score, 10, TimeUnit.MINUTES);

        return score;
    }

    @Override
    public List<Long> getTagMatchRecommendations(Long userId, int limit) {
        // 获取用户的标签
        List<Long> userTags = getUserTagIds(userId);
        if (userTags.isEmpty()) {
            return Collections.emptyList();
        }

        // 查询具有相同标签的用户
        // 这里简化实现，实际应该使用更复杂的推荐算法
        Set<Long> recommendedUsers = new HashSet<>();

        for (Long tagId : userTags) {
            List<Long> usersWithSameTag = userTagMapper.selectUserIdsByTagId(tagId);
            recommendedUsers.addAll(usersWithSameTag);
        }

        // 移除自己
        recommendedUsers.remove(userId);

        // 计算匹配度并排序
        Map<Long, Integer> userScores = new HashMap<>();
        for (Long candidateUserId : recommendedUsers) {
            int score = calculateTagMatchScore(userId, candidateUserId);
            userScores.put(candidateUserId, score);
        }

        // 按匹配度降序排序
        return userScores.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    @Override
    public Map<Long, Integer> findUsersByTagMatch(Long userId, List<Long> candidateUserIds) {
        Map<Long, Integer> matchScores = new HashMap<>();

        for (Long candidateUserId : candidateUserIds) {
            int score = calculateTagMatchScore(userId, candidateUserId);
            matchScores.put(candidateUserId, score);
        }

        return matchScores;
    }

    @Override
    public int getCommonTagCount(Long userId1, Long userId2) {
        List<Long> tags1 = getUserTagIds(userId1);
        List<Long> tags2 = getUserTagIds(userId2);

        Set<Long> set1 = new HashSet<>(tags1);
        Set<Long> set2 = new HashSet<>(tags2);
        set1.retainAll(set2); // 交集

        return set1.size();
    }

    @Override
    public List<Long> getMostSimilarUsers(Long userId, int limit) {
        return getTagMatchRecommendations(userId, limit);
    }

    // ========== 私有方法 ==========

    /**
     * 计算两个标签列表的匹配度分数
     */
    private int calculateMatchScore(List<Long> tags1, List<Long> tags2) {
        if (tags1.isEmpty() && tags2.isEmpty()) {
            return 50; // 都没有标签，给一个基础分
        }
        if (tags1.isEmpty() || tags2.isEmpty()) {
            return 0; // 只有一方有标签，不匹配
        }

        Set<Long> set1 = new HashSet<>(tags1);
        Set<Long> set2 = new HashSet<>(tags2);

        // 计算交集
        Set<Long> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);

        // 计算并集
        Set<Long> union = new HashSet<>(set1);
        union.addAll(set2);

        // 使用Jaccard相似度计算匹配度
        if (union.isEmpty()) {
            return 0;
        }

        double jaccard = (double) intersection.size() / union.size();
        return (int) (jaccard * 100);
    }

    /**
     * 清除用户标签缓存
     */
    private void clearUserTagsCache(Long userId) {
        String cacheKey = USER_TAGS_KEY + userId;
        redisTemplate.delete(cacheKey);

        // 清除相关的匹配分数缓存（简化处理，清除所有相关缓存）
        // 实际应该更精确地清除相关缓存
        String pattern = TAG_MATCH_SCORE_KEY + userId + ":*";
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }

        pattern = TAG_MATCH_SCORE_KEY + "*:" + userId;
        keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}