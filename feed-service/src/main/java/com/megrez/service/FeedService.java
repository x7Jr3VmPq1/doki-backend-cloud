package com.megrez.service;

import com.megrez.rabbit.exchange.VideoLikeExchange;
import com.megrez.vo.video_info_service.VideoVO;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FeedService {

    /**
     * 获取推荐视频
     *
     * @param uid 用户ID
     * @return 视频列表
     */
    public List<VideoVO> getRecommend(Integer uid) {
        // 1. 获取用户偏好，如果未登录，随机获取热门视频
        if (uid < 0) {

        }
        // 2. 查询对应偏好视频列表

        // 3. 去重

        // 4. 排序

        // 5. 获取视频信息

        // 6. 返回
        return List.of();
    }


}
