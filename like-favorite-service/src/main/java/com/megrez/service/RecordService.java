package com.megrez.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.megrez.mapper.LikeMapper;
import com.megrez.mysql_entity.VideoLikes;
import com.megrez.mapper.RecordMapper;
import com.megrez.result.Result;
import com.megrez.utils.PageTokenUtils;
import com.megrez.vo.CursorLoad;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecordService {

    private final RecordMapper recordMapper;
    private final LikeMapper likeMapper;

    public RecordService(RecordMapper recordMapper, LikeMapper likeMapper) {
        this.recordMapper = recordMapper;
        this.likeMapper = likeMapper;
    }

    public Result<CursorLoad<VideoLikes>> getRecordsByUserId(Integer userId, String cursor) throws Exception {
        LambdaQueryWrapper<VideoLikes> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VideoLikes::getUserId, userId);
        wrapper.orderByDesc(VideoLikes::getCreatedAt);
        wrapper.orderByDesc(VideoLikes::getId);
        if (cursor != null) {
            VideoLikes videoLikes = PageTokenUtils.decryptState(cursor, VideoLikes.class);
            wrapper.le(VideoLikes::getCreatedAt, videoLikes.getCreatedAt());
            wrapper.ne(VideoLikes::getId, videoLikes.getId());
        }
        wrapper.last("LIMIT 21");

        List<VideoLikes> videoLikes = recordMapper.selectList(wrapper);

        boolean hasMore = false;
        cursor = null;
        if (videoLikes.size() > 20) {
            videoLikes = videoLikes.subList(0, videoLikes.size() - 1);
            hasMore = true;
            cursor = PageTokenUtils.encryptState(videoLikes.get(videoLikes.size() - 1));
        }

        return Result.success(CursorLoad.of(videoLikes, hasMore, cursor));
    }

    public Result<List<VideoLikes>> getRecordsByCount(Integer userId, Integer count) {
        List<VideoLikes> videoLikes = likeMapper.selectList(
                new LambdaQueryWrapper<VideoLikes>()
                        .eq(VideoLikes::getUserId, userId)
                        .orderByDesc(VideoLikes::getCreatedAt)
                        .last("LIMIT " + count)
        );
        return Result.success(videoLikes);
    }
}
