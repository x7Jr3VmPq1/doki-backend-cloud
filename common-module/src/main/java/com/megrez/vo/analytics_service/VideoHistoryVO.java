package com.megrez.vo.analytics_service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VideoHistoryVO {
    private Integer videoId;
    private Double watchedTime;
}
