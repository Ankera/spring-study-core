package com.zimu.mvc.view;

// 模拟 Spring MVC 里的 InternalResourceViewResolver。
//
// 它做的事非常朴素：
//
// 逻辑视图名 login
// + prefix /WEB-INF/views/
// + suffix .jsp
// = /WEB-INF/views/login.jsp
public class InternalResourceViewResolver implements ViewResolver {

    // 视图前缀。
    private final String prefix;

    // 视图后缀。
    private final String suffix;

    public InternalResourceViewResolver(String prefix, String suffix) {
        this.prefix = prefix;
        this.suffix = suffix;
    }

    @Override
    public View resolveViewName(String viewName) {
        return new View(prefix + viewName + suffix);
    }
}
