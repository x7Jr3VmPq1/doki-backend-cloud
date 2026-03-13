package com.megrez.controller;

import com.megrez.annotation.CurrentUser;
import com.megrez.mysql_entity.Video;
import com.megrez.result.Result;
import com.megrez.service.FeedService;
import com.megrez.service.RandomService;
import com.megrez.vo.video_info_service.VideoVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/feed")
public class FeedController {
    private static final Logger log = LoggerFactory.getLogger(FeedController.class);
    private final RandomService randomService;
    private final FeedService feedService;

    public FeedController(RandomService randomService, FeedService feedService) {
        this.randomService = randomService;
        this.feedService = feedService;
    }

    @GetMapping("/random")
    public Result<List<Video>> getRandomVideos() {
        return randomService.randomVideo();
    }


    @GetMapping
    public Result<List<VideoVO>> get(@CurrentUser(required = false) Integer uid) {
        log.info("获取推荐视频,UID:{}", uid);
        return Result.success(feedService.getRecommend(uid));
    }
}
