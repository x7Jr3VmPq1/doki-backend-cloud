package com.megrez.service;

import com.megrez.entity.Video;
import com.megrez.mapper.VideoMapper;
import com.megrez.result.Result;
import org.springframework.stereotype.Service;

@Service
public class VideoInfoService {
    private final VideoMapper videoMapper;

    public VideoInfoService(VideoMapper videoMapper) {
        this.videoMapper = videoMapper;
    }

    public Result<Video> getVideoInfo(Integer videoId) {
        Video video = videoMapper.selectById(videoId);
        return Result.success(video);
    }
}
