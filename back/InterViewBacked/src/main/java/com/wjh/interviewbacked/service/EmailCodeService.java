package com.wjh.interviewbacked.service;

/**
 * 邮箱验证码服务：发送与校验。
 */
public interface EmailCodeService {

    /** 注册场景 */
    String SCENE_REGISTER = "register";

    /** 重置密码场景 */
    String SCENE_RESET = "reset-password";

    /**
     * 发送验证码（带重发限流）。
     *
     * @param email 接收邮箱
     * @param scene 场景
     * @return 有效分钟数，便于前端提示
     */
    int send(String email, String scene);

    /**
     * 校验验证码，通过即作废该码（一次性）。
     *
     * @throws com.wjh.interviewbacked.exception.BusinessException 验证码错误/过期/未获取
     */
    void verify(String email, String scene, String code);
}
