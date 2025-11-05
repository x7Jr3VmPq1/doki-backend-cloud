package com.megrez.vo.search_service;

import com.megrez.mysql_entity.User;
import com.megrez.mysql_entity.Video;
import com.megrez.mysql_entity.VideoStatistics;
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
    private Video video;
    /**
     * 用户信息
     */
    private User user;

    /**
     * 视频统计信息
     */
    private VideoStatistics statistics;
    /**
     * 高亮字段
     */
    private String highlight;
}
