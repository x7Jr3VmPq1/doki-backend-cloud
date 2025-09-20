package com.megrez.controller;

import com.megrez.result.Result;
import com.megrez.service.DraftService;
import com.megrez.vo.VideoDraftVO;
import org.apache.ibatis.annotations.Delete;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/video/upload/draft")
public class DraftController {

    private static final Logger log = LoggerFactory.getLogger(DraftController.class);
    private final DraftService draftService;

    public DraftController(DraftService draftService) {
        this.draftService = draftService;
    }

    /**
     * 根据用户ID获取未发布的草稿
     *
     * @param userId 用户ID
     * @return 草稿视图类
     */
    @GetMapping
    public Result get(Integer userId) {
        return draftService.get(10000);
    }

    @PostMapping("/create")
    public Result create(Integer userId) {
        return draftService.createDraft(10000);
    }

    @PutMapping("/update")
    public Result update(Integer userId, @RequestBody VideoDraftVO draft) {
        log.info("更新草稿：{}", draft);
        return draftService.updateDraft(10000, draft);
    }

    @DeleteMapping("/delete")
    public Result delete(Integer userId,
                         @RequestParam("draftId") Integer draftId) {
        log.info("删除草稿：{}", draftId);
        return draftService.delete(10000, draftId);
    }

    @PostMapping("/submit")
    public Result submit(Integer userId,
                         @RequestBody VideoDraftVO draft) {
        log.info("提交发布：{}", draft);
        return draftService.submit(10000, draft);
    }
}
