package com.megrez.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommentLike {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;              // 主键ID
    private Integer commentId;       // 评论ID
    private Integer userId;          // 用户ID
    private Boolean isLike;       // true=点赞, false=点踩
    private Long createdAt; // 创建时间
    @TableLogic(value = "0", delval = "1")
    private Boolean isDeleted;    // 是否逻辑删除
}
