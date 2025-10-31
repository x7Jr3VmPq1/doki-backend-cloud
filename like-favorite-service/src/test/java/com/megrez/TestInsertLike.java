package com.megrez;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.megrez.mysql_entity.Video;
import com.megrez.mysql_entity.VideoLikes;
import com.megrez.mapper.LikeMapper;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class TestInsertLike {

    private static final Logger log = LoggerFactory.getLogger(TestInsertLike.class);
    @Autowired
    LikeMapper likeMapper;

    @Autowired
    VideoMapper videoMapper;


    @Test
    public void testInsert() {
        LambdaQueryWrapper<Video> wrapper = new LambdaQueryWrapper<Video>().eq(Video::getIsTest, 1);
        wrapper.last("LIMIT 50");
        List<Video> videos = videoMapper.selectList(wrapper);
        videos.forEach(video -> {
            VideoLikes likes = VideoLikes.builder().videoId(video.getId()).userId(10001).isTest(1).build();
            likeMapper.insert(likes);
            log.info("插入测试点赞数据：{}", likes);
        });
    }
}
