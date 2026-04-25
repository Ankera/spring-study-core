package com.zimu;

// 导入我们自己写的配置类。
import com.zimu.demo.config.AppConfig;
// 导入我们自己写的控制器类。
import com.zimu.demo.controller.UserController;
// 导入我们自己写的普通 Bean 类。
import com.zimu.demo.bean.SystemReporter;
// 导入我们自己写的容器类。
import com.zimu.spring.context.ApplicationContext;

// 这是程序启动入口。
public class Main {

    // main 方法就是 Java 程序最先执行的地方。
    public static void main(String[] args) {
        System.out.println("hello world");
        // 创建我们自己的容器，并把配置类传进去。
        ApplicationContext applicationContext = new ApplicationContext(AppConfig.class);

        // 从容器里拿到控制器对象。
        UserController userController = applicationContext.getBean(UserController.class);

        // 调用控制器方法，模拟一次“请求处理”。
        userController.queryUser();

        // 再从容器里拿到 @Bean 创建出来的对象。
        SystemReporter systemReporter = applicationContext.getBean(SystemReporter.class);

        // 调用普通 Bean 的方法，看看 @Bean 和 @Value 是否生效。
        systemReporter.printSummary();
    }
}
