package com.megrez.rabbit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class VideoLikeMessage {
    private Integer type; // 类型，1代表点赞，0代表取消点赞
    private Integer userId; // 操作者ID
    private Integer videoId; // 视频ID
}
