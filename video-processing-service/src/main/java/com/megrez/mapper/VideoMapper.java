package com.megrez.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.megrez.entity.Video;
import org.apache.ibatis.annotations.Mapper;

/**
 * 视频数据访问层
 * 
 * 提供视频相关的数据库操作接口
 * 继承MyBatis-Plus的BaseMapper，提供基础的CRUD操作
 * 
 * @author Doki Team
 * @since 1.0.0
 */
@Mapper
public interface VideoMapper extends BaseMapper<Video> {
    

}
