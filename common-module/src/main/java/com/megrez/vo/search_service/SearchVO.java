package com.megrez.vo.search_service;

import com.megrez.mysql_entity.User;
import com.megrez.mysql_entity.Video;
import com.megrez.mysql_entity.VideoStatistics;
import com.megrez.vo.video_info_service.VideoVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchVO {
    /**
     * 视频信息
     */
    private VideoVO video;
    /**
     * 高亮字段
     */
    private String highlight;
}
