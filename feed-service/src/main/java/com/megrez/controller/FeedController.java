package com.megrez.controller;

import com.megrez.result.Result;
import com.megrez.service.RandomService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/feed")
public class FeedController {
    private final RandomService randomService;

    public FeedController(RandomService randomService) {
        this.randomService = randomService;
    }

    @GetMapping("/random")
    public Result getRandomVideos() {
        return randomService.randomVideo();
    }
}
