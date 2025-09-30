package com.megrez.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.megrez.entity.Video;
import com.megrez.entity.VideoStatistics;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StatisticsMapper extends BaseMapper<VideoStatistics> {
}
