package com.megrez.controller;

import com.megrez.annotation.CurrentUser;
import com.megrez.result.Result;
import com.megrez.service.LikeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/like")
public class LikeController {

    private final LikeService likeService;

    private static final Logger log = LoggerFactory.getLogger(LikeController.class);

    public LikeController(LikeService likeService) {
        this.likeService = likeService;
    }


    @GetMapping
    public Result<Void> addLikeRecord(
            @CurrentUser Integer userId,
            @RequestParam("videoId") Integer videoId) {
        log.info("用户：{} 添加点赞记录，video_id：{}", userId, videoId);
        return likeService.addLikeRecord(userId, videoId);
    }
}
