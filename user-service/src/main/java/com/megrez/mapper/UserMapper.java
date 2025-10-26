package com.megrez.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.megrez.entity.User;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserMapper extends BaseMapper<User> {

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
     *
     * @param user 用户信息
     */
    @Insert("INSERT INTO user (phone_number,username,avatar_url,created_at,updated_at,is_test) " +
            "VALUE (#{phoneNumber},#{username},#{avatarUrl},#{createdAt},#{updatedAt},#{isTest})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void add(User user);

    /**
     * 修改密码
     *
     * @param phone        手机号
     * @param passwordHash 加密密码
     */
    @Update("UPDATE user SET password_hash = #{passwordHash} WHERE phone_number = #{phone}")
    Integer setPassword(@Param("phone") String phone,
                        @Param("passwordHash") String passwordHash);

    /**
     * 根据用户名获取用户基本信息
     *
     * @param username 用户名
     * @return 用户名，头像地址，简介
     */
    @Select("SELECT id,username,avatar_url,bio FROM user WHERE username = #{username}")
    User findUserByName(String username);
}
