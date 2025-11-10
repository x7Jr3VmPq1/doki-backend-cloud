package com.megrez.service;

import com.megrez.es_document.UserESDocument;
import com.megrez.mysql_entity.User;
import com.megrez.rabbit.exchange.UserAddExchange;
import com.megrez.rabbit.exchange.UserUpdateExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;

import org.springframework.stereotype.Service;

@Service
public class UserListener {

    private static final Logger log = LoggerFactory.getLogger(UserListener.class);
    private final ElasticsearchOperations operations;

    public UserListener(ElasticsearchOperations operations) {
        this.operations = operations;
    }

    @RabbitListener(queues = UserUpdateExchange.QUEUE_USER_UPDATE_SEARCH)
    public void updateUser(User user) {
        log.info("更新用户资料：{}", user);
        UserESDocument document = UserESDocument.builder()
                .id(user.getId().toString())
                .userId(user.getId())
                .username(user.getUsername())
                .bio(user.getBio())
                .avatarUrl(user.getAvatarUrl())
                .updatedAt(System.currentTimeMillis())
                .build();
        operations.update(document);
    }

    @RabbitListener(queues = UserAddExchange.QUEUE_USER_ADD_SEARCH)
    public void addUser(User user) {
        log.info("新增用户：{}", user);
        UserESDocument document = UserESDocument.builder()
                .id(user.getId().toString())
                .userId(user.getId())
                .username(user.getUsername())
                .bio(user.getBio())
                .avatarUrl(user.getAvatarUrl())
                .followerCount(0)
                .followingCount(0)
                .likeCount(0)
                .createdAt(System.currentTimeMillis())
                .updatedAt(System.currentTimeMillis())
                .build();
        operations.save(document);
    }
}
