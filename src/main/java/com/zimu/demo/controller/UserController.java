package com.zimu.demo.controller;

// 导入服务类。
import com.zimu.demo.service.UserService;
// 导入自动注入注解。
import com.zimu.spring.annotation.Autowired;
// 导入控制器注解。
import com.zimu.spring.annotation.Controller;
// 导入值注入注解。
import com.zimu.spring.annotation.Value;

// 标记这是控制层组件。
@Controller
// 控制器负责接收“请求”并调用业务层。
public class UserController {

    // 这个字段演示从配置文件中读取端口。
    @Value("${server.port}")
    private int port;

    // 控制器依赖业务层对象。
    @Autowired
    private UserService userService;

    // 提供一个无参构造器，方便演示字段注入版的 @Autowired。
    public UserController() {
    }

    public UserController(String username) {

    }

    // 模拟一个查询用户的方法。
    public void queryUser() {
        // 打印一行，表示控制器开始工作。
        System.out.println("1. Controller 收到请求，当前服务端口: " + port);

        // 调用业务层方法并接收结果。
        String result = userService.getUserInfo();

        // 把最终结果打印出来。
        System.out.println("4. Controller 把结果返回给用户: " + result);
    }
}
