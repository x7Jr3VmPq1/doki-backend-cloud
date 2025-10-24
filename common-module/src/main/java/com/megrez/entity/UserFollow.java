package com.megrez.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户关注关系实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_follow")
public class UserFollow {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 关注者ID，关联user表
     */
    private Integer followerId;

    /**
     * 被关注者ID，关联user表
     */
    private Integer followingId;

    /**
     * 关注时间戳
     */
    @Builder.Default
    private Long createdAt = System.currentTimeMillis();

    /**
     * 更新时间戳
     */
    @Builder.Default
    private Long updatedAt = System.currentTimeMillis();

    /**
     * 是否删除 0=否 1=是
     */
    @TableLogic
    @Builder.Default
    private Integer isDeleted = 0;
}
