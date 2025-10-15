package com.megrez.client;

import com.megrez.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

// 一定记得在RequestParam里写上参数名字，不然无法正确绑定！
@FeignClient(name = "like-favorite-service", path = "/like")
public interface LikeFavoriteClient {

    /**
     * 判断用户是否点赞了某个视频
     *
     * @param userId  用户ID
     * @param videoId 视频ID
     * @return 判断结果
     */
    @GetMapping("/exist")
    Result<Boolean> existLikeRecord(
            @RequestParam("userId") Integer userId,
            @RequestParam("videoId") Integer videoId
    );
}
