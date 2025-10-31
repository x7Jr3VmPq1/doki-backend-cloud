package com.megrez.controller;

import com.megrez.mysql_entity.Video;
import com.megrez.result.Result;
import com.megrez.service.RandomService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/feed")
public class FeedController {
    private final RandomService randomService;

    public FeedController(RandomService randomService) {
        this.randomService = randomService;
    }

    @GetMapping("/random")
    public Result<List<Video>> getRandomVideos() {
        return randomService.randomVideo();
    }
}
