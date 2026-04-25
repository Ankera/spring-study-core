package com.zimu.demo.bean;

// 这是一个普通 Java 类，没有任何组件注解。
// 它之所以能进入容器，是因为在配置类里通过 @Bean 手动注册了。
public class SystemReporter {

    // 保存应用名字。
    private final String appName;

    // 保存应用版本。
    private final String appVersion;

    // 构造器接收配置类方法传进来的参数。
    public SystemReporter(String appName, String appVersion) {
        // 保存应用名字。
        this.appName = appName;
        // 保存应用版本。
        this.appVersion = appVersion;
    }

    // 打印一个总结信息。
    public void printSummary() {
        // 打印普通 Bean 的信息，证明它也被容器托管了。
        System.out.println("5. @Bean 创建的普通对象也在容器里，应用名: " + appName + "，版本: " + appVersion);
    }
}
