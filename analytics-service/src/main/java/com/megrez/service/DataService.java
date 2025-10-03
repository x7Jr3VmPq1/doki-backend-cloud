package com.megrez.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.megrez.entity.VideoStatistics;
import com.megrez.mapper.StatisticsMapper;
import com.megrez.result.Result;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class DataService {

    private final StatisticsMapper statisticsMapper;

    public DataService(StatisticsMapper statisticsMapper) {
        this.statisticsMapper = statisticsMapper;
    }

    /**
     * 根据视频ID批量查询统计信息
     *
     * @param ids 视频ID集合
     * @return 结果集
     */
    public Result<List<VideoStatistics>> getVideoStatById(List<Integer> ids) {
        List<VideoStatistics> statisticsList = statisticsMapper.selectList(
                new LambdaQueryWrapper<VideoStatistics>().in(VideoStatistics::getVideoId, ids)
        );
        return Result.success(statisticsList);
    }
}
