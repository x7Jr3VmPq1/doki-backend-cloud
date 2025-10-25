package com.megrez.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.megrez.entity.UserFollow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserFollowMapper extends BaseMapper<UserFollow> {

    @Select("SELECT * FROM user_follow WHERE follower_id = #{userId} AND is_deleted = 0 ORDER BY created_at DESC")
    List<UserFollow> getListByFollowerId(Integer userId);
}
