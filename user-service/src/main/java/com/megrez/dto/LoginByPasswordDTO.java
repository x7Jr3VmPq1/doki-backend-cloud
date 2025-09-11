package com.megrez.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 手机号密码登录DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginByPasswordDTO {
    private String phone;
    private String password;
}
