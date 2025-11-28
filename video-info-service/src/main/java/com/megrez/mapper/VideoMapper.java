package com.megrez.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.megrez.mysql_entity.Video;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface VideoMapper extends BaseMapper<Video> {
}
