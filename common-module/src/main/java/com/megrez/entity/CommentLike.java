package com.megrez.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommentLike {
    private Integer id;              // 主键ID
    private Integer commentId;       // 评论ID
    private Integer userId;          // 用户ID
    private Boolean isLike;       // true=点赞, false=点踩
    private Long createdAt; // 创建时间
    private Boolean isDeleted;    // 是否逻辑删除
}
