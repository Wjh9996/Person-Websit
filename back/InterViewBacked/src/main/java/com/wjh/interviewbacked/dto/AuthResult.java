package com.wjh.interviewbacked.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录 / 注册返回结果：令牌 + 用户信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResult {
    private String token;
    private UserVO user;
}
