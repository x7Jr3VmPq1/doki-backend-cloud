package com.megrez.es_document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "search_history")
public class ESSearchHistory {
    @Id
    private String id;

    private String word;

    private Integer count;

    private long createdTime = System.currentTimeMillis();

    private long updatedTime = System.currentTimeMillis();

    private Integer isTest = 0;
}
