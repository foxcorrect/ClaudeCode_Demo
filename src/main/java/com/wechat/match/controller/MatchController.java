package com.wechat.match.controller;

import com.wechat.match.dto.response.ApiResponse;
import com.wechat.match.dto.response.MatchResultResponse;
import com.wechat.match.entity.MatchRecord;
import com.wechat.match.service.MatchingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 匹配控制器
 */
@RestController
@RequestMapping("/match")
@RequiredArgsConstructor
public class MatchController {

    private final MatchingService matchingService;

    /**
     * 加入匹配池
     */
    @PostMapping("/join")
    public ApiResponse<Void> joinMatchPool(@RequestParam Long userId) {
        try {
            boolean success = matchingService.joinMatchPool(userId);
            if (success) {
                return ApiResponse.success("加入匹配池成功", null);
            } else {
                return ApiResponse.error("加入匹配池失败");
            }
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 离开匹配池
     */
    @PostMapping("/leave")
    public ApiResponse<Void> leaveMatchPool(@RequestParam Long userId) {
        try {
            boolean success = matchingService.leaveMatchPool(userId);
            if (success) {
                return ApiResponse.success("离开匹配池成功", null);
            } else {
                return ApiResponse.error("离开匹配池失败");
            }
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取匹配状态
     */
    @GetMapping("/status")
    public ApiResponse<Integer> getMatchStatus(@RequestParam Long userId) {
        try {
            Integer status = matchingService.getMatchStatus(userId);
            return ApiResponse.success(status);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 执行匹配（为用户匹配5个对象）
     */
    @PostMapping("/execute")
    public ApiResponse<List<MatchResultResponse>> executeMatch(@RequestParam Long userId) {
        try {
            List<MatchResultResponse> results = matchingService.matchUsers(userId);
            return ApiResponse.success("匹配成功", results);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取用户匹配记录
     */
    @GetMapping("/records")
    public ApiResponse<List<MatchRecord>> getUserMatches(@RequestParam Long userId) {
        try {
            List<MatchRecord> matches = matchingService.getUserMatches(userId);
            return ApiResponse.success(matches);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取活跃匹配记录
     */
    @GetMapping("/active")
    public ApiResponse<List<MatchRecord>> getActiveMatches(@RequestParam Long userId) {
        try {
            List<MatchRecord> matches = matchingService.getActiveMatches(userId);
            return ApiResponse.success(matches);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 提交满意度反馈
     */
    @PostMapping("/feedback")
    public ApiResponse<Void> submitFeedback(
            @RequestParam Long userId,
            @RequestParam String matchId,
            @RequestParam Integer satisfaction) {
        try {
            boolean success = matchingService.submitFeedback(userId, matchId, satisfaction);
            if (success) {
                return ApiResponse.success("满意度反馈提交成功", null);
            } else {
                return ApiResponse.error("满意度反馈提交失败");
            }
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 交换微信
     */
    @PostMapping("/exchange-wechat")
    public ApiResponse<Void> exchangeWeChat(
            @RequestParam Long userId1,
            @RequestParam Long userId2,
            @RequestParam String matchId) {
        try {
            boolean success = matchingService.exchangeWeChat(userId1, userId2, matchId);
            if (success) {
                return ApiResponse.success("微信交换成功", null);
            } else {
                return ApiResponse.error("微信交换失败");
            }
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 删除匹配
     */
    @DeleteMapping("/delete")
    public ApiResponse<Void> deleteMatch(
            @RequestParam Long userId,
            @RequestParam String matchId) {
        try {
            boolean success = matchingService.deleteMatch(userId, matchId);
            if (success) {
                return ApiResponse.success("匹配删除成功", null);
            } else {
                return ApiResponse.error("匹配删除失败");
            }
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取匹配池大小
     */
    @GetMapping("/pool-size")
    public ApiResponse<Integer> getMatchPoolSize() {
        try {
            int size = matchingService.getMatchPoolSize();
            return ApiResponse.success(size);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 清理过期匹配
     */
    @PostMapping("/cleanup")
    public ApiResponse<Integer> cleanupExpiredMatches() {
        try {
            int count = matchingService.cleanupExpiredMatches();
            return ApiResponse.success("清理完成，共清理 " + count + " 条记录", count);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}