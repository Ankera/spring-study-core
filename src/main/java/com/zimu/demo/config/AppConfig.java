package com.zimu.demo.config;

// 导入普通 Bean 类型。
import com.zimu.demo.bean.SystemReporter;
// 导入 Bean 注解。
import com.zimu.spring.annotation.Bean;
// 导入包扫描注解。
import com.zimu.spring.annotation.ComponentScan;
// 导入配置类注解。
import com.zimu.spring.annotation.Configuration;
// 导入值注入注解。
import com.zimu.spring.annotation.Value;

// 标记这是一个配置类。
@Configuration
// 告诉容器要从 com.zimu 这个包开始扫描。
@ComponentScan("com.zimu")
// 这是整个示例项目的配置入口。
public class AppConfig {

    // 这个方法相当于 Spring 里用 @Bean 手动交给容器管理一个对象。
    @Bean
    public SystemReporter systemReporter(
            // 这里演示 @Value 也可以写在方法参数上。
            @Value("${app.name}") String appName,
            // 这里继续注入配置文件里的版本号。
            @Value("${app.version}") String appVersion
    ) {
        System.out.println("== systemReporter ==");
        // 返回一个普通对象，容器会把它注册成 Bean。
        return new SystemReporter(appName, appVersion);
    }
}
