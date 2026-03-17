package com.wechat.match.controller;

import com.wechat.match.dto.response.ApiResponse;
import com.wechat.match.entity.Tag;
import com.wechat.match.entity.UserTag;
import com.wechat.match.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 标签管理控制器
 */
@RestController
@RequestMapping("/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    /**
     * 获取所有可用标签
     */
    @GetMapping
    public ApiResponse<List<Tag>> getAllTags() {
        try {
            List<Tag> tags = tagService.getAllTags();
            return ApiResponse.success(tags);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 根据分类获取标签
     */
    @GetMapping("/category/{category}")
    public ApiResponse<List<Tag>> getTagsByCategory(@PathVariable String category) {
        try {
            List<Tag> tags = tagService.getTagsByCategory(category);
            return ApiResponse.success(tags);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 根据类型获取标签
     */
    @GetMapping("/type/{type}")
    public ApiResponse<List<Tag>> getTagsByType(@PathVariable Integer type) {
        try {
            List<Tag> tags = tagService.getTagsByType(type);
            return ApiResponse.success(tags);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 搜索标签
     */
    @GetMapping("/search")
    public ApiResponse<List<Tag>> searchTags(@RequestParam String keyword) {
        try {
            List<Tag> tags = tagService.searchTags(keyword);
            return ApiResponse.success(tags);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取用户标签列表
     */
    @GetMapping("/user/{userId}")
    public ApiResponse<List<UserTag>> getUserTags(@PathVariable Long userId) {
        try {
            List<UserTag> userTags = tagService.getUserTags(userId);
            return ApiResponse.success(userTags);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取用户标签ID列表
     */
    @GetMapping("/user/{userId}/ids")
    public ApiResponse<List<Long>> getUserTagIds(@PathVariable Long userId) {
        try {
            List<Long> tagIds = tagService.getUserTagIds(userId);
            return ApiResponse.success(tagIds);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 添加用户标签
     */
    @PostMapping("/user/{userId}")
    public ApiResponse<Void> addUserTag(
            @PathVariable Long userId,
            @RequestParam Long tagId,
            @RequestParam(defaultValue = "1") Integer weight) {
        try {
            boolean success = tagService.addUserTag(userId, tagId, weight);
            if (success) {
                return ApiResponse.success("标签添加成功", null);
            } else {
                return ApiResponse.error("标签添加失败");
            }
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 批量添加用户标签
     */
    @PostMapping("/user/{userId}/batch")
    public ApiResponse<Void> batchAddUserTags(
            @PathVariable Long userId,
            @RequestBody List<Long> tagIds,
            @RequestParam(defaultValue = "1") Integer weight) {
        try {
            boolean success = tagService.batchAddUserTags(userId, tagIds, weight);
            if (success) {
                return ApiResponse.success("批量添加标签成功", null);
            } else {
                return ApiResponse.error("批量添加标签失败");
            }
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 移除用户标签
     */
    @DeleteMapping("/user/{userId}")
    public ApiResponse<Void> removeUserTag(
            @PathVariable Long userId,
            @RequestParam Long tagId) {
        try {
            boolean success = tagService.removeUserTag(userId, tagId);
            if (success) {
                return ApiResponse.success("标签移除成功", null);
            } else {
                return ApiResponse.error("标签移除失败");
            }
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 清空用户标签
     */
    @DeleteMapping("/user/{userId}/all")
    public ApiResponse<Void> clearUserTags(@PathVariable Long userId) {
        try {
            boolean success = tagService.clearUserTags(userId);
            if (success) {
                return ApiResponse.success("用户标签清空成功", null);
            } else {
                return ApiResponse.error("用户标签清空失败");
            }
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 更新用户标签权重
     */
    @PutMapping("/user/{userId}")
    public ApiResponse<Void> updateUserTagWeight(
            @PathVariable Long userId,
            @RequestParam Long tagId,
            @RequestParam Integer weight) {
        try {
            boolean success = tagService.updateUserTagWeight(userId, tagId, weight);
            if (success) {
                return ApiResponse.success("标签权重更新成功", null);
            } else {
                return ApiResponse.error("标签权重更新失败");
            }
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 计算两个用户的标签匹配度
     */
    @GetMapping("/match-score")
    public ApiResponse<Integer> calculateTagMatchScore(
            @RequestParam Long userId1,
            @RequestParam Long userId2) {
        try {
            int score = tagService.calculateTagMatchScore(userId1, userId2);
            return ApiResponse.success(score);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取用户的标签匹配推荐用户
     */
    @GetMapping("/recommendations/{userId}")
    public ApiResponse<List<Long>> getTagMatchRecommendations(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<Long> recommendations = tagService.getTagMatchRecommendations(userId, limit);
            return ApiResponse.success(recommendations);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}