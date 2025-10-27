package com.megrez.controller;

import com.megrez.annotation.CurrentUser;
import com.megrez.result.Result;
import com.megrez.service.DraftService;
import com.megrez.vo.videoupload_service.VideoDraftVO;
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
    public Result<VideoDraftVO> get(@CurrentUser Integer userId) {
        return draftService.get(userId);
    }

    @PostMapping("/create")
    public Result<VideoDraftVO> create(@CurrentUser Integer userId) {
        return draftService.createDraft(userId);
    }

    @PutMapping("/update")
    public Result<VideoDraftVO> update(@CurrentUser Integer userId, @RequestBody VideoDraftVO draft) {
        log.info("更新草稿：{}", draft);
        return draftService.updateDraft(userId, draft);
    }

    @DeleteMapping("/delete")
    public Result<Void> delete(@CurrentUser Integer userId,
                               @RequestParam("draftId") Integer draftId) {
        log.info("删除草稿：{}", draftId);
        return draftService.delete(userId, draftId);
    }

    @PostMapping("/submit")
    public Result<Void> submit(@CurrentUser Integer userId,
                               @RequestBody VideoDraftVO draft) {
        log.info("提交发布：{}", draft);
        return draftService.submit(userId, draft);
    }
}
