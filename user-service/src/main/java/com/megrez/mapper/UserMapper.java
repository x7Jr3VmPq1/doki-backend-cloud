package com.megrez.mapper;

import com.megrez.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {

    /**
     * 根据手机号查询用户信息
     *
     * @param phone 手机号
     * @return 用户信息
     */
    @Select("SELECT * FROM user WHERE phone_number = #{phone}")
    User getUserByPhone(String phone);

    /**
     * 添加新用户
     * @param user 用户信息
     */
    @Insert("INSERT INTO user (phone_number,username,avatar_url,created_at,updated_at) " +
            "VALUE (#{phoneNumber},#{username},#{avatarUrl},#{createdAt},#{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void addUser(User user);
}
