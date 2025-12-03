package com.megrez.client;

import com.megrez.mysql_entity.VideoFavorites;
import com.megrez.mysql_entity.VideoLikes;
import com.megrez.result.Result;
import com.megrez.vo.CursorLoad;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

// 一定记得在RequestParam里写上参数名字，不然无法正确绑定！
@FeignClient(name = "like-favorite-service")
public interface LikeFavoriteClient {

    /**
     * 判断用户是否点赞了某个视频
     *
     * @param userId  用户ID
     * @param videoId 视频ID
     * @return 判断结果
     */
    @GetMapping("/like/exist")
    Result<Boolean> existLikeRecord(
            @RequestParam("userId") Integer userId,
            @RequestParam("videoId") Integer videoId
    );

    /**
     * 获取用户点赞记录
     *
     * @param userId 目标用户
     * @param cursor 游标
     * @return 点赞记录集合
     */
    @GetMapping("/like/records")
    Result<CursorLoad<VideoLikes>> getLikeRecordsByUserId(
            @RequestParam("userId") Integer userId,
            @RequestParam("cursor") String cursor);


    /**
     * 获取指定数量的点赞记录
     *
     * @param userId 用户ID
     * @param count  数量
     * @return 点赞记录
     */
    @GetMapping("/like/records/count")
    Result<List<VideoLikes>> getLikeRecordsByCount(@RequestParam("userId") Integer userId,
                                                   @RequestParam("count") Integer count);


    /**
     * 根据用户ID和视频ID批量判断是否存在点赞
     *
     * @param uid 用户ID
     * @param vid 视频ID
     * @return 点赞记录
     */
    @GetMapping("/like/records/batch")
    Result<List<VideoLikes>> getLikeRecordsByBatchVIds(@RequestParam("uid") Integer uid,
                                                       @RequestParam("vid") List<Integer> vid);


    /**
     * 获取收藏记录
     *
     * @param uid 用户ID
     * @return 收藏记录列表
     */
    @GetMapping("/favorite")
    Result<List<VideoFavorites>> getFavorite(@RequestParam("uid") Integer uid);

    /**
     * 游标获取收藏记录
     *
     * @param uid    用户id
     * @param cursor 游标
     * @return 收藏列表
     */
    @GetMapping("/favorite/records")
    Result<CursorLoad<VideoFavorites>> getFavoriteRecordsByUserId(@RequestParam("uid") Integer uid,
                                                                  @RequestParam("cursor") String cursor);

}
