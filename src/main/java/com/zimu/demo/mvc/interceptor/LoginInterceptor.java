package com.zimu.demo.mvc.interceptor;

import com.zimu.mvc.handler.HandlerMethod;
import com.zimu.mvc.http.MiniHttpRequest;
import com.zimu.mvc.http.MiniHttpResponse;
import com.zimu.mvc.interceptor.HandlerInterceptor;
import com.zimu.spring.annotation.Component;

// 登录拦截器。
//
// 它模拟 Spring MVC 里的 LoginInterceptor：
// 在 Controller 方法执行前，先检查用户是否已经登录。
//
// 这里约定：
// - /admin 开头的路径需要登录
// - session 中有 loginUser 就表示已登录
@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(MiniHttpRequest request, MiniHttpResponse response, HandlerMethod handlerMethod) {
        // 不是 /admin 开头的请求，直接放行。
        if (!request.getPath().startsWith("/admin")) {
            return true;
        }

        // 已登录，放行。
        if (request.getSession().getAttribute("loginUser") != null) {
            return true;
        }

        // 未登录，重定向到登录页，并中断后续 Controller 调用。
        response.redirect("/login");
        return false;
    }
}
