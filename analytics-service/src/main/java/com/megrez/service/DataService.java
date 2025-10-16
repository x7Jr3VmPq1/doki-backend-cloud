package com.megrez.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.megrez.client.LikeFavoriteClient;
import com.megrez.entity.UserStatistics;
import com.megrez.entity.VideoStatistics;
import com.megrez.mapper.UserStatisticsMapper;
import com.megrez.mapper.VideoStatisticsMapper;
import com.megrez.result.Response;
import com.megrez.result.Result;
import com.megrez.vo.VideoStatVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DataService {

    private static final Logger log = LoggerFactory.getLogger(DataService.class);
    private final LikeFavoriteClient likeFavoriteClient;
    private final VideoStatisticsMapper videoStatisticsMapper;
    private final UserStatisticsMapper userStatisticsMapper;

    public DataService(LikeFavoriteClient likeFavoriteClient, VideoStatisticsMapper statisticsMapper, UserStatisticsMapper userStatisticsMapper) {
        this.likeFavoriteClient = likeFavoriteClient;
        this.videoStatisticsMapper = statisticsMapper;
        this.userStatisticsMapper = userStatisticsMapper;
    }

    /**
     * 根据视频ID批量查询统计信息
     *
     * @param ids 视频ID集合
     * @return 结果集
     */
    public Result<VideoStatVO> getVideoStatById(List<Integer> ids, Integer userId) {
        // 查询视频统计信息
        List<VideoStatistics> statisticsList = videoStatisticsMapper.selectList(
                new LambdaQueryWrapper<VideoStatistics>().in(VideoStatistics::getVideoId, ids)
        );
        VideoStatVO videoStatVO = new VideoStatVO();
        BeanUtils.copyProperties(statisticsList.get(0), videoStatVO);
        // 已登录的请求，判断是否点赞了该视频
        if (userId > 0) {
            // 判断该用户是否点赞视频
            try {
                Result<Boolean> result = likeFavoriteClient.existLikeRecord(userId, videoStatVO.getVideoId());
                if (result.isSuccess()) {
                    videoStatVO.setUserLiked(result.getData());
                }
            } catch (Exception e) {
                log.error("点赞/收藏服务调用异常");
            }
        }
        return Result.success(videoStatVO);
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
