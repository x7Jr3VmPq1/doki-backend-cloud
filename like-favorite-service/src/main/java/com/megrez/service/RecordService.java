package com.megrez.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.megrez.entity.VideoLikes;
import com.megrez.mapper.RecordMapper;
import com.megrez.result.Result;
import com.megrez.utils.PageTokenUtils;
import com.megrez.vo.CursorLoad;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecordService {

    private final RecordMapper recordMapper;

    public RecordService(RecordMapper recordMapper) {
        this.recordMapper = recordMapper;
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
}
