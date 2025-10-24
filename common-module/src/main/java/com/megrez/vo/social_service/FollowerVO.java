package com.megrez.vo.social_service;

import com.megrez.entity.User;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class FollowerVO extends User {
    private Long followedTime; // 关注时间
}
