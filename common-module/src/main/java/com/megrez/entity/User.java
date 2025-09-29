package com.megrez.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// 用户实体类
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    // 用户ID
    private Integer id;
    // 手机号
    @JsonIgnore
    private String phoneNumber;
    // 密码哈希值
    @JsonIgnore
    private String passwordHash;
    // 用户名
    private String username;
    // 头像URL
    private String avatarUrl;
    // 个人简介
    private String bio;
    // 注册时间
    private long createdAt;
    // 更新时间
    private long updatedAt;
}
