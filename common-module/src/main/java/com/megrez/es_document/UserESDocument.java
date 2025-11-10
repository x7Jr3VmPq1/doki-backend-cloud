package com.megrez.es_document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(indexName = "user")
public class UserESDocument {
    @Id
    private String id;

    private Integer userId;

    private String username;

    private String avatarUrl;

    private String bio;

    private Integer followingCount;

    private Integer followerCount;

    private Integer likeCount;

    private Long createdAt;

    @Builder.Default
    private Long updatedAt = System.currentTimeMillis();
}
