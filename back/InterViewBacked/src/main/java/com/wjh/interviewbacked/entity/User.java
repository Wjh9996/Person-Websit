package com.wjh.interviewbacked.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体，对应数据库 user 表。
 * password 设为 transient，Jackson 默认不序列化该字段，避免响应泄露密码。
 */
@Data
public class User {
    private String id;
    private String username;
    private transient String password;
    private String nickname;
    private String email;
    /** 头像占位字符（后续可改为 url） */
    private String avatar;
    private String bio;
    private LocalDateTime createdAt;
}
