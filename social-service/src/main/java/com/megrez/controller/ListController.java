package com.megrez.controller;

import com.megrez.annotation.CurrentUser;
import com.megrez.result.Response;
import com.megrez.result.Result;
import com.megrez.service.ListService;
import com.megrez.vo.social_service.UserCursorLoadVO;
import com.megrez.vo.user_service.UsersVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/social")
public class ListController {
    private static final Logger log = LoggerFactory.getLogger(ListController.class);

    private final ListService listService;

    public ListController(ListService listService) {
        this.listService = listService;
    }

    /**
     * 获取关注列表。
     * 支持游标加载。
     *
     * @param userId 用户ID
     * @param cursor 游标
     * @param mode   模式， 1=综合排序，2=最近关注，3=最早关注
     * @return 粉丝列表。
     */
    @GetMapping("/followings")
    public Result<UserCursorLoadVO> getFollowers(@CurrentUser Integer userId,
                                                 @RequestParam String cursor,
                                                 @RequestParam Integer mode) {
        if (mode < 1 || mode > 3) {
            // 过滤不正确的模式
            return Result.error(Response.PARAMS_WRONG);
        }
        log.info("用户ID：{} 拉取关注列表，模式：{}", userId, mode);
        return listService.getFollowings(userId, cursor, mode);

    }
}
