package com.wjh.interviewbacked.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 验证码发送结果。
 */
@Data
@AllArgsConstructor
public class EmailCodeResult {
    /** 是否已发送 */
    private boolean sent;
    /** 有效期（分钟），前端用于提示用户 */
    private int expireMinutes;
    /** 重发间隔（秒），前端用于倒计时 */
    private int resendIntervalSeconds;
}
