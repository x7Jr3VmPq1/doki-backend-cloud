package com.megrez.controller;

import com.megrez.annotation.CurrentUser;
import com.megrez.mysql_entity.Video;
import com.megrez.result.Result;
import com.megrez.service.VideoInfoService;
import com.megrez.vo.CursorLoad;
import com.megrez.vo.video_info_service.VideoVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
    public Result<Video> getVideoInfoById(@CurrentUser(required = false) Integer userId, @RequestParam Integer videoId) {
        log.info("查询视频信息ID：{}", videoId);
        return videoInfoService.getVideoInfo(userId, videoId);
    }

    /**
     * 根据视频id批量获取视频元数据
     *
     * @param vid 视频id集合
     * @return 视频元数据
     */
    @GetMapping("/multiGet")
    Result<List<Video>> getVideoInfoByIds(@RequestParam("vid") List<Integer> vid) {
        log.info("批量查询视频信息ID：{}", vid);
        return videoInfoService.getVideoInfoByIds(vid);
    }

    /**
     * 获取用户的所有作品
     *
     * @param tid 目标用户ID
     * @return 用户作品集合
     */
    @GetMapping("/all")
    public Result<CursorLoad<VideoVO>> getVideosInfoByUserId(@CurrentUser(required = false) Integer userId,
                                                             @RequestParam Integer tid,
                                                             @RequestParam(required = false) String cursor) throws Exception {
        log.info("获取用户视频信息：{}", tid);
        return videoInfoService.getVideosInfoByUserId(userId, tid, cursor);
    }

    /**
     * 获取用户点过赞的视频。
     *
     * @param tid    目标用户ID
     * @param cursor 分页游标
     * @return 用户点赞视频集合
     */
    @GetMapping("/likes")
    public Result<CursorLoad<VideoVO>> getLikeVideosInfoByUserId(@CurrentUser(required = false) Integer uid,
                                                                 @RequestParam Integer tid,
                                                                 @RequestParam(required = false) String cursor) {
        log.info("获取用户点赞视频信息：{}", tid);
        return videoInfoService.getLikeVideosInfoByUserId(uid, tid, cursor);
    }
//
//    /**
//     * 获取用户收藏的视频
//     *
//     * @param targetUid 目标用户ID
//     * @return 用户收藏视频集合
//     */
//    @GetMapping("/favorites")
//    public Result<List<Video>> getFavoriteInfoByUserId(@CurrentUser(required = false) Integer userId, @RequestParam Integer targetUid) {
//        log.info("获取用户收藏视频信息：{}", targetUid);
//        return videoInfoService.getFavoriteInfoByUserId(userId, targetUid);
//    }
//

    /**
     * 获取用户的历史观看记录
     *
     * @param userId 用户ID
     * @param cursor 游标，对于历史记录来说，只需提供时间即可。
     * @return 历史记录集合
     */
    @GetMapping("/history")
    public Result<CursorLoad<VideoVO>> getHistoryInfoByUserId(@CurrentUser Integer userId,
                                                              @RequestParam(required = false) Long cursor) {
        log.info("获取用户历史记录信息：{}", userId);
        return videoInfoService.getHistoryInfoByUserId(userId, cursor);
    }
}
