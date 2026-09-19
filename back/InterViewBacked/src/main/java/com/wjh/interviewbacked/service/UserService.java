package com.wjh.interviewbacked.service;

import com.wjh.interviewbacked.dto.AuthResult;
import com.wjh.interviewbacked.dto.LoginDTO;
import com.wjh.interviewbacked.dto.PasswordChangeRequest;
import com.wjh.interviewbacked.dto.PasswordResetRequest;
import com.wjh.interviewbacked.dto.RegisterDTO;
import com.wjh.interviewbacked.dto.UserProfileUpdate;
import com.wjh.interviewbacked.dto.UserVO;

public interface UserService {

    /** 登录，返回令牌 + 用户信息 */
    AuthResult login(LoginDTO dto);

    /** 注册，返回令牌 + 用户信息（注册前必须已通过邮箱验证码校验） */
    AuthResult register(RegisterDTO dto);

    /** 账号是否可用（唯一性预检查，注册页实时提示用） */
    boolean isUsernameAvailable(String username);

    /** 忘记密码：校验邮箱验证码后重置密码 */
    void resetPassword(PasswordResetRequest req);

    /** 已登录用户修改密码：必须校验原密码 */
    void changePassword(String userId, PasswordChangeRequest req);

    /** 获取当前登录用户 */
    UserVO getCurrentUser(String userId);

    /** 修改个人资料（部分字段） */
    UserVO updateProfile(String userId, UserProfileUpdate patch);
}
