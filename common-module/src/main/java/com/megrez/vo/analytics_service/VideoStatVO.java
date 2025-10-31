package com.megrez.vo.analytics_service;

import com.megrez.mysql_entity.VideoStatistics;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class VideoStatVO extends VideoStatistics {
    boolean userLiked = false; // 用户是否点赞了该视频，默认false
}
