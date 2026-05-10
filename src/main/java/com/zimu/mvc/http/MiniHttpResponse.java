package com.zimu.mvc.http;

import java.util.HashMap;
import java.util.Map;

// 迷你版 HTTP 响应对象。
//
// 真正的 ServletResponse 会把内容写回浏览器。
// 这里为了测试和学习，把结果保存成普通字段：
// - status：状态码
// - viewPath：最终转发到哪个 JSP
// - redirectPath：是否发生重定向
// - model：页面需要的数据
public class MiniHttpResponse {

    // 默认 200。
    private int status = 200;

    // 视图解析后的页面路径。
    private String viewPath;

    // 重定向路径。
    private String redirectPath;

    // 响应模型数据。
    private final Map<String, Object> model = new HashMap<>();

    // 设置页面转发结果。
    public void forward(String viewPath, Map<String, Object> model) {
        this.viewPath = viewPath;
        this.model.putAll(model);
    }

    // 设置重定向结果。
    public void redirect(String redirectPath) {
        this.status = 302;
        this.redirectPath = redirectPath;
    }

    // 设置错误状态。
    public void error(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public String getViewPath() {
        return viewPath;
    }

    public String getRedirectPath() {
        return redirectPath;
    }

    public Map<String, Object> getModel() {
        return model;
    }
}
