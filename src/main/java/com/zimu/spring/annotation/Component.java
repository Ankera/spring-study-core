package com.zimu.spring.annotation;

// 导入运行时注解所需的类型。
import java.lang.annotation.ElementType;
// 导入保留策略类型。
import java.lang.annotation.Retention;
// 导入保留策略枚举。
import java.lang.annotation.RetentionPolicy;
// 导入目标位置类型。
import java.lang.annotation.Target;

// 表示这个注解可以放在类上面。
@Target(ElementType.TYPE)
// 表示这个注解会保留到运行时，这样反射才能读取到。
@Retention(RetentionPolicy.RUNTIME)
// 定义最基础的组件注解。
public @interface Component {

    // 允许用户自己给 Bean 起名字，默认空字符串。
    String value() default "";
}
