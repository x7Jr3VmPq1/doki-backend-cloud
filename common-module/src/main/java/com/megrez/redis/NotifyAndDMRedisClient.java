package com.megrez.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class NotifyAndDMRedisClient {
    private static final String DM_UNREAD_KEY = "dm:unread:user:";
    private static final String NOTIFY_UNREAD_KEY = "notify:unread:user";

    private static final Logger log = LoggerFactory.getLogger(NotifyAndDMRedisClient.class);
    private final StringRedisTemplate redisTemplate;

    public NotifyAndDMRedisClient(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 增加或减少未读数
     *
     * @param uid 用户id
     * @param cid 会话id
     */
    public void incDMUnread(Integer uid, String cid, Integer delta) {
        log.info("增加未读数：{}，{}，{}", uid, cid, delta);
        // 对应会话的未读数 + 1
        redisTemplate.opsForHash().increment(DM_UNREAD_KEY + uid, cid, delta);
        // 总数 + 1
        redisTemplate.opsForHash().increment(DM_UNREAD_KEY + uid, "total", delta);
    }

    /**
     * 获取会话未读数
     *
     * @param uid  用户id
     * @param cIds 批量的会话id
     * @return 未读数
     */
    public Map<String, Integer> getUnread(Integer uid, List<Object> cIds) {
        if (cIds == null || cIds.isEmpty()) {
            return Map.of();
        }
        List<Object> counts = redisTemplate.opsForHash().multiGet(DM_UNREAD_KEY + uid, cIds);
        HashMap<String, Integer> map = new HashMap<>();
        for (int i = 0; i < cIds.size(); i++) {
            map.put(cIds.get(i).toString(), counts.get(i) == null ? 0 : Integer.parseInt(counts.get(i).toString()));
        }
        return map;
    }

    /**
     * 获取私信未读数
     *
     * @param uid 用户id
     * @return 私信未读数
     */
    public Integer getDMUnreadTotal(Integer uid) {
        Object count = redisTemplate.opsForHash().get(DM_UNREAD_KEY + uid, "total");
        return count == null ? 0 : Integer.parseInt(count.toString());
    }

    /**
     * 获取通知未读数
     *
     * @param uid 用户id
     * @return 私信未读数
     */
    public Integer getNotifyUnreadTotal(Integer uid) {
        String number = redisTemplate.opsForValue().get(NOTIFY_UNREAD_KEY + uid);
        return number == null ? 0 : Integer.parseInt(number);
    }

    /**
     * 清空单个会话未读数
     *
     * @param uid 用户id
     * @param cid 会话id
     */
    public void clearSingleDMUnread(Integer uid, String cid) {
        Map<String, Integer> map = getUnread(uid, List.of(cid));
        Integer unread = map.get(cid);
        redisTemplate.opsForHash().delete(DM_UNREAD_KEY + uid, cid);
        redisTemplate.opsForHash().increment(DM_UNREAD_KEY + uid, "total", -unread);
    }

    /**
     * 清空所有会话未读数
     *
     * @param uid 用户id
     */
    public void clearAllDMUnread(Integer uid) {
        redisTemplate.delete(DM_UNREAD_KEY + uid);
    }

    /**
     * 给通知未读数增加 1
     *
     * @param uid 目标用户ID
     */
    public void incNotifyUnread(Integer uid) {
        redisTemplate.opsForValue().increment(NOTIFY_UNREAD_KEY + uid);
    }

    /**
     * 清空通知未读数
     *
     * @param uid 目标用户ID
     */
    public void delNotifyUnreadCount(Integer uid) {
        redisTemplate.delete(NOTIFY_UNREAD_KEY + uid);
    }
}
