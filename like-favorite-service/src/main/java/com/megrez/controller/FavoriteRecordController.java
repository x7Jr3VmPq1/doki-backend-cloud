package com.megrez.controller;

import com.megrez.mysql_entity.VideoFavorites;
import com.megrez.result.Result;
import com.megrez.service.FavoriteService;
import com.megrez.service.RecordService;
import com.megrez.vo.CursorLoad;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 获取用户对视频的收藏记录
 */
@RestController
@RequestMapping("/favorite")
public class FavoriteRecordController {
    private static final Logger log = LoggerFactory.getLogger(FavoriteRecordController.class);
    private final RecordService recordService;

    public FavoriteRecordController(FavoriteService favoriteService, RecordService recordService) {
        this.recordService = recordService;
    }

    /**
     * 游标获取收藏记录
     *
     * @param uid 用户id
     * @param cursor 游标
     * @return 收藏列表
     * @throws Exception
     */
    @GetMapping("/records")
    public Result<CursorLoad<VideoFavorites>> getFavoriteRecordsByUserId(Integer uid, String cursor) throws Exception {
        log.info("获取收藏记录:{}", uid);
        return recordService.getFavoriteRecordsByUserId(uid, cursor);
    }


}
