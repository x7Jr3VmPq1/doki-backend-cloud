package com.megrez.client;

import com.megrez.dto.social_service.CheckFollowDTO;
import com.megrez.mysql_entity.UserFollow;
import com.megrez.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "social-service", path = "/social")
public interface SocialServiceClient {
    @PostMapping("/follow/check")
    Result<List<UserFollow>> checkFollow(@RequestBody CheckFollowDTO dto);
}
