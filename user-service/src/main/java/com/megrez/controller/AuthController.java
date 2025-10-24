package com.megrez.controller;

import com.megrez.annotation.CurrentUser;
import com.megrez.result.Result;
import com.megrez.service.AuthService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 授权服务，发放授权码和获取token
 */
@RestController
@RequestMapping("/user/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 获取一次性授权码
     *
     * @param userId 用户ID
     * @return 授权码
     */
    @GetMapping("/code")
    public Result<String> getAuthCode(@CurrentUser Integer userId) {
        return authService.getAuthCode(userId);
    }

    /**
     * 使用授权码交换token
     *
     * @param code 授权码
     * @return token
     */
    @GetMapping("/token")
    public Result<String> getTokenByAuthCode(@RequestParam("userId") Integer userId,
                                             @RequestParam("code") String code) {
        return authService.getTokenByAuthCode(userId, code);
    }
}
