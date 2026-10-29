package com.inventory.config;

import com.inventory.entity.User;
import com.inventory.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final UserService userService;

    public AuthInterceptor(UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        
        // 放行登录注册退出页面、忘记密码、静态资源和邮箱验证接口
        if (uri.equals("/login") || uri.equals("/register") || uri.equals("/forgot-password") || uri.equals("/logout") || uri.startsWith("/css") || uri.startsWith("/js") || uri.startsWith("/api/email/")) {
            return true;
        }
        
        User sessionUser = (User) request.getSession().getAttribute("currentUser");
        if (sessionUser == null) {
            response.sendRedirect("/login");
            return false;
        }

        // 校验 session token 防止多设备登录
        String sessionToken = (String) request.getSession().getAttribute("sessionToken");
        User dbUser = userService.findById(sessionUser.getId());
        if (dbUser == null || dbUser.getSessionToken() == null || !dbUser.getSessionToken().equals(sessionToken)) {
            request.getSession().invalidate();
            response.sendRedirect("/login");
            return false;
        }

        if (uri.startsWith("/admin") && !"ADMIN".equals(sessionUser.getRole())) {
            response.sendRedirect("/");
            return false;
        }

        return true;
    }
}