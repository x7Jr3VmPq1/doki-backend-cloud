package com.megrez.mongo_document;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "conversation")
public class Conversation {
    @Id
    private String id;

    private List<Integer> members;

    private DirectMessage lastMessage;

//    @JsonIgnore
    @Builder.Default
    private Map<Integer, Long> lastDeleteAt = Map.of();

    @Builder.Default
    private Long createdAt = System.currentTimeMillis();

    @Builder.Default
    private Long updatedAt = System.currentTimeMillis();
}
