package com.megrez.service;

import com.megrez.mysql_entity.Video;
import com.megrez.mysql_entity.VideoStatistics;
import com.megrez.mapper.VideoStatisticsMapper;
import com.megrez.rabbit.exchange.VideoPublishedExchange;
import com.megrez.utils.JSONUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class IncreaseService {

    private final VideoStatisticsMapper statisticsMapper;
    private static final Logger log = LoggerFactory.getLogger(IncreaseService.class);

    public IncreaseService(VideoStatisticsMapper statisticsMapper) {
        this.statisticsMapper = statisticsMapper;
    }

    /**
     * 为新增的视频添加统计记录
     *
     * @param video 新增视频消息实体
     */
    @RabbitListener(queues = VideoPublishedExchange.QUEUE_ANALYTICS)
    public void createVideoStatisticsRecord(String video) {
        try {
            // 解析视频消息
            Video videoEntity = JSONUtils.fromJSON(video, Video.class);
            log.info("开始为视频创建统计记录，视频ID：{}", videoEntity.getId());

            // 创建初始统计记录
            VideoStatistics statistics = VideoStatistics.builder()
                    .videoId(videoEntity.getId())
                    .viewCount(0L)
                    .likeCount(0L)
                    .dislikeCount(0L)
                    .commentCount(0L)
                    .shareCount(0L)
                    .favoriteCount(0L)
                    .downloadCount(0L)
                    .createdTime(System.currentTimeMillis())
                    .updatedTime(System.currentTimeMillis())
                    .deleted(0)
                    .build();

            // 插入统计记录
            int result = statisticsMapper.insert(statistics);
            if (result > 0) {
                log.info("视频统计记录创建成功，视频ID：{}，统计ID：{}",
                        videoEntity.getId(), statistics.getId());
            } else {
                log.error("视频统计记录创建失败，视频ID：{}", videoEntity.getId());
            }

        } catch (Exception e) {
            log.error("创建视频统计记录时发生异常：{}", e.getMessage(), e);
        }
    }
}
