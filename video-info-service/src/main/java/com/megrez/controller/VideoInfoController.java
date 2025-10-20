package com.megrez.controller;

import com.megrez.entity.Video;
import com.megrez.result.Result;
import com.megrez.service.VideoInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/video/info")
public class VideoInfoController {

    private static final Logger log = LoggerFactory.getLogger(VideoInfoController.class);
    private final VideoInfoService videoInfoService;

    public VideoInfoController(VideoInfoService videoInfoService) {
        this.videoInfoService = videoInfoService;
    }

    /**
     * 根据视频id获取视频元数据
     *
     * @param videoId 视频Id
     * @return 视频元数据
     */
    @GetMapping
    public Result<Video> getVideoInfo(Integer videoId) {
        log.info("查询视频信息ID：{}", videoId);
        return videoInfoService.getVideoInfo(videoId);
    }
}
