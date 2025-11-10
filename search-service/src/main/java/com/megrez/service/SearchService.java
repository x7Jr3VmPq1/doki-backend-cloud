package com.megrez.service;

import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.megrez.client.AnalyticsServiceClient;
import com.megrez.client.SocialServiceClient;
import com.megrez.client.UserServiceClient;
import com.megrez.client.VideoInfoClient;
import com.megrez.constant.GatewayHttpPath;
import com.megrez.dto.social_service.CheckFollowDTO;
import com.megrez.es_document.ESSearchHistory;
import com.megrez.es_document.UserESDocument;
import com.megrez.es_document.VideoESDocument;
import com.megrez.mapper.SearchHistoryMapper;
import com.megrez.mysql_entity.*;
import com.megrez.result.Result;
import com.megrez.utils.CollectionUtils;
import com.megrez.vo.search_service.SearchVO;
import com.megrez.vo.user_service.UsersVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;

import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.HighlightQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.core.query.highlight.Highlight;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightField;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightFieldParameters;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Map;

@Service
public class SearchService {

    private static final Logger log = LoggerFactory.getLogger(SearchService.class);
    private final SearchHistoryMapper mapper;
    private final ElasticsearchOperations operations;
    private final UserServiceClient userServiceClient;
    private final VideoInfoClient videoInfoClient;
    private final AnalyticsServiceClient analyticsServiceClient;
    private final SocialServiceClient socialServiceClient;

    public SearchService(SearchHistoryMapper mapper, ElasticsearchOperations operations, UserServiceClient userServiceClient, VideoInfoClient videoInfoClient, AnalyticsServiceClient analyticsServiceClient, SocialServiceClient socialServiceClient) {
        this.mapper = mapper;
        this.operations = operations;
        this.userServiceClient = userServiceClient;
        this.videoInfoClient = videoInfoClient;
        this.analyticsServiceClient = analyticsServiceClient;
        this.socialServiceClient = socialServiceClient;
    }

    /**
     * 获取前十的热搜
     *
     * @return 热搜列表
     */
    public Result<List<SearchHistory>> getSearchHistory() {
        // 只要前十个。
        LambdaQueryWrapper<SearchHistory> queryWrapper = new LambdaQueryWrapper<SearchHistory>()
                .orderByDesc(SearchHistory::getCount)
                .last("LIMIT 10");
        return Result.success(mapper.selectList(queryWrapper));
    }


    /**
     * 获取搜索补全建议
     *
     * @param pre 前缀
     * @return 补全列表
     */
    public Result<List<String>> getSuggestion(String pre) {
        Query query = new NativeQueryBuilder()
                .withQuery(q -> q.match(m -> m
                        .query(pre)
                        .field("word")
                ))
                .build();

        SearchHits<ESSearchHistory> result = operations.search(query, ESSearchHistory.class);
        List<ESSearchHistory> list = result.stream().map(SearchHit::getContent).toList();

        List<String> words = list.stream().map(ESSearchHistory::getWord).toList();
        return Result.success(words);
    }

