package com.megrez.service;

import com.megrez.mapper.VideoMapper;
import com.megrez.result.Result;
import org.springframework.stereotype.Service;

@Service
public class RandomService {

    private final VideoMapper videoMapper;

    public RandomService(VideoMapper videoMapper) {
        this.videoMapper = videoMapper;
    }

    /**
     * 随机返回五个视频。
     *
     * @return 结果集
     */
    public Result randomVideo() {
        return Result.success(videoMapper.getRandomVideo());
    }
}
