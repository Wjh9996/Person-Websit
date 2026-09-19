package com.wjh.interviewbacked.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 账号可用性检查结果。
 */
@Data
@AllArgsConstructor
public class UsernameCheckResult {
    /** true 表示可以使用 */
    private boolean available;
}
