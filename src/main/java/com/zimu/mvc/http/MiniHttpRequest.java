package com.zimu.mvc.http;

import java.util.HashMap;
import java.util.Map;

// 迷你版 HTTP 请求对象。
//
// 真实 Spring MVC 运行在 Servlet 容器里，请求来自浏览器和 Tomcat。
// 这里为了学习主流程，用普通 Java 对象模拟一次请求。
//
// DispatcherServlet 只需要知道：
// - 请求方法：GET / POST
// - 请求路径：/login
// - 请求参数：username=...
// - 当前会话：session
public class MiniHttpRequest {

    // HTTP 方法。
    private final String method;

    // 请求路径。
    private final String path;

    // 请求参数。
    private final Map<String, String> parameters = new HashMap<>();

    // 当前请求关联的 session。
    private final HttpSession session;

    // 创建请求对象。
    public MiniHttpRequest(String method, String path, HttpSession session) {
        this.method = method.toUpperCase();
        this.path = path;
        this.session = session;
    }

    // 快速创建 GET 请求。
    public static MiniHttpRequest get(String path, HttpSession session) {
        return new MiniHttpRequest("GET", path, session);
    }

    // 快速创建 POST 请求。
    public static MiniHttpRequest post(String path, HttpSession session) {
        return new MiniHttpRequest("POST", path, session);
    }

    // 添加请求参数，并返回自身，方便测试里链式写法。
    public MiniHttpRequest param(String name, String value) {
        parameters.put(name, value);
        return this;
    }

    // 返回 HTTP 方法。
    public String getMethod() {
        return method;
    }

    // 返回请求路径。
    public String getPath() {
        return path;
    }

    // 根据名字取请求参数。
    public String getParameter(String name) {
        return parameters.get(name);
    }

    // 返回 session。
    public HttpSession getSession() {
        return session;
    }
}
