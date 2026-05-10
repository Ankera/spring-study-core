package com.zimu.mvc.handler;

import java.lang.reflect.Method;

// Handler 的具体表现形式。
//
// 在 Spring MVC 里，Handler 最后通常会被包装成一个 HandlerMethod：
// - bean：Controller 对象
// - method：Controller 里的具体方法
//
// 比如：
//
// bean   = loginController
// method = doLogin(String username, String password, HttpSession session)
public class HandlerMethod {

    // Controller 对象。
    private final Object bean;

    // Controller 中真正处理请求的方法。
    private final Method method;

    public HandlerMethod(Object bean, Method method) {
        this.bean = bean;
        this.method = method;
    }

    public Object getBean() {
        return bean;
    }

    public Method getMethod() {
        return method;
    }
}
