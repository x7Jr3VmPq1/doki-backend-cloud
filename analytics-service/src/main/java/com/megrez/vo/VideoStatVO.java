package com.megrez.vo;

import com.megrez.entity.VideoStatistics;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class VideoStatVO extends VideoStatistics {
    boolean userLiked = false; // 用户是否点赞了该视频，默认false
}
