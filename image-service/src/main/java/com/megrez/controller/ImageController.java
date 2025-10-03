package com.megrez.controller;

import com.megrez.ImageType;
import com.megrez.result.Response;
import com.megrez.result.Result;
import com.megrez.service.ImageService;
import com.megrez.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/image")
public class ImageController {

    private static final Logger log = LoggerFactory.getLogger(ImageController.class);
    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    // 上传图片
    @PostMapping("/{type}/upload")
    public Result<String> upload(@RequestBody Map<String, String> base64,
                         @PathVariable String type) throws Exception {
        log.info("上传图片类型：{}", type);
        if (!FileUtils.isBase64(base64.get("base64"))) {
            log.info("错误的BASE64参数");
            return Result.error(Response.IMAGE_UPLOAD_DECODE_WRONG);
        }

        try {
            String filename = imageService.saveImage(base64.get("base64"), ImageType.fromString(type));
            return Result.success(filename);
        } catch (Exception e) {
            return Result.error(Response.IMAGE_UPLOAD_SAVE_WRONG);
        }
    }

    // 获取图片
    @GetMapping("/{type}/{filename}")
    public ResponseEntity<byte[]> getImage(@PathVariable String filename,
                                           @PathVariable String type) throws Exception {
        byte[] image = imageService.getImage(filename, ImageType.fromString(type));
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(image);
    }
}
