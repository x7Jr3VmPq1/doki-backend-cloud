package com.megrez.controller;

import com.megrez.annotation.CurrentUser;
import com.megrez.es_document.ESSearchHistory;
import com.megrez.es_document.UserESDocument;
import com.megrez.mysql_entity.SearchHistory;
import com.megrez.result.Result;
import com.megrez.service.SearchService;
import com.megrez.vo.search_service.SearchVO;
import com.megrez.vo.user_service.UsersVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/search")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    /**
     * 获取全站搜索历史（返回前十）
     *
     * @return 结果列表
     */
    @GetMapping("/history")
    public Result<List<SearchHistory>> getSearchHistory() {
        return searchService.getSearchHistory();
    }

    /**
     * 根据关键词搜索视频
     *
     * @param keyword 关键词
     * @return 视频列表
     */
    @GetMapping
    public Result<List<SearchVO>> search(
            @CurrentUser(required = false) Integer userId,
            @RequestParam String keyword) {
        return searchService.search(userId, keyword);
    }

    /**
     * 根据关键词搜索用户
     *
     * @param userId  用户ID
     * @param keyword 关键词
     * @return 用户列表
     */
    @GetMapping("/user")
    public Result<List<UserESDocument>> searchUsers(@CurrentUser(required = false) Integer userId,
                                                    @RequestParam String keyword) {
        return searchService.searchUsers(userId, keyword);
    }

    @GetMapping("/suggest")
    public Result<List<String>> getSuggestion(String pre) {
        return searchService.getSuggestion(pre);
    }
}
