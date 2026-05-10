package com.zimu.mvc.handler;

import com.zimu.mvc.http.MiniHttpRequest;

// HandlerMapping 负责“根据请求找到处理方法”。
//
// DispatcherServlet 收到请求后，并不知道该调用哪个 Controller。
// 它会问 HandlerMapping：
//
// GET + /login 应该交给谁？
//
// HandlerMapping 返回 HandlerMethod 后，
// 后面的 HandlerAdapter 才负责真正调用方法。
public interface HandlerMapping {

    // 根据请求找到对应的 Handler。
    HandlerMethod getHandler(MiniHttpRequest request);
}
