package com.megrez.controller;

import com.megrez.entity.VideoLikes;
import com.megrez.result.Result;
import com.megrez.service.RecordService;
import com.megrez.vo.CursorLoad;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 获取用户对视频的点赞记录
 */
@RestController
@RequestMapping("/like")
public class RecordController {
    private static final Logger log = LoggerFactory.getLogger(RecordController.class);

    private final RecordService recordService;

    public RecordController(RecordService recordService) {
        this.recordService = recordService;
    }

    @GetMapping("/records")
    public Result<CursorLoad<VideoLikes>> getRecordsByUserId(Integer userId, String cursor) throws Exception {
        log.info("获取用户点赞记录，ID：{}", userId);
        return recordService.getRecordsByUserId(userId,cursor);
    }
}
