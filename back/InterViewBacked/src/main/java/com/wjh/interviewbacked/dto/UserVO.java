package com.wjh.interviewbacked.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户对外视图对象（不含密码）
 */
@Data
public class UserVO {
    private String id;
    private String username;
    private String nickname;
    private String email;
    private String avatar;
    private String bio;
    private LocalDateTime createdAt;

    public static UserVO fromEntity(com.wjh.interviewbacked.entity.User u) {
        if (u == null) return null;
        UserVO vo = new UserVO();
        vo.setId(u.getId());
        vo.setUsername(u.getUsername());
        vo.setNickname(u.getNickname());
        vo.setEmail(u.getEmail());
        vo.setAvatar(u.getAvatar());
        vo.setBio(u.getBio());
        vo.setCreatedAt(u.getCreatedAt());
        return vo;
    }
}
