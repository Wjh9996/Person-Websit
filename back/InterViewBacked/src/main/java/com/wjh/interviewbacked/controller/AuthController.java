package com.wjh.interviewbacked.controller;

import com.wjh.interviewbacked.common.ApiResult;
import com.wjh.interviewbacked.dto.AuthResult;
import com.wjh.interviewbacked.dto.EmailCodeResult;
import com.wjh.interviewbacked.dto.LoginDTO;
import com.wjh.interviewbacked.dto.RegisterDTO;
import com.wjh.interviewbacked.dto.SendEmailCodeRequest;
import com.wjh.interviewbacked.dto.UserVO;
import com.wjh.interviewbacked.dto.UsernameCheckResult;
import com.wjh.interviewbacked.service.EmailCodeService;
import com.wjh.interviewbacked.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final EmailCodeService emailCodeService;
    private final int resendIntervalSeconds;

    public AuthController(UserService userService,
                          EmailCodeService emailCodeService,
                          @Value("${app.mail.code-resend-interval-seconds:60}") int resendIntervalSeconds) {
        this.userService = userService;
        this.emailCodeService = emailCodeService;
        this.resendIntervalSeconds = resendIntervalSeconds;
    }

    /** POST /api/auth/email-code —— 发送邮箱验证码（注册前置步骤，带重发限流） */
    @PostMapping("/email-code")
    public ApiResult<EmailCodeResult> sendEmailCode(@Valid @RequestBody SendEmailCodeRequest req) {
        String email = req.getEmail().trim().toLowerCase();
        String scene = StringUtils.hasText(req.getScene()) ? req.getScene() : EmailCodeService.SCENE_REGISTER;
        int minutes = emailCodeService.send(email, scene);
        return ApiResult.ok(new EmailCodeResult(true, minutes, resendIntervalSeconds));
    }

    /** GET /api/auth/check-username?username=xxx —— 账号唯一性预检查 */
    @GetMapping("/check-username")
    public ApiResult<UsernameCheckResult> checkUsername(@RequestParam("username") String username) {
        return ApiResult.ok(new UsernameCheckResult(userService.isUsernameAvailable(username)));
    }

    /** POST /api/auth/login */
    @PostMapping("/login")
    public ApiResult<AuthResult> login(@Valid @RequestBody LoginDTO dto, HttpServletResponse response) {
        AuthResult result = userService.login(dto);
        response.setHeader("Authorization", "Bearer " + result.getToken());
        return ApiResult.ok(result);
    }

    /** POST /api/auth/register */
    @PostMapping("/register")
    public ApiResult<AuthResult> register(@Valid @RequestBody RegisterDTO dto, HttpServletResponse response) {
        AuthResult result = userService.register(dto);
        response.setHeader("Authorization", "Bearer " + result.getToken());
        return ApiResult.ok(result);
    }

    /** POST /api/auth/logout（无状态，前端丢弃令牌即可） */
    @PostMapping("/logout")
    public ApiResult<Void> logout() {
        return ApiResult.ok();
    }

    /** GET /api/auth/me */
    @GetMapping("/me")
    public ApiResult<UserVO> me(@RequestAttribute("userId") String userId) {
        return ApiResult.ok(userService.getCurrentUser(userId));
    }
}
