package com.megrez.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// 用户实体类
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class User {
    // 用户ID
    @TableId(value = "id", type = IdType.AUTO)
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
    @JsonIgnore
    private long createdAt = System.currentTimeMillis();
    // 更新时间
    @JsonIgnore
    private long updatedAt = System.currentTimeMillis();
    // 测试数据标记
    @JsonIgnore
    private Integer isTest = 0;
}
