package com.megrez.service;

import com.megrez.mapper.LikeMapper;
import com.megrez.result.Result;
import org.springframework.stereotype.Service;

@Service
public class LikeService {
    private final LikeMapper likeMapper;

    public LikeService(LikeMapper likeMapper) {
        this.likeMapper = likeMapper;
    }


//    public Result<Void> addLikeRecord(Integer videoId){
//        likeMapper.insert()
//    }
}
