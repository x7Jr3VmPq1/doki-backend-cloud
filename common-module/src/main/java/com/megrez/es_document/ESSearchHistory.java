package com.megrez.es_document;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @JsonIgnore
    private String id;

    private String word;

    @JsonIgnore
    private Integer count;
    @JsonIgnore
    private long createdTime = System.currentTimeMillis();
    @JsonIgnore
    private long updatedTime = System.currentTimeMillis();
    @JsonIgnore
    private Integer isTest = 0;
}
