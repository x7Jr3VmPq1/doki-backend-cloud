package com.megrez.dto.social_service;

import com.megrez.mysql_entity.UserFollow;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NextOffsetFollower {
    private Integer userId; // 发起查询请求的用户
    private UserFollow userFollow; // 关注记录对象，存储上次加载的最后一条记录内容
    private Integer mode; // 拉取模式，1=综合，2=最近，3=最早
    @Builder.Default
    Long timestamp = System.currentTimeMillis(); // 时间戳
}
