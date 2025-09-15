package com.megrez.client;

import com.megrez.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient("image-service")
public interface ImageServiceClient {

    /**
     * 上传头像服务
     *
     * @param base64 头像BASE64字符串
     * @return 文件名
     */
    @PostMapping("/image/avatar/upload")
    Result uploadAvatar(@RequestBody Map<String, String> base64);

    /**
     * 上传封面图服务
     *
     * @param base64 封面图BASE64字符串
     * @return 文件名
     */
    @PostMapping("/image/cover/upload")
    Result uploadCover(@RequestBody Map<String, String> base64);


}
