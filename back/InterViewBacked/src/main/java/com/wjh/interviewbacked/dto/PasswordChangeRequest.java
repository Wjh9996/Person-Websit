package com.wjh.interviewbacked.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 已登录用户修改密码：必须提供旧密码。
 */
@Data
public class PasswordChangeRequest {
    @NotBlank(message = "原密码不能为空")
    private String oldPassword;
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, message = "密码至少 6 位")
    private String newPassword;
}
