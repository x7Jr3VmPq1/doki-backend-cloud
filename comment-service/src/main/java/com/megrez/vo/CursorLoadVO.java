package com.megrez.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 游标加载评论响应体
 * list:具体的评论列表
 * hasMore:是否还有更多评论
 * cursor:游标，一个加密后的字符串，其中是分页依据
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CursorLoadVO {
    @Builder.Default
    private List<VideoCommentsVO> list = List.of();
    @Builder.Default
    private Boolean hasMore = false;
    @Builder.Default
    private String cursor = null;
}
