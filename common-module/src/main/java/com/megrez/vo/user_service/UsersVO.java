package com.megrez.vo.user_service;

import com.megrez.mysql_entity.User;
import com.megrez.mysql_entity.UserStatistics;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = false)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UsersVO extends User {
    private UserStatistics stat;  // 用户统计数据
    private Boolean followed = false; // 当前用户是否关注了此用户
}
