package com.megrez;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.megrez.entity.Video;
import com.megrez.entity.VideoStatistics;
import com.megrez.mapper.VideoMapper;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class TestInsertVideos {

    private static final Logger log = LoggerFactory.getLogger(TestInsertVideos.class);
    @Autowired
    VideoMapper videoMapper;
    @Autowired
    StatMapper statMapper;

    /**
     * 插入100条测试视频
     */
    @Test
    public void testInsertVideos() {
        for (int i = 1; i <= 100; i++) {
            Video video = Video.builder().title("测试视频" + i)
                    .description("这是第" + i + "个测试视频的描述")
                    .uploaderId(10001)
                    .tags("测试,视频,示例")
                    .categoryId(1)
                    .videoFilename("fd0fb2bf-2274-42fe-bf40-907bfd7ac2b6")
                    .videoSize(1024L * 1024 * 50) // 50MB
                    .videoDuration(60 + i) // 60秒到159秒
                    .videoFormat(".mp4")
                    .videoWidth(1920)
                    .videoHeight(1080)
                    .videoBitrate(2000) // 2Mbps
                    .publishTime(System.currentTimeMillis())
                    .permission(1) // 公开
                    .allowComment(1) // 允许评论
                    .createdTime(System.currentTimeMillis())
                    .updatedTime(System.currentTimeMillis())
                    .coverName("default.jpg")
                    .isTest(true)
                    .build();
            videoMapper.insert(video);
            VideoStatistics videoStatistics = new VideoStatistics();
            videoStatistics.setVideoId(video.getId());
            videoStatistics.setTest(true);
            statMapper.insert(videoStatistics);
            log.info("插入第 {} 条测试数据成功。{}", i, video);
        }
    }

    @Test
    public void deleteTestData() {
        statMapper.delete(new LambdaQueryWrapper<VideoStatistics>().eq(VideoStatistics::isTest, true));
        videoMapper.delete(new LambdaQueryWrapper<Video>().eq(Video::isTest, true));
    }


}
