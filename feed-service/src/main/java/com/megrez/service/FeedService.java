package com.megrez.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.megrez.mapper.StatMapper;
import com.megrez.mapper.VideoMapper;
import com.megrez.mysql_entity.Video;
import com.megrez.mysql_entity.VideoStatistics;
import com.megrez.vo.video_info_service.VideoVO;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

@Service
public class FeedService {

    private final StatMapper statMapper;
    private final VideoMapper videoMapper;
    private final VideoUtils videoUtils;
    private final StringRedisTemplate redisTemplate;

    public FeedService(StatMapper statMapper, VideoMapper videoMapper, VideoUtils videoUtils, StringRedisTemplate redisTemplate) {
        this.statMapper = statMapper;
        this.videoMapper = videoMapper;
        this.videoUtils = videoUtils;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 获取推荐视频
     *
     * @param uid 用户ID
     * @return 视频列表
     */
    public List<VideoVO> getRecommend(Integer uid) {
        // 1. 获取用户偏好，如果未登录,或没有历史数据，随机获取视频
        Set<String> tags = redisTemplate.opsForZSet().reverseRange("user:model:" + uid, 0, 4);

        List<VideoVO> maybeList = new ArrayList<>(); // 最终推荐结果

        if (tags == null || tags.isEmpty()) {
            LambdaQueryWrapper<VideoStatistics> wrapper = new LambdaQueryWrapper<>();
            wrapper.last("ORDER BY RAND() LIMIT 10");
            List<VideoStatistics> list = statMapper.selectList(wrapper);
            List<Integer> vIds = list.stream().map(VideoStatistics::getVideoId).toList();
            List<Video> videos = videoMapper.selectBatchIds(vIds);
            maybeList = videoUtils.batchToVO(uid, videos);
            return maybeList;
        }
        // 2. 查询对应偏好视频列表
        LambdaQueryWrapper<VideoStatistics> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(VideoStatistics::getTags, tags);
        List<VideoStatistics> list = statMapper.selectList(wrapper);
        // 3. 去重
        Set<String> history = redisTemplate.opsForZSet().range("user:history:" + uid, 0, -1);
        if (history != null && !history.isEmpty()) {
            // 转换为 Set<Integer>
            Set<Integer> intHistory = history.stream()
                    .map(Integer::valueOf)
                    .collect(Collectors.toSet());
            // 移除
            list.removeIf(stat -> intHistory.contains(stat.getVideoId()));
        }
        // 4. 排序
        long now = System.currentTimeMillis();
        double gravity = 1.5; // 重力因子

        list.sort((a, b) -> {
            ToDoubleFunction<VideoStatistics> scoreCalc = (v) -> {
                // 1. 计算基础互动分
                double baseScore = v.getViewCount() * 1.0 +
                        v.getLikeCount() * 5.0 +
                        v.getCommentCount() * 10.0 +
                        v.getFavoriteCount() * 15.0;

                // 2. 计算发布时长（小时）
                double hoursSincePublished = Math.max(0, (now - v.getCreatedTime()) / (1000.0 * 3600.0));
                // 3. 计算最终得分：BaseScore / (Hours + 2)^Gravity
                // +2 是平滑常数，防止新发布的视频（Hours趋近0）得分无穷大
                return baseScore / Math.pow(hoursSincePublished + 2, gravity);
            };

            // 降序：分高者在前
            return Double.compare(scoreCalc.applyAsDouble(b), scoreCalc.applyAsDouble(a));
        });
        // 5. 获取视频信息
        List<Integer> vIds = list.stream()
                .map(VideoStatistics::getVideoId)
                .limit(10)
                .toList();

        if (!vIds.isEmpty()) {
            List<Video> videos = videoMapper.selectBatchIds(vIds);
            maybeList.addAll(videoUtils.batchToVO(uid, videos));
        }
        // 如果数量不足，随机补充
        int size = maybeList.size();
        if (size < 10) {
            LambdaQueryWrapper<Video> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.last("ORDER BY RAND() LIMIT " + (10 - size));
            List<Video> supplies = videoMapper.selectList(queryWrapper);
            maybeList.addAll(videoUtils.batchToVO(uid, supplies));
        }
        // 6. 返回
        return maybeList;
    }


}
