package com.zimu.mvc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// 标在 Controller 方法上，表示这个方法处理 GET 请求。
//
// 在真正的 Spring MVC 里，HandlerMapping 会扫描 @GetMapping。
// 这里我们也做同样的事：启动时扫描 Controller Bean 的方法，
// 看到 @GetMapping("/login")，就建立映射：
//
// GET + /login -> LoginController.showLogin()
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface GetMapping {

    // 请求路径，比如 /login。
    String value();
}
