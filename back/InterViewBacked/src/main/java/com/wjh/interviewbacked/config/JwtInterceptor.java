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
        if (path.equals("/api/auth/login") || path.equals("/api/auth/register") || path.equals("/api/auth/logout")) {
            return true;
        }

        boolean needAuth = !"GET".equals(method)
                || path.equals("/api/auth/me")
                || path.startsWith("/api/users");
        if (!needAuth) return true;

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            writeUnauthorized(response, "未登录或缺少令牌");
            return false;
        }
        String token = header.substring(7);
        if (!jwtUtil.validate(token)) {
            writeUnauthorized(response, "令牌无效或已过期");
            return false;
        }
        request.setAttribute("userId", jwtUtil.getUserId(token));
        return true;
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(ApiResult.error(401, message)));
    }
}
