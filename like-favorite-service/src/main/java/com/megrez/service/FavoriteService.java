package com.megrez.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.megrez.mapper.FavoriteMapper;
import com.megrez.mysql_entity.VideoFavorites;
import com.megrez.result.Response;
import com.megrez.result.Result;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FavoriteService {

    private final FavoriteMapper favoriteMapper;

    public FavoriteService(FavoriteMapper favoriteMapper) {
        this.favoriteMapper = favoriteMapper;
    }


    public Result<List<VideoFavorites>> get(Integer uid) {
        LambdaQueryWrapper<VideoFavorites> wrapper = new LambdaQueryWrapper<VideoFavorites>().eq(VideoFavorites::getUserId, uid);
        List<VideoFavorites> videoFavorites = favoriteMapper.selectList(wrapper);
        return Result.success(videoFavorites);
    }

    public Result<Void> add(Integer uid, Integer vid) {

        VideoFavorites favorites = VideoFavorites.builder().userId(uid).videoId(vid).build();
        int inserted = favoriteMapper.insert(favorites);
        return inserted == 0 ? Result.error(Response.PARAMS_WRONG) : Result.success(null);
    }

    public Result<Void> del(Integer uid, Integer vid) {
        LambdaQueryWrapper<VideoFavorites> wrapper = new LambdaQueryWrapper<VideoFavorites>()
                .eq(VideoFavorites::getUserId, uid)
                .eq(VideoFavorites::getVideoId, vid);
        favoriteMapper.delete(wrapper);
        return Result.success(null);
    }
}
