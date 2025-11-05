package com.megrez.es_document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "abc")
public class TestDocument {
    @Id // 对应 Elasticsearch 文档的 _id 字段
    private String id;

    private String title;


}
