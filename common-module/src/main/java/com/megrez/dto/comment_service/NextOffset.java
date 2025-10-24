package com.megrez.dto.comment_service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NextOffset {
    Integer userId; // 用户id
    Integer videoId;  // 视频ID
    Double score;   //  热度值
    String lastCommentId;   // 上次加载的最后一条评论ID
    String parentCommentId; // 父评论ID，可有可无
    @Builder.Default
    Long timestamp = System.currentTimeMillis(); // 时间戳
}
