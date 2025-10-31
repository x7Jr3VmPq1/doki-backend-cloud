package com.megrez.client;

import com.megrez.mysql_entity.VideoStatistics;
import com.megrez.result.Result;
import com.megrez.vo.analytics_service.VideoHistory;
import com.megrez.vo.analytics_service.VideoWatched;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 分析服务客户端
 * 提供数据分析相关的远程调用接口
 */
@FeignClient(name = "analytics-service", path = "/analytics")
public interface AnalyticsServiceClient {

    /**
     * 根据视频ID列表批量获取视频统计数据
     *
     * @param ids 视频ID列表
     * @return 视频统计数据结果
     */
    @PostMapping("/stat/videos")
    Result<List<VideoStatistics>> getVideoStatById(@RequestBody List<Integer> ids);

    /**
     * 获取用户的历史观看记录
     *
     * @param userId 用户ID
     * @return 观看记录对象集合
     */
    @GetMapping("/history/records")
    Result<List<VideoHistory>> getVideoHistory(@RequestParam("userId") Integer userId,
                                               @RequestParam(value = "cursor", required = false) Long cursor);

    /**
     * 获取用户的历史观看时长
     *
     * @param userId  用户ID
     * @param videoId 视频ID
     * @return 时长记录
     */
    @GetMapping("/history/watched")
    Result<List<VideoWatched>> getVideoWatched(@RequestParam("userId") Integer userId,
                                               @RequestParam("videoId") List<Integer> videoId);
}