    public Result<List<SearchVO>> search(Integer userId, String keyword) {
        Query query = new NativeQueryBuilder()
                .withQuery(q -> q
                        .functionScore(fs -> fs
                                .query(inner -> inner
                                        .multiMatch(mm -> mm
                                                .query(keyword)
                                                .fields("title", "description", "username")
                                        )
                                )
                                .functions(
                                        List.of(
                                                // 根据点赞加权
                                                FunctionScore.of(f -> f
                                                        .fieldValueFactor(ff -> ff
                                                                .field("likeCount")
                                                                .factor(1.0)       // 放大系数
                                                                .modifier(FieldValueFactorModifier.Log1p) // 评分函数
                                                        )
                                                ),
                                                // 根据评论加权
                                                FunctionScore.of(f -> f
                                                        .fieldValueFactor(ff -> ff
                                                                .field("commentCount")
                                                                .factor(1.0)
                                                                .modifier(FieldValueFactorModifier.Log1p)
                                                        )
                                                ),
                                                // 根据播放加权
                                                FunctionScore.of(f -> f
                                                        .fieldValueFactor(ff -> ff
                                                                .field("views")
                                                                .factor(1.0)
                                                                .modifier(FieldValueFactorModifier.Log1p)
                                                        )
                                                )

                                        )
                                )
                                .scoreMode(FunctionScoreMode.Sum)
                                .boostMode(FunctionBoostMode.Multiply)

                        )
                )
                .withHighlightQuery(new HighlightQuery(
                        new Highlight(
                                List.of(
                                        new HighlightField("title",
                                                HighlightFieldParameters.builder()
                                                        .withPreTags("<em>")
                                                        .withPostTags("</em>")
                                                        .build()
                                        )
                                )
                        ),
                        null
                ))
                .withSort(s -> s
                        .field(f -> f.field("_score").order(SortOrder.Desc))
                )
//                .withSearchAfter(List.of(123)) // 某个分数
                .withMaxResults(21)
                .build();

        SearchHits<VideoESDocument> hits = operations.search(query, VideoESDocument.class);

        if (hits.getSearchHits().isEmpty()) {
            return Result.success(List.of());
        }

        List<VideoESDocument> results = hits.getSearchHits()
                .stream()
                .map(SearchHit::getContent)
                .toList();

        // 提取出视频ID和用户ID
        List<Integer> vIds = results.stream().map(document -> Integer.parseInt(document.getId())).toList();
        List<Integer> uIds = results.stream().map(VideoESDocument::getUserId).toList();

        Result<List<Video>> videoInfoByIds = videoInfoClient.getVideoInfoByIds(vIds);
        Result<List<VideoStatistics>> videoStatById = analyticsServiceClient.getVideoStatById(vIds);
        Result<List<User>> userinfoById = userServiceClient.getUserinfoById(uIds);

        if (!videoInfoByIds.isSuccess() || !videoStatById.isSuccess() || !userinfoById.isSuccess()) {
            log.error("搜索服务故障：外部服务调用失败");
            throw new RuntimeException();
        }

        Map<Integer, Video> videoMap = CollectionUtils.toMap(videoInfoByIds.getData(), Video::getId);
        Map<Integer, VideoStatistics> statisticsMap = CollectionUtils.toMap(videoStatById.getData(), VideoStatistics::getVideoId);
        Map<Integer, User> userMap = CollectionUtils.toMap(userinfoById.getData(), User::getId);

        List<SearchVO> list = hits.stream().map(hit -> {

            String highlightedTitle = null;
            if (hit.getHighlightFields().containsKey("title")) {
                highlightedTitle = hit.getHighlightFields().get("title").get(0);
            }
            VideoESDocument document = hit.getContent();

            int vid = Integer.parseInt(document.getId());
            Video video = videoMap.get(vid);
            VideoStatistics videoStatistics = statisticsMap.get(vid);
            User user = userMap.get(document.getUserId());
            return new SearchVO(video, user, videoStatistics, highlightedTitle);
        }).toList();

        return Result.success(list);
    }


    public Result<List<UsersVO>> searchUsers(Integer userId, String keyword) {

        NativeQuery query = new NativeQueryBuilder().withQuery(
                q -> q.multiMatch(mm -> mm.query(keyword).fields(List.of("id", "username", "bio")))
        ).build();
        SearchHits<UserESDocument> hits = operations.search(query, UserESDocument.class);

        if (hits.getSearchHits().isEmpty()) {
            return Result.success(List.of());
        }

        List<UserESDocument> list = hits.stream().map(SearchHit::getContent).toList();

        // 如果有userId,则判断关注状态
        Map<Integer, UserFollow> followMap;
        if (userId != null && userId > 0) {
            List<Integer> ids = list.stream().map(UserESDocument::getUserId).toList();

            CheckFollowDTO checkFollowDTO = new CheckFollowDTO();
            checkFollowDTO.setUid(userId);
            checkFollowDTO.setTargetIds(ids);
            Result<List<UserFollow>> result = socialServiceClient.checkFollow(checkFollowDTO);

            if (!result.isSuccess()) {
                log.error("社交服务调用失败,{}", result.getMsg());
                throw new RuntimeException("社交服务调用失败");
            }

            List<UserFollow> followList = result.getData();
            followMap = CollectionUtils.toMap(followList, UserFollow::getFollowingId);
        } else {
            followMap = Map.of();
        }

        List<UsersVO> voList = list.stream().map(doc -> {
            UsersVO vo = new UsersVO();
            BeanUtils.copyProperties(doc, vo);
            vo.setId(doc.getUserId());
            vo.setFollowed(followMap.containsKey(doc.getUserId()));
            UserStatistics statistics = new UserStatistics();
            statistics.setFollowerCount(doc.getFollowerCount());
            statistics.setFollowingCount(doc.getFollowingCount());
            statistics.setLikeCount(doc.getLikeCount());
            vo.setStat(statistics);
            vo.setAvatarUrl(GatewayHttpPath.AVATAR + vo.getAvatarUrl());
            return vo;
        }).toList();

        return Result.success(voList);
    }
}
