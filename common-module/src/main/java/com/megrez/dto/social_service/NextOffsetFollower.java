package com.megrez.dto.social_service;

import com.megrez.vo.social_service.FollowerVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NextOffsetFollower {
    private Integer userId; // 请求的用户
    private FollowerVO followerVO; // 关注者视图对象
    private Integer mode; // 拉取模式，1=综合，2=最近，3=最早
    @Builder.Default
    Long timestamp = System.currentTimeMillis(); // 时间戳
}
