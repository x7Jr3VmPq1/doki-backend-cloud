package com.megrez.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.megrez.entity.Video;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface VideoMapper extends BaseMapper<Video> {

    /**
     * 随机返回五个视频。
     * @return 结果集
     */
    @Select("SELECT * FROM video ORDER BY RAND() LIMIT 5;")
    List<Video> getRandomVideo();
}
