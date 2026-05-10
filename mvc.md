# mini Spring MVC 学习笔记

这份笔记对应项目里新增的迷你版 Spring MVC。

它不依赖 Tomcat、Servlet API、Spring MVC 或其他 Web 框架。

目的只有一个：

把 Spring MVC 的核心调用链用普通 Java 代码跑清楚。

---

## 1. 这次模拟了哪些角色

### 1.1 DispatcherServlet

源码：

- `src/main/java/com/zimu/mvc/servlet/DispatcherServlet.java`

它是前端控制器。

所有请求先进入它，再由它统一调度后面的组件。

核心流程：

```text
请求进来
-> HandlerMapping 找方法
-> Interceptor preHandle
-> HandlerAdapter 调用 Controller 方法
-> Interceptor postHandle
-> ViewResolver 解析视图
-> View 渲染结果
-> Interceptor afterCompletion
```

### 1.2 web.xml

源码：

- `src/main/java/com/zimu/mvc/servlet/MiniWebXml.java`

真实 Web 项目里，`web.xml` 会配置 `DispatcherServlet`。

当前项目不接 Tomcat，所以用 `MiniWebXml` 表达同样意思：

```java
DispatcherServlet dispatcherServlet = new MiniWebXml(AppConfig.class).buildDispatcherServlet();
```

### 1.3 HandlerMapping

源码：

- `src/main/java/com/zimu/mvc/handler/HandlerMapping.java`
- `src/main/java/com/zimu/mvc/handler/AnnotationHandlerMapping.java`

它负责根据请求找到 Controller 方法。

比如：

```text
POST /login
```

会找到：

```java
LoginController.doLogin(...)
```

### 1.4 HandlerAdapter

源码：

- `src/main/java/com/zimu/mvc/handler/HandlerAdapter.java`
- `src/main/java/com/zimu/mvc/handler/AnnotationHandlerAdapter.java`

它负责真正调用 Controller 方法。

比如：

```java
doLogin(String username, String password, HttpSession session)
```

会被反射调用成：

```java
method.invoke(loginController, "zimu", "123456", session)
```

### 1.5 Handler

源码：

- `src/main/java/com/zimu/demo/mvc/controller/LoginController.java`

这里的 Handler 就是 Controller 里的具体方法。

比如：

```java
@PostMapping("/login")
public Object doLogin(...)
```

### 1.6 ViewResolver

源码：

- `src/main/java/com/zimu/mvc/view/InternalResourceViewResolver.java`

它负责把逻辑视图名转成真实页面路径。

比如：

```text
login
```

会解析成：

```text
/WEB-INF/views/login.jsp
```

### 1.7 Interceptor

源码：

- `src/main/java/com/zimu/mvc/interceptor/HandlerInterceptor.java`
- `src/main/java/com/zimu/demo/mvc/interceptor/LoginInterceptor.java`

它负责在 Controller 前后做公共处理。

这次 demo 里实现的是登录拦截：

```text
/admin 开头的请求
-> 检查 session 中有没有 loginUser
-> 没有就 redirect:/login
```

---

## 2. 重点测试入口

源码：

- `src/test/java/com/zimu/mvc/MiniMvcTest.java`

它覆盖了 5 个场景：

1. `GET /login` 渲染登录页
2. 未登录访问 `/admin/home` 被拦截
3. `POST /login` 自动绑定参数并调用 `doLogin`
4. 登录后访问 `/admin/home` 正常放行
5. 密码错误时返回登录页和错误信息

运行：

```bash
mvn test
```

---

## 3. 一句话记住

Spring MVC 的核心不是 Controller 自己会被调用。

而是：

```text
DispatcherServlet 统一接请求，
HandlerMapping 找到方法，
HandlerAdapter 准备参数并反射调用方法，
ViewResolver 决定返回哪个页面，
Interceptor 在前后插入公共逻辑。
```
