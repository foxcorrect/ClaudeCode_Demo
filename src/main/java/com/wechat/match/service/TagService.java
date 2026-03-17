package com.wechat.match.service;

import com.wechat.match.entity.Tag;
import com.wechat.match.entity.UserTag;

import java.util.List;
import java.util.Map;

/**
 * 标签服务接口
 */
public interface TagService {

    // ========== 标签管理 ==========

    /**
     * 获取所有可用标签
     */
    List<Tag> getAllTags();

    /**
     * 根据分类获取标签
     */
    List<Tag> getTagsByCategory(String category);

    /**
     * 根据类型获取标签
     */
    List<Tag> getTagsByType(Integer type);

    /**
     * 搜索标签
     */
    List<Tag> searchTags(String keyword);

    /**
     * 创建标签
     */
    boolean createTag(Tag tag);

    /**
     * 更新标签
     */
    boolean updateTag(Tag tag);

    /**
     * 删除标签
     */
    boolean deleteTag(Long tagId);

    // ========== 用户标签管理 ==========

    /**
     * 获取用户标签列表
     */
    List<UserTag> getUserTags(Long userId);

    /**
     * 获取用户标签ID列表
     */
    List<Long> getUserTagIds(Long userId);

    /**
     * 添加用户标签
     */
    boolean addUserTag(Long userId, Long tagId, Integer weight);

    /**
     * 批量添加用户标签
     */
    boolean batchAddUserTags(Long userId, List<Long> tagIds, Integer weight);

    /**
     * 移除用户标签
     */
    boolean removeUserTag(Long userId, Long tagId);

    /**
     * 清空用户标签
     */
    boolean clearUserTags(Long userId);

    /**
     * 更新用户标签权重
     */
    boolean updateUserTagWeight(Long userId, Long tagId, Integer weight);

    // ========== 标签匹配计算 ==========

    /**
     * 计算两个用户的标签匹配度
     * @return 匹配度分数（0-100）
     */
    int calculateTagMatchScore(Long userId1, Long userId2);

    /**
     * 获取用户的标签匹配推荐用户
     */
    List<Long> getTagMatchRecommendations(Long userId, int limit);

    /**
     * 根据标签匹配用户
     */
    Map<Long, Integer> findUsersByTagMatch(Long userId, List<Long> candidateUserIds);

    /**
     * 获取共同标签数量
     */
    int getCommonTagCount(Long userId1, Long userId2);

    /**
     * 获取标签相似度最高的用户
     */
    List<Long> getMostSimilarUsers(Long userId, int limit);
}