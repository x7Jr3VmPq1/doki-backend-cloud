package com.megrez.mapper;

import com.megrez.entity.User;
import org.apache.ibatis.annotations.*;

@Mapper
public interface LoginAndRegisterMapper {
    /**
     * 根据手机号获取用户信息
     *
     * @param phone
     * @return
     */
    @Select("SELECT * FROM users WHERE phone_number = #{phone}")
    User getUserByPhone(String phone);

    /**
     * 添加新用户
     *
     * @return
     */
    @Insert("INSERT INTO users (username,phone_number,avatar_url)" +
            " VALUE (#{userName},#{phoneNumber},#{avatarUrl})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void addNewUser(User newUser);

    /**
     * 判断是否设置了密码
     *
     * @param phone
     */
    @Select("SELECT 1 FROM users WHERE phone_number = #{phone} AND password_hash IS NOT NULL")
    String hasPassword(String phone);

    /**
     * 设置用户密码
     *
     * @param userId
     * @param password
     */
    @Update("UPDATE users SET password_hash = #{password} WHERE id = #{userId}")
    void setPassword(@Param("userId") Long userId, @Param("password") String password);

}
