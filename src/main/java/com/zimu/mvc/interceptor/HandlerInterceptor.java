package com.zimu.mvc.interceptor;

import com.zimu.mvc.handler.HandlerMethod;
import com.zimu.mvc.http.MiniHttpRequest;
import com.zimu.mvc.http.MiniHttpResponse;

// 迷你版拦截器接口。
//
// 它模拟 Spring MVC 的 HandlerInterceptor。
// 请求进入 Controller 前后，可以在这里统一处理公共逻辑。
//
// 典型用途：
// - 登录校验
// - 权限判断
// - 打印日志
public interface HandlerInterceptor {

    // Controller 调用前执行。
    //
    // 返回 true：继续执行 Controller。
    // 返回 false：中断请求，DispatcherServlet 不再调用 Controller。
    boolean preHandle(MiniHttpRequest request, MiniHttpResponse response, HandlerMethod handlerMethod);

    // Controller 调用后执行。
    default void postHandle(MiniHttpRequest request, MiniHttpResponse response, HandlerMethod handlerMethod, ModelAndViewAdapter modelAndViewAdapter) {
    }

    // 请求完成后执行。
    default void afterCompletion(MiniHttpRequest request, MiniHttpResponse response, HandlerMethod handlerMethod) {
    }

    // 为了避免拦截器直接依赖可变 ModelAndView，这里给一个很小的适配对象。
    interface ModelAndViewAdapter {
        String getViewName();
    }
}
