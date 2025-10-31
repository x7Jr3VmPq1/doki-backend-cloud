package com.megrez.controller;

import com.megrez.annotation.CurrentUser;
import com.megrez.dto.social_service.CheckFollowDTO;
import com.megrez.mysql_entity.UserFollow;
import com.megrez.result.Result;
import com.megrez.service.FollowService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 处理用户关注的接口
 */
@RestController
@RequestMapping("/social")
public class FollowController {

    private static final Logger log = LoggerFactory.getLogger(FollowController.class);
    private final FollowService followService;

    public FollowController(FollowService followService) {
        this.followService = followService;
    }

    /**
     * 关注操作。
     *
     * @param userId    用户id。
     * @param targetUid 目标id。
     * @return 操作结果
     */
    @GetMapping("/follow")
    public Result<Void> follow(@CurrentUser Integer userId,
                               @RequestParam("tid") Integer targetUid) {
        log.info("用户ID：{} 关注用户ID：{}", userId, targetUid);
        return followService.follow(userId, targetUid);
    }

    /**
     * 取关操作。
     *
     * @param userId    用户id。
     * @param targetUid 目标id。
     * @return 操作结果
     */
    @GetMapping("/unFollow")
    public Result<Void> unFollow(@CurrentUser Integer userId,
                                 @RequestParam("tid") Integer targetUid) {
        log.info("用户ID: {} 取消关注用户ID:{}", userId, targetUid);

        return followService.unFollow(userId, targetUid);
    }

    /**
     * 批量查询当前用户是否对目标用户有关注关系。
     * 提供一个ID列表，如果存在对这些用户的关注，则把关注的信息返回。
     *
     * @param dto 查询DTO，包含当前用户id，要查询的目标id
     * @return 结果集，包含当前用户的关注列表。
     */
    @PostMapping("/follow/check")
    public Result<List<UserFollow>> checkFollow(@RequestBody CheckFollowDTO dto) {
        log.info("查询关注关系：{}", dto);
        return followService.checkFollow(dto.getUid(), dto.getTargetIds());
    }
}
