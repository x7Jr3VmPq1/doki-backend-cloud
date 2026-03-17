package com.megrez.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.megrez.mongo_document.VideoComments;
import com.megrez.mysql_entity.SensitiveWords;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SensitiveWordMapper extends BaseMapper<SensitiveWords> {

    @Select("SELECT word FROM sensitive_words")
    List<String> findAllWords();
}
