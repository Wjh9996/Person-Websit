package com.wjh.interviewbacked.service.impl;

import com.wjh.interviewbacked.component.MailClient;
import com.wjh.interviewbacked.entity.EmailVerifyCode;
import com.wjh.interviewbacked.exception.BusinessException;
import com.wjh.interviewbacked.mapper.EmailVerifyCodeMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 邮箱验证码服务的单元测试：覆盖发送限流、有效期、错误上限、一次性失效等安全策略。
 */
@ExtendWith(MockitoExtension.class)
class EmailCodeServiceImplTest {

    private static final String EMAIL = "tester@example.com";
    private static final String SCENE = "register";

    @Mock
    private EmailVerifyCodeMapper codeMapper;
    @Mock
    private MailClient mailClient;

    private EmailCodeServiceImpl service;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @BeforeEach
    void setUp() {
        service = new EmailCodeServiceImpl(codeMapper, mailClient, 5, 60);
    }

    private EmailVerifyCode record(String rawCode, LocalDateTime createdAt, LocalDateTime expiresAt,
                                   int used, int failCount) {
        EmailVerifyCode entity = new EmailVerifyCode();
        entity.setId("code-1");
        entity.setEmail(EMAIL);
        entity.setScene(SCENE);
        entity.setCodeHash(encoder.encode(rawCode));
        entity.setExpiresAt(expiresAt);
        entity.setCreatedAt(createdAt);
        entity.setUsed(used);
        entity.setFailCount(failCount);
        return entity;
    }

    @Test
    @DisplayName("首次发送：落库一条并调用邮件发送，返回有效期分钟数")
    void send_whenNoRecentCode_shouldInsertAndSend() {
        when(codeMapper.selectLatest(EMAIL, SCENE)).thenReturn(null);

        int minutes = service.send(EMAIL, SCENE);

        assertEquals(5, minutes);
        verify(codeMapper).insert(any(EmailVerifyCode.class));
        verify(mailClient).sendVerifyCode(anyString(), anyString(), anyLong());
    }

    @Test
    @DisplayName("60 秒内重复发送：被限流拒绝，且不再落库")
    void send_withinResendInterval_shouldBeRejected() {
        EmailVerifyCode recent = record("123456", LocalDateTime.now().minusSeconds(10),
                LocalDateTime.now().plusMinutes(5), 0, 0);
        when(codeMapper.selectLatest(EMAIL, SCENE)).thenReturn(recent);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.send(EMAIL, SCENE));
        assertTrue(ex.getMessage().contains("秒后再试"), "错误信息应提示剩余等待秒数");
        verify(codeMapper, never()).insert(any(EmailVerifyCode.class));
        verify(mailClient, never()).sendVerifyCode(anyString(), anyString(), anyLong());
    }

    @Test
    @DisplayName("超过重发间隔后可以再次发送")
    void send_afterInterval_shouldAllow() {
        EmailVerifyCode old = record("123456", LocalDateTime.now().minusSeconds(61),
                LocalDateTime.now().minusSeconds(1), 0, 0);
        when(codeMapper.selectLatest(EMAIL, SCENE)).thenReturn(old);

        service.send(EMAIL, SCENE);

        verify(codeMapper).insert(any(EmailVerifyCode.class));
    }

    @Test
    @DisplayName("验证码正确：校验通过并立即标记已使用（一次性）")
    void verify_withCorrectCode_shouldMarkUsed() {
        when(codeMapper.selectLatest(EMAIL, SCENE))
                .thenReturn(record("888888", LocalDateTime.now(), LocalDateTime.now().plusMinutes(5), 0, 0));

        service.verify(EMAIL, SCENE, "888888");

        verify(codeMapper).markUsed("code-1");
        verify(codeMapper, never()).incrementFailCount(anyString());
    }

    @Test
    @DisplayName("验证码错误：报错并累加失败次数")
    void verify_withWrongCode_shouldIncrementFailCount() {
        when(codeMapper.selectLatest(EMAIL, SCENE))
                .thenReturn(record("888888", LocalDateTime.now(), LocalDateTime.now().plusMinutes(5), 0, 0));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.verify(EMAIL, SCENE, "000000"));
        assertEquals("验证码不正确", ex.getMessage());
        verify(codeMapper).incrementFailCount("code-1");
        verify(codeMapper, never()).markUsed(anyString());
    }

    @Test
    @DisplayName("验证码已过期：直接拒绝，且不计入失败次数")
    void verify_whenExpired_shouldReject() {
        when(codeMapper.selectLatest(EMAIL, SCENE))
                .thenReturn(record("888888", LocalDateTime.now().minusMinutes(10),
                        LocalDateTime.now().minusMinutes(5), 0, 0));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.verify(EMAIL, SCENE, "888888"));
        assertTrue(ex.getMessage().contains("已过期"));
        verify(codeMapper, never()).markUsed(anyString());
    }

    @Test
    @DisplayName("失败次数达上限：拒绝校验（防止暴力枚举 6 位数字）")
    void verify_whenFailCountExceeded_shouldReject() {
        when(codeMapper.selectLatest(EMAIL, SCENE))
                .thenReturn(record("888888", LocalDateTime.now(), LocalDateTime.now().plusMinutes(5), 0, 5));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.verify(EMAIL, SCENE, "888888"));
        assertTrue(ex.getMessage().contains("错误次数过多"));
    }

    @Test
    @DisplayName("从未获取验证码：拒绝校验")
    void verify_whenNoCode_shouldReject() {
        when(codeMapper.selectLatest(EMAIL, SCENE)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.verify(EMAIL, SCENE, "888888"));
        assertEquals("请先获取验证码", ex.getMessage());
    }

    @Test
    @DisplayName("验证码已被使用过：不能复用")
    void verify_whenAlreadyUsed_shouldReject() {
        when(codeMapper.selectLatest(EMAIL, SCENE))
                .thenReturn(record("888888", LocalDateTime.now(), LocalDateTime.now().plusMinutes(5), 1, 0));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.verify(EMAIL, SCENE, "888888"));
        assertEquals("请先获取验证码", ex.getMessage());
    }
}
