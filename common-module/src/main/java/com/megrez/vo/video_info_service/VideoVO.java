package com.megrez.vo.video_info_service;

import com.megrez.entity.Video;
import com.megrez.entity.VideoStatistics;
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
}
