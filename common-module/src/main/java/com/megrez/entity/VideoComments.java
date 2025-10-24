package com.megrez.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


/**
 * 视频评论实体
 * 使用MongoDB存储
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "video_comments")
public class VideoComments {

    @Id
    private String id;              // 文档ID

    @Builder.Default
    private Integer videoId = 0;           // 视频ID，默认0

    @Builder.Default
    private Integer userId = 0;            // 用户ID，默认0

    @Builder.Default
    private String content = "";         // 评论内容，默认空字符串

    @Builder.Default
    private String parentCommentId = null;   // 父评论ID，默认null

    @Builder.Default
    private String replyTargetId = null; // 回复目标评论ID，默认为null

    @Builder.Default
    private Boolean isRoot = true;         // 是否为根评论

    @Builder.Default
    private Integer childCount = 0;     // 子评论数量

    @Builder.Default
    private Long createdAt = System.currentTimeMillis();  // 创建时间

    @Builder.Default
    private Integer likeCount = 0;      // 点赞数量

    @JsonIgnore
    @Builder.Default
    private Integer dislikeCount = 0;   // 踩数量

    @Builder.Default
    private String imgUrl = "";          // 图片URL，默认空字符串

    @Builder.Default
    @JsonIgnore
    private double score = 0;   // 热度评分，排序评论的依据

    @Builder.Default
    @JsonIgnore
    private Boolean isDeleted = false;      // 是否删除，默认false
}
