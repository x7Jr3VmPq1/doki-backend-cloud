package com.megrez.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.megrez.annotation.CurrentUser;
import com.megrez.common.Result;
import com.megrez.dto.LoginByPasswordDTO;
import com.megrez.exception.CodeNotConsumedException;
import com.megrez.exception.PasswordWrongException;
import com.megrez.service.LoginAndRegisterService;
import com.megrez.service.SmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import com.megrez.exception.*;

import java.util.Map;

@RestController
@RequestMapping("/user")
public class LoginAndRegisterController {
    private static final Logger log = LoggerFactory.getLogger(LoginAndRegisterController.class);
    private final LoginAndRegisterService loginAndRegisterService;
    private final SmsService smsService;

    public LoginAndRegisterController(LoginAndRegisterService loginAndRegisterService, SmsService smsService) {
        this.loginAndRegisterService = loginAndRegisterService;
        this.smsService = smsService;
    }

    /**
     * 手机号登录
     *
     * @param phone
     * @param code
     * @return
     */
    @GetMapping("/loginByPhone")
    Result<Map<String, String>> loginByPhone(String phone, String code) {
        try {
            return Result.success(loginAndRegisterService.loginBySms(phone, code));
        } catch (JsonProcessingException e) {
            return Result.error("未知错误");
        } catch (InvalidCodeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取短信验证码
     *
     * @param phone
     * @return
     */
    @GetMapping("/getSmsCode")
    Result<String> getSmsCode(String phone) {
        try {
            return Result.success(smsService.sendCode(phone));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } catch (CodeNotConsumedException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 设置用户密码
     *
     * @param userId
     * @param password
     * @return
     */
    @GetMapping("/setPassword")
    Result<String> setPassword(@CurrentUser Long userId, String password) {
        loginAndRegisterService.setPassword(userId, password);
        return Result.success();
    }

    /**
     * 手机号密码登录
     *
     * @param login
     * @return
     */

    @PostMapping("/loginByPassword")
    Result<String> loginByPassword(@RequestBody LoginByPasswordDTO login) {
        log.info("用户登录：{}", login.getPhone());
        try {
            return Result.success(loginAndRegisterService.loginByPassword(login.getPhone(), login.getPassword()));
        } catch (PasswordWrongException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
