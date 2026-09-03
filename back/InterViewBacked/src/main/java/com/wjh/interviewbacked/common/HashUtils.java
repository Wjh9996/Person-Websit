package com.wjh.interviewbacked.common;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * 摘要工具：笔记正文 MD5 去重用。
 */
public class HashUtils {

    /** 计算字符串 MD5 十六进制（小写） */
    public static String md5Hex(String input) {
        if (input == null) input = "";
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                sb.append(Character.forDigit((b >> 4) & 0xF, 16));
                sb.append(Character.forDigit(b & 0xF, 16));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("MD5 计算失败", e);
        }
    }
}
