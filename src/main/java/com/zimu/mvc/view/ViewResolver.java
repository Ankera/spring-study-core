package com.zimu.mvc.view;

// 视图解析器接口。
//
// DispatcherServlet 拿到 Controller 返回的逻辑视图名后，
// 不应该自己拼 JSP 路径，而是交给 ViewResolver。
public interface ViewResolver {

    // 根据逻辑视图名解析出真正的 View。
    View resolveViewName(String viewName);
}
