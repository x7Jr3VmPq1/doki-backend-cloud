package com.megrez.controller;

import com.megrez.annotation.CurrentUser;
import com.megrez.result.Result;
import com.megrez.service.VideoHistoryService;
import com.megrez.vo.analytics_service.VideoHistoryVO;
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
    @PostMapping
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
     * @return 观看记录对象集合
     */
    @GetMapping
    public Result<List<VideoHistoryVO>> getVideoHistory(Integer userId) {
        return videoHistoryService.getVideoHistory(userId);
    }
}
