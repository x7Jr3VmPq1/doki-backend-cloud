package com.megrez.controller;

import com.megrez.annotation.CurrentUser;
import com.megrez.result.Response;
import com.megrez.result.Result;
import com.megrez.service.LikeService;
import com.megrez.service.RecordService;
import com.megrez.vo.like_favortite_service.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/like")
public class LikeController {

    private final LikeService likeService;

    private static final Logger log = LoggerFactory.getLogger(LikeController.class);

    public LikeController(LikeService likeService, RecordService recordService) {
        this.likeService = likeService;
    }


    /**
     * 为指定视频添加点赞记录
     *
     * @param userId  用户ID
     * @param videoId 视频ID
     * @return result
     */
    @GetMapping
    public Result<Void> addLikeRecord(
            @CurrentUser Integer userId,
            @RequestParam("videoId") Integer videoId) {
        log.info("用户：{} 添加点赞记录，video_id：{}", userId, videoId);
        return likeService.addLikeRecord(userId, videoId);
    }

    /**
     * 判断用户是否点赞了某个视频
     *
     * @param userId  用户ID
     * @param videoId 视频ID
     * @return 判断结果
     */
    @GetMapping("/exist")
    public Result<Boolean> existLikeRecord(
            @RequestParam Integer userId,
            @RequestParam Integer videoId
    ) {
        // 基本校验
        if (userId <= 0 || videoId <= 0) {
            return Result.error(Response.PARAMS_WRONG);
        }
        return likeService.existLikeRecord(userId, videoId);
    }
}
