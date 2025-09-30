package com.megrez.client;

import com.megrez.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient("user-service")
public interface UserServiceClient {
    @GetMapping("/image/avatar/upload")
    Result uploadAvatar(@RequestBody Map<String, String> base64);
}
