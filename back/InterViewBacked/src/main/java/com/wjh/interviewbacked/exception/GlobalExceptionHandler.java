package com.wjh.interviewbacked.exception;

import com.wjh.interviewbacked.common.ApiResult;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器：将业务异常 / 校验异常统一包装为 ApiResult
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ApiResult<Void> handleBusiness(BusinessException e) {
        return ApiResult.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResult<Void> handleValid(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldError() != null
                ? e.getBindingResult().getFieldError().getDefaultMessage()
                : "参数校验失败";
        return ApiResult.error(1, msg);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ApiResult<Void> handleConstraint(ConstraintViolationException e) {
        return ApiResult.error(1, e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResult<Void>> handleOther(Exception e) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResult.error(500, "服务器内部错误：" + e.getMessage()));
    }
}
