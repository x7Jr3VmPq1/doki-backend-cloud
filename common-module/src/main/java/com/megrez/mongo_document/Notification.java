package com.megrez.mongo_document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Document("notifications")
public class Notification {
    @Id
    private String id;
    // 要推给谁？
    private Integer userId;
    // 是什么类型，比如，点赞视频，给视频评论，回复评论，关注用户？
    private Integer type; // 1.关注 2.点赞视频 3.点赞评论 4.视频评论 5.评论回复
    // 通知的具体内容（如果是评论）
    private String content;
    // 触发通知的源头ID，比如视频ID或者评论ID等
    private String sourceId;
    // 操作者用户ID
    private Integer operatorId;
    // 通知分组键，用于合并通知，格式是：userId-type-sourceId，只合并点赞和关注。
    private String groupKey;
    // 合并了多少条
    @Builder.Default
    private Integer mergedCount = 1;
    // 已读标记？
    @Builder.Default
    private Integer isRead = 0;
    // 时间？
    @Builder.Default
    private Long createdAt = System.currentTimeMillis();
}
