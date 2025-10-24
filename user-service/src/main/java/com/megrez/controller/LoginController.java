package com.megrez.controller;

import com.megrez.result.Response;
import com.megrez.result.Result;
import com.megrez.service.LoginService;
import com.megrez.service.SmsService;
import com.megrez.utils.Validator;
import com.megrez.vo.user_service.LoginSuccessVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class LoginController {


    private final LoginService loginService;
    private final SmsService smsService;

    public LoginController(LoginService loginService, SmsService smsService) {
        this.loginService = loginService;
        this.smsService = smsService;
    }

    /**
     * 短信验证码登录
     *
     * @param phone 手机号
     * @param code 短信验证码
     * @return token
     */
    @GetMapping("/loginBySms")
    Result<LoginSuccessVO> login(String phone, String code) {
        if (Validator.checkPhone(phone) && Validator.checkCode(code)) {
            return loginService.loginBySms(phone, code);
        }
        return Result.error(Response.VALIDATE_FAILED);
    }

    /**
     * 发送短信验证码
     *
     * @param phone 手机
     * @return 操作结果
     */
    @GetMapping("/getSmsCode")
    Result<Void> getSmsCode(String phone) {
        if (Validator.checkPhone(phone)) {
            return smsService.sendCode(phone);
        }
        return Result.error(Response.VALIDATE_FAILED);
    }
}
