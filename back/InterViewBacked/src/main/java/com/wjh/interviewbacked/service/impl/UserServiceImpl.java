package com.wjh.interviewbacked.service.impl;

import com.wjh.interviewbacked.common.JwtUtil;
import com.wjh.interviewbacked.dto.AuthResult;
import com.wjh.interviewbacked.dto.LoginDTO;
import com.wjh.interviewbacked.dto.RegisterDTO;
import com.wjh.interviewbacked.dto.UserProfileUpdate;
import com.wjh.interviewbacked.dto.UserVO;
import com.wjh.interviewbacked.entity.User;
import com.wjh.interviewbacked.exception.BusinessException;
import com.wjh.interviewbacked.mapper.UserMapper;
import com.wjh.interviewbacked.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder encoder =
            new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();

    public UserServiceImpl(UserMapper userMapper, JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.jwtUtil = jwtUtil;
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

    @Override
    public AuthResult register(RegisterDTO dto) {
        if (userMapper.selectByUsername(dto.getUsername()) != null) {
            throw new BusinessException("该账号已被注册");
        }
        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setUsername(dto.getUsername());
        user.setPassword(encoder.encode(dto.getPassword()));
        String nickname = StringUtils.hasText(dto.getNickname()) ? dto.getNickname() : dto.getUsername();
        user.setNickname(nickname);
        user.setEmail(dto.getEmail());
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
