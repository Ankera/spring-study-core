package com.zimu.mvc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// 标在 Controller 方法参数上，表示这个参数来自请求参数。
//
// 为什么要这个注解？
// Java 反射默认不一定能拿到真实参数名。
// 所以用 @RequestParam("username") 明确告诉 HandlerAdapter：
// 请从 request 参数里取 username，再传给这个方法参数。
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestParam {

    // 请求参数名。
    String value();
}
