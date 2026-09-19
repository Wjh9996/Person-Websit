package com.wjh.interviewbacked.service.impl;

import com.wjh.interviewbacked.component.MailClient;
import com.wjh.interviewbacked.entity.EmailVerifyCode;
import com.wjh.interviewbacked.exception.BusinessException;
import com.wjh.interviewbacked.mapper.EmailVerifyCodeMapper;
import com.wjh.interviewbacked.service.EmailCodeService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 邮箱验证码实现。
 *
 * 安全设计（面试可讲的四道闸门）：
 * 1. 重发限流：同一邮箱同一场景在 N 秒内只能发一次，防止短信/邮件轰炸；
 * 2. 有效期：默认 5 分钟，过期作废；
 * 3. 哈希存储：库里只存 BCrypt 哈希，拖库也拿不到明文验证码；
 * 4. 错误上限 + 一次性：累计错 5 次作废、校验通过立即 used=1，堵住 6 位数字枚举。
 */
@Service
public class EmailCodeServiceImpl implements EmailCodeService {

    /** 单条验证码最大校验失败次数 */
    private static final int MAX_FAIL_COUNT = 5;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final EmailVerifyCodeMapper codeMapper;
    private final MailClient mailClient;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    private final int expireMinutes;
    private final int resendIntervalSeconds;

    public EmailCodeServiceImpl(EmailVerifyCodeMapper codeMapper,
                                MailClient mailClient,
                                @Value("${app.mail.code-expire-minutes:5}") int expireMinutes,
                                @Value("${app.mail.code-resend-interval-seconds:60}") int resendIntervalSeconds) {
        this.codeMapper = codeMapper;
        this.mailClient = mailClient;
        this.expireMinutes = expireMinutes;
        this.resendIntervalSeconds = resendIntervalSeconds;
    }

    @Override
    public int send(String email, String scene) {
        EmailVerifyCode latest = codeMapper.selectLatest(email, scene);
        if (latest != null && latest.getCreatedAt() != null) {
            long seconds = java.time.Duration.between(latest.getCreatedAt(), LocalDateTime.now()).getSeconds();
            if (seconds < resendIntervalSeconds) {
                throw new BusinessException("验证码已发送，请 " + (resendIntervalSeconds - seconds) + " 秒后再试");
            }
        }

        String code = generateCode();
        EmailVerifyCode entity = new EmailVerifyCode();
        entity.setId(UUID.randomUUID().toString());
        entity.setEmail(email);
        entity.setScene(scene);
        entity.setCodeHash(encoder.encode(code));
        entity.setExpiresAt(LocalDateTime.now().plusMinutes(expireMinutes));
        entity.setUsed(0);
        entity.setFailCount(0);
        entity.setCreatedAt(LocalDateTime.now());
        codeMapper.insert(entity);

        mailClient.sendVerifyCode(email, code, expireMinutes);
        return expireMinutes;
    }

    @Override
    public void verify(String email, String scene, String code) {
        EmailVerifyCode latest = codeMapper.selectLatest(email, scene);
        if (latest == null || latest.getUsed() != null && latest.getUsed() == 1) {
            throw new BusinessException("请先获取验证码");
        }
        if (latest.getExpiresAt() == null || latest.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException("验证码已过期，请重新获取");
        }
        if (latest.getFailCount() != null && latest.getFailCount() >= MAX_FAIL_COUNT) {
            throw new BusinessException("验证码错误次数过多，请重新获取");
        }
        if (!encoder.matches(code, latest.getCodeHash())) {
            codeMapper.incrementFailCount(latest.getId());
            throw new BusinessException("验证码不正确");
        }
        // 校验通过立即作废，避免同一条验证码被复用
        codeMapper.markUsed(latest.getId());
    }

    /** 6 位数字验证码 */
    private String generateCode() {
        int value = RANDOM.nextInt(1_000_000);
        return String.format("%06d", value);
    }
}
