package com.wjh.interviewbacked.service.impl;

import com.wjh.interviewbacked.common.JwtUtil;
import com.wjh.interviewbacked.dto.AuthResult;
import com.wjh.interviewbacked.dto.LoginDTO;
import com.wjh.interviewbacked.dto.PasswordChangeRequest;
import com.wjh.interviewbacked.dto.PasswordResetRequest;
import com.wjh.interviewbacked.dto.RegisterDTO;
import com.wjh.interviewbacked.dto.UserProfileUpdate;
import com.wjh.interviewbacked.dto.UserVO;
import com.wjh.interviewbacked.entity.User;
import com.wjh.interviewbacked.exception.BusinessException;
import com.wjh.interviewbacked.mapper.UserMapper;
import com.wjh.interviewbacked.service.EmailCodeService;
import com.wjh.interviewbacked.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final EmailCodeService emailCodeService;
    private final org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder encoder =
            new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();

    public UserServiceImpl(UserMapper userMapper, JwtUtil jwtUtil, EmailCodeService emailCodeService) {
        this.userMapper = userMapper;
        this.jwtUtil = jwtUtil;
        this.emailCodeService = emailCodeService;
    }

    @Override
    public AuthResult login(LoginDTO dto) {
        User user = userMapper.selectByUsername(dto.getUsername());
        if (user == null) throw new BusinessException("账号不存在");
        if (!encoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException("密码错误");
        }
        LocalDateTime now = LocalDateTime.now();
        userMapper.updateLastLogin(user.getId(), now, now);
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        return new AuthResult(token, UserVO.fromEntity(user));
    }

    /**
     * 注册：邮箱必填 + 验证码校验通过才落库，账号与邮箱均保证唯一。
     * 顺序说明：先查重再验码，避免被人用验证码接口探测某个邮箱/账号是否已注册。
     */
    @Override
    public AuthResult register(RegisterDTO dto) {
        String username = dto.getUsername().trim();
        String email = dto.getEmail().trim().toLowerCase();
        if (userMapper.selectByUsername(username) != null) {
            throw new BusinessException(409, "该账号已被注册");
        }
        if (userMapper.selectByEmail(email) != null) {
            throw new BusinessException(409, "该邮箱已被注册");
        }
        emailCodeService.verify(email, EmailCodeService.SCENE_REGISTER, dto.getCode());

        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setUsername(username);
        user.setPassword(encoder.encode(dto.getPassword()));
        String nickname = StringUtils.hasText(dto.getNickname()) ? dto.getNickname().trim() : username;
        user.setNickname(nickname);
        user.setEmail(email);
        user.setAvatar(nickname.substring(0, 1).toUpperCase());
        user.setBio("");
        user.setRole("user");
        user.setStatus(1);
        user.setDeleted(0);
        LocalDateTime now = LocalDateTime.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        userMapper.insert(user);

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        return new AuthResult(token, UserVO.fromEntity(user));
    }

    /**
     * 忘记密码重置。
     *
     * 顺序很关键：**先校验验证码，再判断邮箱是否已注册**。
     * 反过来的话，任何人都能拿这个接口去探测"某个邮箱是不是本站用户"（用户枚举）；
     * 而验证码只有邮箱主人拿得到，先验码等于给查询加了一道门槛。
     */
    @Override
    public void resetPassword(PasswordResetRequest req) {
        String email = req.getEmail().trim().toLowerCase();
        emailCodeService.verify(email, EmailCodeService.SCENE_RESET, req.getCode());
        User user = userMapper.selectByEmail(email);
        if (user == null) throw new BusinessException(404, "该邮箱未注册");
        userMapper.updatePassword(user.getId(), encoder.encode(req.getNewPassword()), LocalDateTime.now());
    }

    /** 已登录用户改密码：必须提供正确的原密码，防止拿到会话后直接改密 */
    @Override
    public void changePassword(String userId, PasswordChangeRequest req) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("用户不存在");
        if (!encoder.matches(req.getOldPassword(), user.getPassword())) {
            throw new BusinessException("原密码不正确");
        }
        userMapper.updatePassword(user.getId(), encoder.encode(req.getNewPassword()), LocalDateTime.now());
    }

    @Override
    public boolean isUsernameAvailable(String username) {
        if (!StringUtils.hasText(username)) return false;
        return userMapper.selectByUsername(username.trim()) == null;
    }

    @Override
    public UserVO getCurrentUser(String userId) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("用户不存在");
        return UserVO.fromEntity(user);
    }

    @Override
    public UserVO updateProfile(String userId, UserProfileUpdate patch) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("用户不存在");
        if (StringUtils.hasText(patch.getNickname())) user.setNickname(patch.getNickname());
        if (StringUtils.hasText(patch.getEmail())) user.setEmail(patch.getEmail());
        if (StringUtils.hasText(patch.getAvatar())) user.setAvatar(patch.getAvatar());
        if (patch.getBio() != null) user.setBio(patch.getBio());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.update(user);
        return UserVO.fromEntity(user);
    }
}
