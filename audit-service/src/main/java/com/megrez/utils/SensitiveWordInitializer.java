package com.megrez.utils;

import com.megrez.mapper.SensitiveWordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SensitiveWordInitializer implements CommandLineRunner {

    @Autowired
    private SensitiveWordFilter filter;

    @Autowired
    private SensitiveWordMapper sensitiveWordMapper;

    @Override
    public void run(String... args) throws Exception {
        // 1. 从数据库查询所有敏感词
        List<String> words = sensitiveWordMapper.findAllWords();

        // 2. 调用过滤器的加载方法
        filter.loadWords(words);
    }
}