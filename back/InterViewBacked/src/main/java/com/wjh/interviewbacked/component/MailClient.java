package com.wjh.interviewbacked.component;

import com.wjh.interviewbacked.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 邮件发送客户端。
 *
 * 约定：
 * - 账号与 SMTP 授权码全部走环境变量（MAIL_USERNAME / MAIL_PASSWORD），不入库；
 * - 未配置账号时，若开启调试开关则把验证码打到日志便于本地联调，否则直接报错，
 *   避免"看起来发送成功但其实没发"的假象。
 */
@Component
public class MailClient {

    private static final Logger log = LoggerFactory.getLogger(MailClient.class);

    private final JavaMailSender sender;
    private final String from;
    private final boolean devLogCode;

    @Value("${spring.mail.username:}")
    private String username;

    @Value("${spring.mail.password:}")
    private String password;

    public MailClient(JavaMailSender sender,
                      @Value("${app.mail.from:}") String from,
                      @Value("${app.mail.dev-log-code:false}") boolean devLogCode) {
        this.sender = sender;
        this.from = from;
        this.devLogCode = devLogCode;
    }

    /** 邮件服务是否已配置好账号 */
    public boolean configured() {
        return StringUtils.hasText(username) && StringUtils.hasText(password);
    }

    /**
     * 发送注册验证码邮件。
     *
     * @param email   接收邮箱
     * @param code    6 位数字验证码
     * @param minutes 有效分钟数（仅用于文案）
     */
    public void sendVerifyCode(String email, String code, long minutes) {
        if (!configured()) {
            if (devLogCode) {
                // 仅用于本地联调：没有 SMTP 账号时，验证码打日志，前端照样能走完整流程
                log.warn("[dev] 邮件服务未配置，验证码已打印到日志 —— {} -> {}", email, code);
                return;
            }
            throw new BusinessException("邮件服务未配置，暂时无法发送验证码");
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(StringUtils.hasText(from) ? from : username);
        message.setTo(email);
        message.setSubject("【个人网站】注册验证码");
        message.setText("你好：\n\n"
                + "你正在注册个人网站账号，验证码为：" + code + "\n"
                + "该验证码 " + minutes + " 分钟内有效，请勿转发给他人。\n\n"
                + "如果不是你本人操作，请忽略本邮件。");
        sender.send(message);
        log.info("注册验证码已发送至 {}", email);
    }
}
