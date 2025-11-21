package com.megrez.controller;

import com.megrez.annotation.CurrentUser;
import com.megrez.result.Response;
import com.megrez.result.Result;
import com.megrez.service.ListService;
import com.megrez.vo.CursorLoad;
import com.megrez.vo.user_service.UsersVO;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/social")
public class ListController {
    private static final Logger log = LoggerFactory.getLogger(ListController.class);

    private final ListService listService;

    public ListController(ListService listService) {
        this.listService = listService;
    }

    /**
     * 获取关注或粉丝列表。/followings = 查询关注列表，/followers = 查询粉丝列表
     * 支持游标加载。
     *
     * @param userId       当前用户ID，必须
     * @param tid          目标用户ID，必须
     * @param cursor       分页游标
     * @param mode         模式， 1=综合排序，2=最近关注，3=最早关注，必须
     * @param leastOneWork 传入大于0的参数，表示返回至少有一个作品的用户
     * @return 粉丝列表。
     */
    @GetMapping({"/followings", "/followers"})
    public Result<CursorLoad<UsersVO>> getFollowers(
            HttpServletRequest request,
            @CurrentUser Integer userId,
            @RequestParam("tid") Integer tid,
            @RequestParam(value = "leastOneWork", required = false) Integer leastOneWork,
            @RequestParam(value = "cursor", required = false) String cursor,
            @RequestParam("mode") Integer mode) throws Exception {
        if (mode < 1 || mode > 3) {
            // 过滤不正确的模式
            return Result.error(Response.PARAMS_WRONG);
        }
        String path = request.getServletPath();

        int type = path.equals("/social/followings") ? 1 : 2;

        log.info("用户ID：{} 查询用户ID：{}的 {} 列表，模式：{}", userId, tid, type == 1 ? "关注" : "粉丝", mode);
        return listService.getFollowings(type, tid, userId, cursor, mode);

    }
}
