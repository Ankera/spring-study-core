package com.zimu.spring.annotation;

// 导入运行时注解所需的类型。
import java.lang.annotation.ElementType;
// 导入保留策略类型。
import java.lang.annotation.Retention;
// 导入保留策略枚举。
import java.lang.annotation.RetentionPolicy;
// 导入目标位置类型。
import java.lang.annotation.Target;

// 表示这个注解可以放在方法上面。
@Target(ElementType.METHOD)
// 表示这个注解会保留到运行时。
@Retention(RetentionPolicy.RUNTIME)
// 定义 Bean 方法注解。
public @interface Bean {

    // 允许给方法产生的 Bean 自定义名字，默认空字符串。
    String value() default "";
}
