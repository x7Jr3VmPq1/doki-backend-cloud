package com.megrez;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.megrez.mysql_entity.User;
import com.megrez.mysql_entity.UserFollow;
import com.megrez.mapper.UserFollowMapper;
import com.megrez.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class TestSocial {

    private static final Logger log = LoggerFactory.getLogger(TestSocial.class);
    @Autowired
    public UserMapper userMapper;

    @Autowired
    public UserFollowMapper userFollowMapper;

    @Test
    public void TestAddFans() {
        List<User> users = userMapper.selectList(new LambdaQueryWrapper<User>().eq(User::getIsTest, 1).last("LIMIT 100"));
        for (User user : users) {
            UserFollow build = UserFollow.builder()
                    .followingId(10001)
                    .followerId(user.getId())
                    .isTest(1)
                    .build();
            log.info("添加粉丝：{}", build);

            userFollowMapper.insert(build);
        }
    }

    @Test
    public void TestAddFollows() {
        List<User> users = userMapper.selectList(new LambdaQueryWrapper<User>().eq(User::getIsTest, 1).last("LIMIT 100"));
        for (User user : users) {
            UserFollow build = UserFollow.builder()
                    .followingId(user.getId())
                    .followerId(10001)
                    .isTest(1)
                    .build();
            log.info("添加关注：{}", build);
            userFollowMapper.insert(build);
        }
    }
}
