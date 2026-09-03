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
    /** 头像 URL（未来走对象存储），原为占位字符 VARCHAR(10) */
    private String avatar;
    private String bio;
    /** 角色：user / admin */
    private String role;
    /** 状态：1 正常 0 禁用 */
    private Integer status;
    /** 软删除：0 未删 1 已删 */
    private Integer deleted;
    /** 最近登录时间，用于限流/审计 */
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
