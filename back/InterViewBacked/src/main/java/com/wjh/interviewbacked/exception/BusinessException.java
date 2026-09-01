package com.wjh.interviewbacked.exception;

/**
 * 业务异常：用于账号不存在、密码错误、重复注册等可预期错误
 */
public class BusinessException extends RuntimeException {

    /** 业务错误码，对应 ApiResult.code */
    private final int code;

    public BusinessException(String message) {
        this(1, message);
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
