package com.zimu.mvc.servlet;

import com.zimu.mvc.handler.AnnotationHandlerAdapter;
import com.zimu.mvc.handler.AnnotationHandlerMapping;
import com.zimu.mvc.handler.HandlerAdapter;
import com.zimu.mvc.handler.HandlerMapping;
import com.zimu.mvc.handler.HandlerMethod;
import com.zimu.mvc.http.MiniHttpRequest;
import com.zimu.mvc.http.MiniHttpResponse;
import com.zimu.mvc.interceptor.HandlerInterceptor;
import com.zimu.mvc.view.ModelAndView;
import com.zimu.mvc.view.View;
import com.zimu.mvc.view.ViewResolver;
import com.zimu.spring.context.ApplicationContext;

import java.util.List;

// 迷你版 DispatcherServlet，也就是前端控制器。
//
// 在真实 Spring MVC 里，它通常配置在 web.xml：
//
// <servlet>
//   <servlet-name>dispatcherServlet</servlet-name>
//   <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
// </servlet>
//
// 这个类的核心职责是统一调度：
// 1. 找 Handler
// 2. 执行 Interceptor.preHandle
// 3. 用 HandlerAdapter 调用 Controller 方法
// 4. 执行 Interceptor.postHandle
// 5. 用 ViewResolver 解析视图
// 6. 渲染响应
// 7. 执行 Interceptor.afterCompletion
public class DispatcherServlet {

    // 根据请求找 Controller 方法。
    private final HandlerMapping handlerMapping;

    // 负责反射调用 Controller 方法。
    private final HandlerAdapter handlerAdapter;

    // 负责把逻辑视图名解析成真实页面路径。
    private final ViewResolver viewResolver;

    // 拦截器列表。
    private final List<HandlerInterceptor> interceptors;

    public DispatcherServlet(ApplicationContext applicationContext, ViewResolver viewResolver) {
        this.handlerMapping = new AnnotationHandlerMapping(applicationContext);
        this.handlerAdapter = new AnnotationHandlerAdapter();
        this.viewResolver = viewResolver;
        this.interceptors = applicationContext.getBeansAssignableTo(HandlerInterceptor.class);
    }

    // 处理一次模拟 HTTP 请求。
    public MiniHttpResponse service(MiniHttpRequest request) {
        MiniHttpResponse response = new MiniHttpResponse();

        // 1. HandlerMapping：根据 method + path 找到 Handler。
        HandlerMethod handlerMethod = handlerMapping.getHandler(request);
        if (handlerMethod == null) {
            response.error(404);
            return response;
        }

        try {
            // 2. Interceptor：Controller 执行前拦截。
            for (HandlerInterceptor interceptor : interceptors) {
                boolean shouldContinue = interceptor.preHandle(request, response, handlerMethod);
                if (!shouldContinue) {
                    return response;
                }
            }

            // 3. HandlerAdapter：真正调用 Controller 方法。
            ModelAndView modelAndView = handlerAdapter.handle(request, response, handlerMethod);

            // 4. Interceptor：Controller 执行后回调。
            for (HandlerInterceptor interceptor : interceptors) {
                interceptor.postHandle(request, response, handlerMethod, modelAndView::getViewName);
            }

            // 5. 处理 redirect: 前缀。
            if (modelAndView.getViewName().startsWith("redirect:")) {
                response.redirect(modelAndView.getViewName().substring("redirect:".length()));
                return response;
            }

            // 6. ViewResolver：把逻辑视图名转成真实页面路径。
            View view = viewResolver.resolveViewName(modelAndView.getViewName());

            // 7. View：渲染响应。
            view.render(modelAndView, response);
            return response;
        } finally {
            // 8. 请求完成后的收尾回调。
            for (HandlerInterceptor interceptor : interceptors) {
                interceptor.afterCompletion(request, response, handlerMethod);
            }
        }
    }
}
