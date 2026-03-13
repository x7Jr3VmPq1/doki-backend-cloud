package com.megrez;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.megrez.mysql_entity.Video;
import com.megrez.mysql_entity.VideoStatistics;
import com.megrez.mapper.VideoMapper;
import com.megrez.utils.ContentLibrary;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;
import java.util.Random;

@SpringBootTest
public class TestInsertVideos {

    private static final Logger log = LoggerFactory.getLogger(TestInsertVideos.class);
    @Autowired
    VideoMapper videoMapper;
    @Autowired
    StatMapper statMapper;

    @Test
    public void insert() {
        // 获取原始数据
        List<Video> videos = videoMapper.selectList(new LambdaQueryWrapper<Video>().eq(Video::getIsTest, 0));
        for (int i = 0; i < 5; i++) {
            // 随机取一条
            Video video = videos.get(new Random().nextInt(videos.size()));
            // 获取测试用分类和标题
            Map.Entry<String, String> randomEntry = ContentLibrary.getRandomEntry();
            assert randomEntry != null;
            video.setTags(randomEntry.getKey());
            video.setTitle(randomEntry.getValue());
            video.setId(null);
            video.setIsTest(1);
            // 插入视频表
            videoMapper.insert(video);
            // 插入统计表
            VideoStatistics statistics = new VideoStatistics();
            statistics.setVideoId(video.getId());
            statistics.setTags(video.getTags());
            statistics.setIsTest(1);

            long nextInt = new Random().nextInt(100000);
            statistics.setViewCount(nextInt);
            statistics.setLikeCount((long) (nextInt * 0.1));
            statistics.setCommentCount((long) (nextInt * 0.01));
            statistics.setFavoriteCount((long) (nextInt * 0.005));

            statMapper.insert(statistics);
        }
    }
}
