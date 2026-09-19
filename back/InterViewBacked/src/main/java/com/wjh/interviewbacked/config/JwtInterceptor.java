package com.wjh.interviewbacked.config;

import tools.jackson.databind.ObjectMapper;
import com.wjh.interviewbacked.common.ApiResult;
import com.wjh.interviewbacked.common.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT 鉴权拦截器：
 * - 登录 / 注册 / 登出 公开
 * - GET 读接口（笔记列表、简历快照、标签）公开
 * - 其余写接口与 /api/auth/me、/api/users/** 需携带有效 Bearer 令牌
 */
@Component
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JwtInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String method = request.getMethod();
        if ("OPTIONS".equals(method)) return true;

        String path = request.getRequestURI();
        // 登录 / 注册 / 登出 公开；注册与重置密码的前置步骤（邮箱验证码、账号查重、重置密码）
        // 也必须公开——用户此时还没有令牌，安全由邮箱验证码保证
        if (path.equals("/api/auth/login") || path.equals("/api/auth/register")
                || path.equals("/api/auth/logout") || path.equals("/api/auth/email-code")
                || path.equals("/api/auth/check-username") || path.equals("/api/auth/reset-password")) {
            return true;
        }

        // 任何携带合法 Bearer 令牌的请求都解析并设置 userId，
        // 使公开 GET 接口（如游客简历模板 / 个人简历）也能识别登录用户身份，
        // 从而返回用户自己的数据而非站长模板。无令牌或令牌失效时 userId 保持 null（走模板/匿名分支）。
        String header = request.getHeader("Authorization");
        String userId = null;
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            if (jwtUtil.validate(token)) {
                userId = jwtUtil.getUserId(token);
            }
        }
        if (userId != null) {
            request.setAttribute("userId", userId);
        }

        boolean needAuth = !"GET".equals(method)
                || path.equals("/api/auth/me")
                || path.startsWith("/api/users");
        if (!needAuth) return true;

        // 需要鉴权的接口：必须携带合法令牌
        if (userId == null) {
            writeUnauthorized(response, "未登录或缺少令牌");
            return false;
        }
        return true;
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(ApiResult.error(401, message)));
    }
}
