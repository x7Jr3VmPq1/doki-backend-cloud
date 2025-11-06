package com.megrez.es_document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.CompletionField;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.core.suggest.Completion;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "search_suggestion")
public class SearchSuggestion {
    @CompletionField(maxInputLength = 100)
    Completion completion;
}
