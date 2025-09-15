package com.megrez.controller;

import com.megrez.result.Result;
import com.megrez.service.VideoUploadService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/video/upload")
public class VideoUploadController {

    private final VideoUploadService videoUploadService;

    public VideoUploadController(VideoUploadService videoUploadService) {
        this.videoUploadService = videoUploadService;
    }

    @PostMapping
    public Result upload(@RequestParam("file") MultipartFile video) {
        return videoUploadService.upload(video);
    }
}
