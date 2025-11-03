package com.megrez.vo.notification_dm_service;

import com.megrez.mongo_document.Conversation;
import com.megrez.mysql_entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ConversationVO extends Conversation {
    private User userinfo; // 对方的用户信息
    private Integer unread;// 未读数量
}
