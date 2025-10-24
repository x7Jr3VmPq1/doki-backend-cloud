package com.megrez.dto.social_service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CheckFollowDTO {
    private Integer uid;    // 当前用户
    private List<Integer> targetIds; // 要查询的用户
}
