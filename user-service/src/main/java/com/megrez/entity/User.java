package com.megrez.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// 用户实体类
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    // 用户ID
    private Integer id;
    // 用户名
    private String userName;
    // 密码哈希值
    @JsonIgnore
    private String passwordHash;
    // 邮箱
    @JsonIgnore
    private String email;
    // 头像URL
    private String avatarUrl;
    // 电话号码
    @JsonIgnore
    private String phoneNumber;
    // 个人简介
    private String bio;
    // 注册时间
    private LocalDateTime createdAt;
    // 更新时间
    private LocalDateTime updatedAt;
    // 关注数
    private Integer followingCount;
    // 粉丝数
    private Integer followerCount;
    // 被赞数
    private Integer likedCount;
}
