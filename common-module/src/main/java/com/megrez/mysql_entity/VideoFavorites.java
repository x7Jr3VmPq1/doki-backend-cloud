package com.megrez.mysql_entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 视频收藏记录表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoFavorites {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer userId;

    private Integer videoId;

    @Builder.Default
    private Long createdAt = System.currentTimeMillis();

    @Builder.Default
    private Integer isTest = 0;
}
