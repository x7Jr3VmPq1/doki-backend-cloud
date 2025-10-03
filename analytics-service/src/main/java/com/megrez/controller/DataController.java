package com.megrez.controller;

import com.megrez.entity.VideoStatistics;
import com.megrez.result.Result;
import com.megrez.service.DataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/analytics/stat")
public class DataController {

    private final DataService dataService;
    private static final Logger log = LoggerFactory.getLogger(DataController.class);

    public DataController(DataService dataService) {
        this.dataService = dataService;
    }

    @PostMapping("/videos")
    public Result<List<VideoStatistics>> getVideoStatById(@RequestBody List<Integer> ids) {
        log.info("查询视频统计信息：{}", ids);
        return dataService.getVideoStatById(ids);
    }
}
