package com.wjh.interviewbacked.controller;

import com.wjh.interviewbacked.common.ApiResult;
import com.wjh.interviewbacked.dto.AuthResult;
import com.wjh.interviewbacked.dto.LoginDTO;
import com.wjh.interviewbacked.dto.RegisterDTO;
import com.wjh.interviewbacked.dto.UserVO;
import com.wjh.interviewbacked.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
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
