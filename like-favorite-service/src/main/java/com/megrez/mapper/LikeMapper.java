package com.megrez.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.megrez.entity.VideoLikes;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LikeMapper extends BaseMapper<VideoLikes> {
}
