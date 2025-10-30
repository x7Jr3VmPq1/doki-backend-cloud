package com.megrez.vo.video_info_service;

import com.megrez.entity.Video;
import com.megrez.entity.VideoStatistics;
import com.megrez.vo.analytics_service.VideoHistory;
import com.megrez.vo.analytics_service.VideoWatched;
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
    private Double watchedTime; // 已观看时长
    private Long watchedAt; // 观看时间
}
