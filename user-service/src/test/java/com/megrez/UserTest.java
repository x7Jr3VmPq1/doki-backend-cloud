package com.megrez;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.megrez.mysql_entity.User;
import com.megrez.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class UserTest {

    private static final Logger log = LoggerFactory.getLogger(UserTest.class);
    @Autowired
    public UserMapper userMapper;

    @Test
    public void addUsers() {
        for (int i = 1; i <= 1000000; i++) {
            User user = new User();
            user.setPhoneNumber(String.valueOf(100000 + i));
            user.setUsername("测试用户" + i);
            user.setBio("我是测试用户" + i);
            user.setAvatarUrl("default.jpg");
            user.setIsTest(1);
            log.info("添加用户：{}", user);
            userMapper.add(user);
        }
    }

    @Test
    public void delTestUser() {
        userMapper.delete(new QueryWrapper<User>().eq("is_test", true));
    }
}
