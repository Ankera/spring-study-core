package com.zimu.mvc.view;

import com.zimu.mvc.http.MiniHttpResponse;

// 迷你版 View。
//
// 真正 Spring MVC 的 View 会负责把 model 渲染成 HTML。
// 这里不接 JSP 引擎，只把解析后的页面路径和 model 放进 response，
// 这样测试里能清楚看到 ViewResolver 的结果。
public class View {

    // 最终页面路径，比如 /WEB-INF/views/login.jsp。
    private final String viewPath;

    public View(String viewPath) {
        this.viewPath = viewPath;
    }

    // 渲染视图。
    public void render(ModelAndView modelAndView, MiniHttpResponse response) {
        response.forward(viewPath, modelAndView.getModel());
    }
}
