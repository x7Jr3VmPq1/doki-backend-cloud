package com.megrez.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.megrez.mysql_entity.SearchHistory;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SearchHistoryMapper extends BaseMapper<SearchHistory> {
}
