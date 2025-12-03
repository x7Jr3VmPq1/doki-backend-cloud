package com.megrez.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.megrez.mapper.FavoriteMapper;
import com.megrez.mapper.LikeMapper;
import com.megrez.mysql_entity.VideoFavorites;
import com.megrez.mysql_entity.VideoLikes;
import com.megrez.result.Result;
import com.megrez.utils.PageTokenUtils;
import com.megrez.vo.CursorLoad;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecordService {

    private final LikeMapper likeMapper;
    private final FavoriteMapper favoriteMapper;

    public RecordService(LikeMapper likeMapper, FavoriteMapper favoriteMapper) {
        this.likeMapper = likeMapper;
        this.favoriteMapper = favoriteMapper;
    }

    public Result<CursorLoad<VideoLikes>> getLikeRecordsByUserId(Integer userId, String cursor) throws Exception {
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

        List<VideoLikes> videoLikes = likeMapper.selectList(wrapper);

        boolean hasMore = false;
        cursor = null;
        if (videoLikes.size() > 20) {
            videoLikes = videoLikes.subList(0, videoLikes.size() - 1);
            hasMore = true;
            cursor = PageTokenUtils.encryptState(videoLikes.get(videoLikes.size() - 1));
        }

        return Result.success(CursorLoad.of(videoLikes, hasMore, cursor));
    }

    public Result<CursorLoad<VideoFavorites>> getFavoriteRecordsByUserId(Integer userId, String cursor) throws Exception {
        LambdaQueryWrapper<VideoFavorites> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VideoFavorites::getUserId, userId);
        wrapper.orderByDesc(VideoFavorites::getCreatedAt);
        wrapper.orderByDesc(VideoFavorites::getId);
        if (cursor != null) {
            VideoFavorites videoFavorites = PageTokenUtils.decryptState(cursor, VideoFavorites.class);
            wrapper.le(VideoFavorites::getCreatedAt, videoFavorites.getCreatedAt());
            wrapper.ne(VideoFavorites::getId, videoFavorites.getId());
        }
        wrapper.last("LIMIT 21");

        List<VideoFavorites> videoFavorites = favoriteMapper.selectList(wrapper);

        boolean hasMore = false;
        cursor = null;
        if (videoFavorites.size() > 20) {
            videoFavorites = videoFavorites.subList(0, videoFavorites.size() - 1);
            hasMore = true;
            cursor = PageTokenUtils.encryptState(videoFavorites.get(videoFavorites.size() - 1));
        }

        return Result.success(CursorLoad.of(videoFavorites, hasMore, cursor));
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

    public Result<List<VideoLikes>> getRecordsByBatchVIds(Integer uid, List<Integer> vid) {
        if (uid <= 0 || vid.isEmpty()) {
            return Result.success(List.of());
        }
        LambdaQueryWrapper<VideoLikes> wrapper = new LambdaQueryWrapper<>();
        // 用户ID
        wrapper.eq(VideoLikes::getUserId, uid);
        // 在这些记录中
        wrapper.in(VideoLikes::getVideoId, vid);

        List<VideoLikes> videoLikes = likeMapper.selectList(wrapper);

        return Result.success(videoLikes);
    }
}
