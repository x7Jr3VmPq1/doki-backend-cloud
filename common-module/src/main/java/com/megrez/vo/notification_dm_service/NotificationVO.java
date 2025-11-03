package com.megrez.vo.notification_dm_service;

import com.megrez.mongo_document.Notification;
import com.megrez.mysql_entity.User;
import com.megrez.mysql_entity.Video;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class NotificationVO extends Notification {
    private User user;  // 触发通知的用户ID
    private Video sourceInfo;   // 源头信息
}
