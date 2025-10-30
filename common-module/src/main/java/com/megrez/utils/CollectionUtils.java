package com.megrez.utils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CollectionUtils {

    public static <K, V> Map<K, V> toMap(Collection<V> list, Function<V, K> getter) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyMap();
        }
        return list.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        getter,                          // key 提取函数
                        Function.identity(),              // value 为对象本身
                        (existing, replacement) -> existing // 遇到重复 key 时保留第一个
                ));
    }

    public static <V, T> List<T> toList(Collection<V> list, Function<V, T> getter) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        return list.stream().filter(Objects::nonNull)
                .map(getter).toList();
    }
}
