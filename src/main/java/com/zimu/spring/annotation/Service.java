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
// 表示这个注解会保留到运行时。
@Retention(RetentionPolicy.RUNTIME)
// 让 @Service 也具备 @Component 的含义。
@Component
// 定义业务层注解。
public @interface Service {

    // 允许用户给业务类 Bean 自定义名字。
    String value() default "";
}
