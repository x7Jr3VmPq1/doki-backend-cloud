package com.megrez.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginSuccessVO {

    private String token;
    private boolean hasPassword;
}
