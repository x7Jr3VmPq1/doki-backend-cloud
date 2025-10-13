package com.megrez.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.megrez.entity.UserStatistics;
import com.megrez.entity.VideoStatistics;
import com.megrez.mapper.UserStatisticsMapper;
import com.megrez.mapper.VideoStatisticsMapper;
import com.megrez.result.Response;
import com.megrez.result.Result;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DataService {

    private final VideoStatisticsMapper videoStatisticsMapper;
    private final UserStatisticsMapper userStatisticsMapper;

    public DataService(VideoStatisticsMapper statisticsMapper, UserStatisticsMapper userStatisticsMapper) {
        this.videoStatisticsMapper = statisticsMapper;
        this.userStatisticsMapper = userStatisticsMapper;
    }

    /**
     * 根据视频ID批量查询统计信息
     *
     * @param ids 视频ID集合
     * @return 结果集
     */
    public Result<List<VideoStatistics>> getVideoStatById(List<Integer> ids) {
        List<VideoStatistics> statisticsList = videoStatisticsMapper.selectList(
                new LambdaQueryWrapper<VideoStatistics>().in(VideoStatistics::getVideoId, ids)
        );
        return Result.success(statisticsList);
    }

    /**
     * 根据用户ID获取其关注/粉丝数量
     *
     * @param id 用户ID
     * @return 关注/粉丝数量
     */
    public Result<UserStatistics> getUserStatistics(Integer id) {
        List<UserStatistics> userStatistics = userStatisticsMapper.selectList(
                new LambdaQueryWrapper<UserStatistics>().in(UserStatistics::getUserId, id)
        );
        if (userStatistics.isEmpty()) {
            return Result.error(Response.USER_NOT_FOUND_WRONG);
        }
        return Result.success(userStatistics.get(0));
    }
}
