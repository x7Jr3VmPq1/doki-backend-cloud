package com.megrez.controller;

import com.megrez.annotation.CurrentUser;
import com.megrez.entity.User;
import com.megrez.result.Response;
import com.megrez.result.Result;
import com.megrez.service.UserService;
import com.megrez.utils.JWTUtil;
import com.megrez.utils.Validator;
import com.megrez.vo.user_service.UsersVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 根据用户名获取基本信息
     *
     * @param username 用户名
     * @return 用户名，头像，个人简介
     */
    @GetMapping("/{username}")
    public Result<User> getUserInfo(@PathVariable String username) {
        return userService.getByName(username);
    }

    /**
     * 更新指定用户信息
     *
     * @param user  用户信息表单
     * @param token token
     * @return 操作结果
     */
    @PutMapping("/update")
    public Result<User> update(@RequestBody User user,
                               @RequestHeader("Authorization") String token) {

        log.info("修改用户信息：{}", user.getId());
        if (token == null) {
            return Result.error(Response.UNAUTHORIZED);
        }
        if (!JWTUtil.isSameUser(user.getId(), token)) {
            return Result.error(Response.FORBIDDEN);
        }

        return userService.update(user);
    }


    /**
     * 根据用户ID批量获取用户信息
     *
     * @param userId    操作用户ID（不必须）
     * @param targetIds 目标用户ID集合
     * @return 用户信息，如果提供了userId，会返回 List<UsersVO>(带有是否关注的信息)
     * 否则会返回List<User>
     */
    @PostMapping("/userinfo")
    public Result<List<? extends User>> getUserinfoById(
            @RequestParam(required = false) Integer userId,
            @CurrentUser(required = false) Integer tokenUid,
            @RequestBody List<Integer> targetIds) {
        log.info("查询用户资料，IDs：{}", targetIds);
        if (targetIds.isEmpty()) {
            return Result.success(null);
        }
        // TODO 这是一个临时方案，由于前端的POST请求带不上查询参数
        return userService.getByIds(tokenUid == -1 ? userId : tokenUid, targetIds);
    }

    @GetMapping("/userinfo")
    public Result<User> getUserinfoByToken(@RequestHeader(value = "Authorization", required = false) String token) {
        log.info("获取用户信息：{}", token);
        if (token == null) {
            return Result.error(Response.FORBIDDEN);
        }
        return userService.getUserinfoByToken(token);
    }
}
