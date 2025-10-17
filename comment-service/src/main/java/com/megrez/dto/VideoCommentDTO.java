package com.megrez.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VideoCommentDTO {
    private Integer videoId; // 视频ID
    private String parentCommentId; // 父评论ID
    private String replyTargetId; // 回复目标评论ID
    private String content; // 评论内容
    private String image; // 图片BASE64
}
