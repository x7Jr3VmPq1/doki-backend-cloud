package com.megrez.client;

import com.megrez.entity.VideoStatistics;
import com.megrez.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 分析服务客户端
 * 
 * 提供数据分析相关的远程调用接口
 * 
 * @author Doki Team
 * @since 1.0.0
 */
@FeignClient(name = "analytics-service")
public interface AnalyticsServiceClient {

    /**
     * 根据视频ID列表批量获取视频统计数据
     * 
     * @param ids 视频ID列表
     * @return 视频统计数据结果
     */
    @PostMapping("/analytics/stat/videos")
    Result<List<VideoStatistics>> getVideoStatById(@RequestBody List<Integer> ids);
}
