package com.megrez.rabbit.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommentLikeMessage {
    private Integer type; // 类型，1代表点赞，0代表取消点赞
    private Integer userId; // 操作者ID
    private String commentId; // 评论ID
    private Integer videoId; // 视频ID
    private Integer commentSender; // 上传者ID
    private String content; // 评论内容
    private Long timestamp = System.currentTimeMillis(); // 时间
}
