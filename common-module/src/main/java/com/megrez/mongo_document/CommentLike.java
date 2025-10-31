package com.megrez.mongo_document;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * 评论点赞文档
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "comment_likes")
public class CommentLike {
    @Id
    private String id;              // 主键ID

    private String commentId;       // 评论ID

    private Integer userId;         // 用户ID

    @Builder.Default
    private Long createdAt = System.currentTimeMillis(); // 创建时间

    @JsonIgnore
    @Builder.Default
    private Boolean isDeleted = false;    // 是否逻辑删除
}
