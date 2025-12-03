package com.megrez.controller;

import com.megrez.mysql_entity.VideoLikes;
import com.megrez.result.Result;
import com.megrez.service.RecordService;
import com.megrez.vo.CursorLoad;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 获取用户对视频的点赞记录
 */
@RestController
@RequestMapping("/like")
public class LikeRecordController {
    private static final Logger log = LoggerFactory.getLogger(LikeRecordController.class);

    private final RecordService recordService;

    public LikeRecordController(RecordService recordService) {
        this.recordService = recordService;
    }

    @GetMapping("/records")
    public Result<CursorLoad<VideoLikes>> getRecordsByUserId(Integer userId, String cursor) throws Exception {
        log.info("获取用户点赞记录，ID：{}", userId);
        return recordService.getLikeRecordsByUserId(userId, cursor);
    }


    @GetMapping("/records/count")
    public Result<List<VideoLikes>> getRecordsByCount(Integer userId, Integer count) {
        log.info("获取用户点赞记录，ID:{},数量：{}", userId, count);
        return recordService.getRecordsByCount(userId, count);
    }

    /**
     * 根据用户ID和视频ID批量判断是否存在点赞
     *
     * @param uid 用户ID
     * @param vid 视频ID
     * @return 点赞记录
     */
    @GetMapping("/records/batch")
    public Result<List<VideoLikes>> getRecordsByBatchVIds(
            @RequestParam("uid") Integer uid,
            @RequestParam("vid") List<Integer> vid) {
        return recordService.getRecordsByBatchVIds(uid, vid);
    }
}
