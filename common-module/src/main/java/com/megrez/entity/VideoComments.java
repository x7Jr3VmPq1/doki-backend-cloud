package com.megrez.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoComments {

    private Long id;                // 主键ID
    private Long videoId;           // 视频ID
    private Long userId;            // 用户ID
    private String content;         // 评论内容
    private Long parentCommentId;   // 父评论ID（null表示根评论）
    private Boolean isRoot;         // 是否为根评论
    private Integer childCount;     // 子评论数量
    private Long createdAt;         // 创建时间
    private Integer likeCount;      // 点赞数量
    private Integer dislikeCount;   // 踩数量
    private String imgUrl;          // 图片URL（可选）
    private Boolean isDeleted;      // 是否逻辑删除
}
