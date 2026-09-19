package com.wjh.interviewbacked.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 邮箱验证码实体，对应数据库 email_verify_code 表。
 *
 * 安全约定：
 * - 验证码只以 BCrypt 哈希落库（code_hash），任何情况下不存明文；
 * - 一条验证码一次性使用（used=1 后失效）；
 * - 失败次数 fail_count 达到上限后作废，防止暴力枚举 6 位数字。
 */
@Data
public class EmailVerifyCode {
    private String id;
    /** 接收邮箱 */
    private String email;
    /** 场景：register / reset-password */
    private String scene;
    /** 验证码的 BCrypt 哈希 */
    private String codeHash;
    /** 过期时间 */
    private LocalDateTime expiresAt;
    /** 是否已使用：0 未使用 1 已使用 */
    private Integer used;
    /** 校验失败次数 */
    private Integer failCount;
    private LocalDateTime createdAt;
}
