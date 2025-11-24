package com.megrez.controller;

import com.megrez.annotation.CurrentUser;
import com.megrez.result.Result;
import com.megrez.service.VideoHistoryService;
import com.megrez.vo.analytics_service.VideoHistory;
import com.megrez.vo.analytics_service.VideoWatched;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/analytics/history")
public class VideoHistoryController {

    private static final Logger log = LoggerFactory.getLogger(VideoHistoryController.class);

    private final VideoHistoryService videoHistoryService;

    public VideoHistoryController(VideoHistoryService videoHistoryService) {
        this.videoHistoryService = videoHistoryService;
    }

    /**
     * 上传播放历史记录
     *
     * @param userId  用户ID
     * @param videoId 视频ID
     * @param time    观看的时长
     * @return 空
     */
    @GetMapping("/add")
    public Result<Void> updateVideoHistory(@CurrentUser Integer userId,
                                           @RequestParam Integer videoId,
                                           @RequestParam Double time) {
        log.info("用户ID：{} 上传播放记录：视频ID:{} 时长:{}", userId, videoId, time);

        return videoHistoryService.updateVideoHistory(userId, videoId, time);
    }

    /**
     * 获取用户的历史观看记录
     *
     * @param userId 用户ID
     * @param cursor 游标，对于历史记录来说，只需要提供上次加载最后一条记录的创建时间即可。
     * @return 观看记录对象集合
     */
    @GetMapping("/records")
    public Result<List<VideoHistory>> getVideoHistory(@RequestParam("userId") Integer userId,
                                                      @RequestParam(value = "cursor", required = false) Long cursor) {
        log.info("查询历史记录，用户ID：{}", userId);
        return videoHistoryService.getVideoHistory(userId, cursor);
    }

    /**
     * 获取指定数量的历史记录
     *
     * @param userId 用户ID
     * @param count  数量
     * @return 观看记录对象集合
     */
    @GetMapping("/records/count")
    public Result<List<VideoHistory>> getVideoHistoryByCount(@RequestParam("userId") Integer userId,
                                                             @RequestParam("count") Integer count) {
        log.info("查询历史记录，用户ID：{} 数量：{}", userId, count);
        return videoHistoryService.getVideoHistoryByCount(userId, count);
    }

    /**
     * 获取用户的历史观看时长
     *
     * @param userId  用户ID
     * @param videoId 视频ID
     * @return 时长记录
     */
    @GetMapping("/watched")
    public Result<List<VideoWatched>> getVideoWatched(@RequestParam("userId") Integer userId,
                                                      @RequestParam("videoId") List<Integer> videoId) {
        log.info("查询观看时长，用户ID：{},视频ID：{}", userId, videoId);
        return videoHistoryService.getVideoWatched(userId, videoId);
    }

    @DeleteMapping("/clear")
    public Result<Void> clearVideoHistory(
            @CurrentUser Integer userId,
            @RequestParam(required = false) Integer close) {
        log.info("清空历史记录，UID:{}", userId);
        return videoHistoryService.clearVideoHistory(userId, close);
    }
}
