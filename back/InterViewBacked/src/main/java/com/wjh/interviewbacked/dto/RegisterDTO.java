package com.wjh.interviewbacked.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 注册入参（继承登录所需字段）
 */
@Data
public class RegisterDTO {
    @NotBlank(message = "账号不能为空")
    private String username;
    @NotBlank(message = "密码不能为空")
    private String password;
    private String nickname;
    @Email(message = "邮箱格式不正确")
    private String email;
}
