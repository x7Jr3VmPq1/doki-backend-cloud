package com.megrez.redis;

import com.megrez.mysql_entity.Video;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class VideoInfoRedisClient {

    private static final Logger log = LoggerFactory.getLogger(VideoInfoRedisClient.class);
    private final RedisTemplate<String, String> redisTemplate;
    private final RedisTemplate<String, Integer> numberRedisTemplate;

    public VideoInfoRedisClient(RedisTemplate<String, String> redisTemplate, RedisTemplate<String, Integer> numberRedisTemplate) {
        this.redisTemplate = redisTemplate;
        this.numberRedisTemplate = numberRedisTemplate;
    }

    /**
     * 添加新内容到时间线
     *
     * @param tid    目标时间线用户ID
     * @param videos 待添加的视频
     */
    public void pushFollowTimeline(Integer tid, List<Video> videos) {

        if (videos.isEmpty()) {
            return;
        }

        Set<ZSetOperations.TypedTuple<String>> tuples = new HashSet<>();

        for (Video video : videos) {
            tuples.add(ZSetOperations.TypedTuple.of(video.getUploaderId() + ":" + video.getId(), video.getCreatedTime().doubleValue()));
        }

        redisTemplate.opsForZSet().add("timeline:follow:" + tid, tuples);

    }

    public List<Integer> getFollowTimeline(Integer uid, Integer limit) {
        Set<String> strings = redisTemplate.opsForZSet().reverseRange("timeline:follow:" + uid, 0, System.currentTimeMillis());
        if (strings == null || strings.isEmpty()) {
            return List.of();
        }

        // 把set转换为一个map
        Map<Integer, List<Integer>> timelineMap = toMapSupporter(strings);

        // 过滤可能已经不在关注列表的情况。
        HashSet<Integer> timelineUserSet = new HashSet<>(timelineMap.keySet());
        // 先获取用户的关注列表
        Set<Integer> followingSet = numberRedisTemplate.opsForSet().members("user:follow:" + uid);
        // 把timeline中的user和用户的实际关注列表求差集，得到已经不在关注列表的用户集合。
        timelineUserSet.removeAll(followingSet);    // 经过这一步，timelineUserSet剩下的元素是目前不存在于关注列表的用户ID

        // 将应该被删除的timeline内容从缓存中清理
        List<String> needDelete = new ArrayList<>();
        for (Integer key : timelineUserSet) {
            List<Integer> pidList = timelineMap.get(key);
            if (pidList != null) {
                List<String> deleteKeys = pidList.stream().map(pid -> key + ":" + pid).toList();
                needDelete.addAll(deleteKeys);
            }
        }
        if (!needDelete.isEmpty())
            redisTemplate.opsForZSet().remove("timeline:follow:" + uid, needDelete.toArray());

        // 过滤返回结果
        timelineMap.keySet().removeAll(timelineUserSet);
        // 收集过滤后的pid并返回
        return timelineMap.values().stream()
                .flatMap(List::stream)
                .sorted(Comparator.reverseOrder())
                .toList();
    }

    // 辅助方法，把timeline提取为一个key为uid，value为List<pid>的map。
    public Map<Integer, List<Integer>> toMapSupporter(Set<String> set) {
        Map<Integer, List<Integer>> result = new HashMap<>();

        for (String item : set) {
            String[] parts = item.split(":");
            int key = Integer.parseInt(parts[0]);
            int value = Integer.parseInt(parts[1]);

            result.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
        }
        return result;
    }

}
