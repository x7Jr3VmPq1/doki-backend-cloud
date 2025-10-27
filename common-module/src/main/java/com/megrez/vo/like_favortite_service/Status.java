package com.megrez.vo.like_favortite_service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Status {
    private Integer currentUserId = 0; // 用户ID
    private Boolean currentUserLiked = false; // 是否点赞
    private Boolean currentUserFavorite = false; // 是否收藏
}
