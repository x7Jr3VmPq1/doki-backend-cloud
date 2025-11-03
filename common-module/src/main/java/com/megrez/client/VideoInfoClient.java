package com.megrez.client;

import com.megrez.mysql_entity.Video;
import com.megrez.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "video-info-service", path = "/video/info")
public interface VideoInfoClient {

    /**
     * 根据视频id获取视频元数据
     *
     * @param videoId 视频Id
     * @return 视频元数据
     */
    @GetMapping
    Result<Video> getVideoInfoById(@RequestParam("videoId") Integer videoId);
}
