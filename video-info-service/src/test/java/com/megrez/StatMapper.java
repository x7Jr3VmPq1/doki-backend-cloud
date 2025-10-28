package com.megrez;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.megrez.entity.VideoStatistics;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StatMapper  extends BaseMapper<VideoStatistics> {
}
