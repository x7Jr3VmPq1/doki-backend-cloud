package com.megrez.mongo_document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "direct_messages")
public class DirectMessage {
    @Id
    private String id;

    private String conversationId;

    private Integer senderId;

    private String content;

    private String imgUrl = null;

    private Long timestamp = System.currentTimeMillis();
}
