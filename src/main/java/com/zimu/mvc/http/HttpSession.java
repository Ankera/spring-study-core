package com.zimu.mvc.http;

import java.util.HashMap;
import java.util.Map;

// 迷你版 Session。
//
// 真实 Web 项目里通常用 javax.servlet.http.HttpSession 或 jakarta.servlet.http.HttpSession。
// 但这个项目要求不依赖任何框架，所以我们自己写一个最小版。
//
// 它的核心作用只有一个：
// 在多次请求之间保存用户状态，比如登录后的 loginUser。
public class HttpSession {

    // 用 Map 保存 session 属性。
    private final Map<String, Object> attributes = new HashMap<>();

    // 保存属性。
    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    // 读取属性。
    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    // 删除属性。
    public void removeAttribute(String name) {
        attributes.remove(name);
    }
}
