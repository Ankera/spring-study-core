package com.zimu.mvc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// 标在 Controller 方法上，表示这个方法处理 POST 请求。
//
// 比如：
//
// @PostMapping("/login")
// public String doLogin(...)
//
// HandlerMapping 会把 POST + /login 映射到这个方法。
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PostMapping {

    // 请求路径，比如 /login。
    String value();
}
