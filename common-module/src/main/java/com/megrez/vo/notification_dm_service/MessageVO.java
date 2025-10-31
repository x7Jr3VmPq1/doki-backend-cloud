package com.megrez.vo.notification_dm_service;

import com.megrez.mongo_document.DirectMessage;
import com.megrez.mysql_entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class MessageVO extends DirectMessage {
    private User userinfo;
}
