package com.zimu.mvc.servlet;

import com.zimu.mvc.view.InternalResourceViewResolver;
import com.zimu.spring.context.ApplicationContext;

// 模拟 web.xml。
//
// 真实项目里，web.xml 负责告诉 Servlet 容器：
// 所有请求都交给 DispatcherServlet。
//
// 当前项目没有 Tomcat，也不依赖 Servlet API，
// 所以用这个类表达同样的配置关系：
//
// AppConfig -> ApplicationContext -> DispatcherServlet
public class MiniWebXml {

    // Spring 容器配置类。
    private final Class<?> configClass;

    public MiniWebXml(Class<?> configClass) {
        this.configClass = configClass;
    }

    // 创建前端控制器。
    public DispatcherServlet buildDispatcherServlet() {
        ApplicationContext applicationContext = new ApplicationContext(configClass);

        return new DispatcherServlet(
                applicationContext,
                new InternalResourceViewResolver("/WEB-INF/views/", ".jsp")
        );
    }
}
