package com.megrez.vo.video_info_service;

import com.megrez.mysql_entity.User;
import com.megrez.mysql_entity.Video;
import com.megrez.mysql_entity.VideoStatistics;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class VideoVO extends Video {
    private VideoStatistics statistics; // 统计数据
    private User user;  // 用户信息
    private Boolean liked; // 是否点过赞
    private Boolean followed; // 是否关注了上传者
    private Double watchedTime = 0.0; // 已观看时长
    private Long watchedAt; // 观看时间
}
