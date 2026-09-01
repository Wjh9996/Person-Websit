package com.wjh.interviewbacked.service;

import com.wjh.interviewbacked.dto.AuthResult;
import com.wjh.interviewbacked.dto.LoginDTO;
import com.wjh.interviewbacked.dto.RegisterDTO;
import com.wjh.interviewbacked.dto.UserProfileUpdate;
import com.wjh.interviewbacked.dto.UserVO;

public interface UserService {

    /** 登录，返回令牌 + 用户信息 */
    AuthResult login(LoginDTO dto);

    /** 注册，返回令牌 + 用户信息 */
    AuthResult register(RegisterDTO dto);

    /** 获取当前登录用户 */
    UserVO getCurrentUser(String userId);

    /** 修改个人资料（部分字段） */
    UserVO updateProfile(String userId, UserProfileUpdate patch);
}
