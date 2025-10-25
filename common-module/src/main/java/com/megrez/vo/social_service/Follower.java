package com.megrez.vo.social_service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.megrez.entity.User;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class Follower extends User {
    @JsonIgnore
    private Long followedTime; // 关注时间， 这个字段只在后台用做排序依据，不提供给前端。
}
