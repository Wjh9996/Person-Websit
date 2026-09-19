package com.wjh.interviewbacked.service.impl;

import com.wjh.interviewbacked.common.JwtUtil;
import com.wjh.interviewbacked.dto.AuthResult;
import com.wjh.interviewbacked.dto.RegisterDTO;
import com.wjh.interviewbacked.entity.User;
import com.wjh.interviewbacked.exception.BusinessException;
import com.wjh.interviewbacked.mapper.UserMapper;
import com.wjh.interviewbacked.service.EmailCodeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 注册逻辑单元测试：重点覆盖「账号/邮箱唯一」与「验证码校验通过才落库」。
 */
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserMapper userMapper;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private EmailCodeService emailCodeService;

    private UserServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new UserServiceImpl(userMapper, jwtUtil, emailCodeService);
    }

    private RegisterDTO dto(String username, String email, String code) {
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername(username);
        dto.setPassword("123456");
        dto.setEmail(email);
        dto.setCode(code);
        return dto;
    }

    @Test
    @DisplayName("账号已存在：409 且不再校验验证码、不落库")
    void register_whenUsernameTaken_shouldReject() {
        when(userMapper.selectByUsername("admin")).thenReturn(new User());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.register(dto("admin", "new@example.com", "123456")));
        assertEquals(409, ex.getCode());
        assertEquals("该账号已被注册", ex.getMessage());
        verify(emailCodeService, never()).verify(anyString(), anyString(), anyString());
        verify(userMapper, never()).insert(any(User.class));
    }

    @Test
    @DisplayName("邮箱已存在：409（一个邮箱只能注册一个账号）")
    void register_whenEmailTaken_shouldReject() {
        when(userMapper.selectByUsername("newbie")).thenReturn(null);
        when(userMapper.selectByEmail("taken@example.com")).thenReturn(new User());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.register(dto("newbie", "taken@example.com", "123456")));
        assertEquals(409, ex.getCode());
        assertEquals("该邮箱已被注册", ex.getMessage());
        verify(userMapper, never()).insert(any(User.class));
    }

    @Test
    @DisplayName("验证码校验失败：不创建账号（注册必须过邮箱验证）")
    void register_whenCodeInvalid_shouldNotCreateUser() {
        when(userMapper.selectByUsername("newbie")).thenReturn(null);
        when(userMapper.selectByEmail("new@example.com")).thenReturn(null);
        org.mockito.Mockito.doThrow(new BusinessException("验证码不正确"))
                .when(emailCodeService).verify("new@example.com", EmailCodeService.SCENE_REGISTER, "000000");

        assertThrows(BusinessException.class,
                () -> service.register(dto("newbie", "new@example.com", "000000")));
        verify(userMapper, never()).insert(any(User.class));
    }

    @Test
    @DisplayName("注册成功：落库、密码为 BCrypt 密文、邮箱转小写、返回令牌")
    void register_whenValid_shouldCreateUser() {
        when(userMapper.selectByUsername("newbie")).thenReturn(null);
        when(userMapper.selectByEmail("new@example.com")).thenReturn(null);
        when(jwtUtil.generateToken(anyString(), anyString())).thenReturn("jwt-token");

        AuthResult result = service.register(dto("newbie", "NEW@Example.com", "123456"));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).insert(captor.capture());
        User saved = captor.getValue();
        assertEquals("newbie", saved.getUsername());
        assertEquals("new@example.com", saved.getEmail(), "邮箱应统一转小写");
        assertNotEquals("123456", saved.getPassword(), "密码必须加密后落库");
        assertTrue(saved.getPassword().startsWith("$2"), "应为 BCrypt 密文");
        assertEquals(0, saved.getDeleted());
        assertEquals("jwt-token", result.getToken());
    }

    @Test
    @DisplayName("账号可用性检查：已存在返回 false，不存在返回 true")
    void isUsernameAvailable_shouldReflectExistence() {
        when(userMapper.selectByUsername("admin")).thenReturn(new User());
        when(userMapper.selectByUsername("brand-new")).thenReturn(null);

        assertFalse(service.isUsernameAvailable("admin"));
        assertTrue(service.isUsernameAvailable("brand-new"));
    }
}
