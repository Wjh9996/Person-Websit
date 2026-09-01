package com.wjh.interviewbacked.dto;

import lombok.Data;

/**
 * 个人资料修改入参（均为可选字段，对应前端 Partial<User>）
 */
@Data
public class UserProfileUpdate {
    private String nickname;
    private String email;
    private String avatar;
    private String bio;
}
