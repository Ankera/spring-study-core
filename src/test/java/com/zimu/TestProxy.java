package com.zimu;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

// 1. 定义接口（JDK 动态代理必须基于接口）
interface UserService {
    void addUser(String name);
    String getUser(int id);
}

// 2. 真实实现类
class UserServiceImpl implements UserService {
    @Override
    public void addUser(String name) {
        System.out.println("【真实方法】添加用户: " + name);
    }

    @Override
    public String getUser(int id) {
        System.out.println("【真实方法】查询用户 ID: " + id);
        return "User-" + id;
    }
}

// 3. 代理逻辑处理器
class MyInvocationHandler implements InvocationHandler {
    // 被代理的真实对象
    private final Object target;

    public MyInvocationHandler(Object target) {
        this.target = target;
    }

    /**
     * proxy  : 代理对象本身（一般不用）
     * method : 被调用的方法
     * args   : 方法参数
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("【代理前置】开始执行: " + method.getName());

        // 执行真实对象的方法
        Object result = method.invoke(target, args);

        System.out.println("【代理后置】执行完毕: " + method.getName() + ", 返回值: " + result);
        return result;
    }
}

public class TestProxy {

    public static void main(String[] args) {
        // 真实对象
        UserService target = new UserServiceImpl();

        // 创建代理对象
        UserService proxy = (UserService) Proxy.newProxyInstance(
                target.getClass().getClassLoader(),           // 类加载器
                target.getClass().getInterfaces(),            // 代理需要实现的接口
                new MyInvocationHandler(target)               // 代理逻辑
        );

        // 调用代理方法
        proxy.addUser("张三");
        System.out.println("------");
        String user = proxy.getUser(1);
        System.out.println("最终拿到: " + user);
    }
}
