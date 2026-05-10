package com.zimu.mvc.handler;

import com.zimu.mvc.http.MiniHttpRequest;
import com.zimu.mvc.http.MiniHttpResponse;
import com.zimu.mvc.view.ModelAndView;

// HandlerAdapter 负责“调用 Handler”。
//
// HandlerMapping 只负责找到方法，不负责调用。
// 为什么还要多一个 HandlerAdapter？
//
// 因为不同类型的 Handler 调用方式可能不同。
// 这里我们只支持 HandlerMethod，但仍然保留 Adapter 这一层，
// 是为了把 Spring MVC 的核心分工讲清楚：
//
// HandlerMapping：找谁处理
// HandlerAdapter：怎么调用它
public interface HandlerAdapter {

    // 调用 Handler，并返回 ModelAndView。
    ModelAndView handle(MiniHttpRequest request, MiniHttpResponse response, HandlerMethod handlerMethod);
}
