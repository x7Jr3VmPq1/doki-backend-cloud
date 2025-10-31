package com.megrez.controller;

import com.megrez.annotation.CurrentUser;
import com.megrez.mysql_entity.UserStatistics;
import com.megrez.mysql_entity.VideoStatistics;
import com.megrez.result.Response;
import com.megrez.result.Result;
import com.megrez.service.DataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/analytics/stat")
public class DataController {

    private final DataService dataService;
    private static final Logger log = LoggerFactory.getLogger(DataController.class);

    public DataController(DataService dataService) {
        this.dataService = dataService;
    }

    /**
     * 查询指定视频的统计信息（点赞/收藏/评论数量）
     *
     * @param userId 操作用户ID（不必须）
     * @param ids    视频ID，传入数组查询:[10001]
     * @return 统计信息视图对象
     */
    @PostMapping("/videos")
    public Result<List<VideoStatistics>> getVideoStatById(
            @CurrentUser(required = false) Integer userId,
            @RequestBody List<Integer> ids) {
        log.info("用户：{}查询视频统计信息，视频id：{}", userId, ids);
        if (ids.isEmpty()) {
            return Result.success(List.of());
        }
        return dataService.getVideoStatById(ids, userId);
    }


    @GetMapping("/user")
    public Result<UserStatistics> getUserStatistics(@RequestParam("id") Integer id) {
        log.info("查询用户关注/粉丝数量：{}", id);
        if (id <= 0) {
            return Result.error(Response.USER_NOT_FOUND_WRONG);
        }
        return dataService.getUserStatistics(id);
    }
}
