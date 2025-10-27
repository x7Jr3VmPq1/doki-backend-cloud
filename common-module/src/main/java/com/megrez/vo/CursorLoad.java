package com.megrez.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 游标加载评论响应体
 * list:具体的数据列表
 * hasMore:是否还有更多
 * cursor:游标，一个加密后的字符串，其中是分页依据
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CursorLoad<T> {
    private List<T> list = List.of();
    private Boolean hasMore = false;
    private String cursor = null;

    public static <T> CursorLoad<T> empty() {
        return new CursorLoad<>();
    }

    public static <T> CursorLoad<T> of(List<T> list, Boolean hasMore, String cursor) {
        CursorLoad<T> result = new CursorLoad<>();
        result.list = list;
        result.hasMore = hasMore;
        result.cursor = cursor;
        return result;
    }
}
