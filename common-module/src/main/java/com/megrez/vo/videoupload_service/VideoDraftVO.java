package com.megrez.vo.videoupload_service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoDraftVO {
    private Integer id;
    private String title;
    private String description;
    private String tags;
    private String coverImage;
    private Integer permission;
    private Integer isScheduled;
    private Long scheduledTime;
}
