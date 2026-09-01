package com.wjh.interviewbacked.controller;

import com.wjh.interviewbacked.common.ApiResult;
import com.wjh.interviewbacked.dto.UserProfileUpdate;
import com.wjh.interviewbacked.dto.UserVO;
import com.wjh.interviewbacked.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /** PATCH /api/users/me */
    @PatchMapping("/me")
    public ApiResult<UserVO> updateProfile(@RequestAttribute("userId") String userId,
                                           @Valid @RequestBody UserProfileUpdate patch) {
        return ApiResult.ok(userService.updateProfile(userId, patch));
    }
}
