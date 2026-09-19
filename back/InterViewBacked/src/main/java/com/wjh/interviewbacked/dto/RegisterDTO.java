package com.wjh.interviewbacked.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 注册入参（继承登录所需字段）
 *
 * 注册流程：邮箱必填且必须先通过验证码校验；账号仍可自定义，但必须唯一（与邮箱均唯一）。
 */
@Data
public class RegisterDTO {
    @NotBlank(message = "账号不能为空")
    private String username;
    @NotBlank(message = "密码不能为空")
    private String password;
    private String nickname;
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;
    /** 邮箱收到的 6 位验证码，校验通过才会真正创建账号 */
    @NotBlank(message = "验证码不能为空")
    private String code;
}
