package com.megrez.es_document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "video")
public class VideoESDocument {
    @Id
    private String id;

    private String title;

    private Integer userId;

    private String username;

    private String description;

    private String tags;

    private Integer duration = 0;

    private Integer views = 0;

    private Integer likeCount = 0;

    private Integer favoriteCount = 0;

    private Integer commentCount = 0;

    private Long createdTime = System.currentTimeMillis();

    private Long updatedTime = System.currentTimeMillis();

    private Integer isTest = 0;
}
