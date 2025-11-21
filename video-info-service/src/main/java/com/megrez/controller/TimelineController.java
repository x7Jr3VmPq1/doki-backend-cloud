package com.megrez.controller;

import com.megrez.annotation.CurrentUser;
import com.megrez.mysql_entity.Video;
import com.megrez.result.Result;
import com.megrez.service.TimelineService;
import com.megrez.vo.video_info_service.VideoVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/video/info/timeline")
public class TimelineController {

    private final TimelineService timelineService;

    public TimelineController(TimelineService timelineService) {
        this.timelineService = timelineService;
    }

    /**
     * 获取关注时间线
     *
     * @param uid 用户ID
     * @return 视频列表
     */
    @GetMapping("/follow")
    public Result<List<VideoVO>> getFollowTimeline(@CurrentUser Integer uid) {
        return timelineService.getFollowTimeline(uid);
//        return null;
    }
}
