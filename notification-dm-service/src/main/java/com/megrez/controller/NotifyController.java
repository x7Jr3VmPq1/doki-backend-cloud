package com.megrez.controller;

import com.megrez.annotation.CurrentUser;
import com.megrez.mongo_document.Notification;
import com.megrez.result.Result;
import com.megrez.service.NotifyService;
import com.megrez.vo.notification_dm_service.NotificationVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notify")
public class NotifyController {

    private final NotifyService notifyService;

    public NotifyController(NotifyService notifyService) {
        this.notifyService = notifyService;
    }


    /**
     * 获取通知列表
     *
     * @param userId 用户ID
     * @param type   类型
     * @return 通知列表
     */
    @GetMapping
    public Result<List<NotificationVO>> get(@CurrentUser Integer userId,
                                            @RequestParam(value = "type", required = false) Integer type) {
        return notifyService.getNotifications(userId, type);
    }


    @GetMapping("/unread")
    public Result<Integer> getUnreadCount(@CurrentUser Integer uid){
        return notifyService.getUnreadCount(uid);
    }


    @DeleteMapping("/unread")
    public Result<Void> delUnreadCount(@CurrentUser Integer uid){
        return notifyService.delUnreadCount(uid);
    }
}
