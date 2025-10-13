package com.megrez.controller;

import com.megrez.entity.UserStatistics;
import com.megrez.entity.VideoStatistics;
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

    @PostMapping("/videos")
    public Result<List<VideoStatistics>> getVideoStatById(@RequestBody List<Integer> ids) {
        log.info("查询视频统计信息：{}", ids);
        return dataService.getVideoStatById(ids);
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
