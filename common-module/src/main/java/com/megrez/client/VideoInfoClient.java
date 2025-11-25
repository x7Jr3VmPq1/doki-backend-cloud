package com.megrez.client;

import com.megrez.annotation.CurrentUser;
import com.megrez.mysql_entity.Video;
import com.megrez.result.Result;
import com.megrez.vo.video_info_service.VideoVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

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

    /**
     * 根据视频id集合批量获取视频元数据
     *
     * @param vid id集合
     * @return 视频元数据列表
     */
    @GetMapping("/multiGet")
    Result<List<Video>> getVideoInfoByIds(@RequestParam("vid") List<Integer> vid);


    /**
     * 根据视频id获取视频视图信息
     *
     * @param vid 视频id
     * @return
     */
    @GetMapping("/v2")
    Result<VideoVO> getVideoInfoByIdV2(@RequestParam("vid") Integer vid);
}
