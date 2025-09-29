package com.megrez.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 视频统计信息实体类
 * 
 * 用于存储视频的各种统计数据
 * 包含播放量、点赞数、评论数、分享数等统计信息
 * 
 * @author Doki Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoStatistics {
    
    /**
     * 统计ID，主键
     * 自增长，唯一标识一个统计记录
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    
    /**
     * 视频ID，关联video表
     * 标识统计信息对应的视频
     */
    private Integer videoId;
    
    /**
     * 播放次数
     * 视频被播放的总次数
     */
    private Long viewCount;
    
    /**
     * 点赞数
     * 用户点赞的总次数
     */
    private Long likeCount;
    
    /**
     * 点踩数
     * 用户点踩的总次数
     */
    private Long dislikeCount;
    
    /**
     * 评论数
     * 视频下的评论总数
     */
    private Long commentCount;
    
    /**
     * 分享数
     * 视频被分享的总次数
     */
    private Long shareCount;
    
    /**
     * 收藏数
     * 视频被收藏的总次数
     */
    private Long favoriteCount;
    
    /**
     * 下载数
     * 视频被下载的总次数
     */
    private Long downloadCount;
    
    /**
     * 创建时间戳
     * 统计记录创建的时间
     */
    private Long createdTime;
    
    /**
     * 更新时间戳
     * 统计信息最后修改的时间
     */
    private Long updatedTime;
    
    /**
     * 是否删除
     * 逻辑删除标记，0-未删除, 1-已删除
     */
    @TableLogic(value = "0", delval = "1")
    private Integer deleted;
}
