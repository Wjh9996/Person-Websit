package com.wjh.interviewbacked.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 忘记密码后通过邮箱重置：邮箱 + 验证码 + 新密码。
 *
 * 验证码与注册共用 email_verify_code 表，但 scene 不同（reset-password），互不干扰。
 */
@Data
public class PasswordResetRequest {
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;
    @NotBlank(message = "验证码不能为空")
    private String code;
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, message = "密码至少 6 位")
    private String newPassword;
}
