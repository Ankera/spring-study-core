package com.zimu.mvc;

import com.zimu.demo.config.AppConfig;
import com.zimu.mvc.http.HttpSession;
import com.zimu.mvc.http.MiniHttpRequest;
import com.zimu.mvc.http.MiniHttpResponse;
import com.zimu.mvc.servlet.DispatcherServlet;
import com.zimu.mvc.servlet.MiniWebXml;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

// mini Spring MVC 测试。
//
// 这个测试不是为了测浏览器，而是为了把 Spring MVC 核心流程跑清楚：
//
// web.xml -> DispatcherServlet
// DispatcherServlet -> HandlerMapping
// HandlerMapping -> LoginController 方法
// DispatcherServlet -> LoginInterceptor
// DispatcherServlet -> HandlerAdapter
// HandlerAdapter -> doLogin(username, password, session)
// DispatcherServlet -> ViewResolver
public class MiniMvcTest {

    // GET /login 应该被 HandlerMapping 找到，并解析成登录页 JSP 路径。
    @Test
    public void shouldRenderLoginPage() {
        DispatcherServlet dispatcherServlet = new MiniWebXml(AppConfig.class).buildDispatcherServlet();
        HttpSession session = new HttpSession();

        MiniHttpResponse response = dispatcherServlet.service(MiniHttpRequest.get("/login", session));

        assertEquals(200, response.getStatus());
        assertEquals("/WEB-INF/views/login.jsp", response.getViewPath());
    }

    // 未登录访问 /admin/home，应该被 LoginInterceptor 拦截并重定向到 /login。
    @Test
    public void shouldRedirectToLoginWhenUserNotLogin() {
        DispatcherServlet dispatcherServlet = new MiniWebXml(AppConfig.class).buildDispatcherServlet();
        HttpSession session = new HttpSession();

        MiniHttpResponse response = dispatcherServlet.service(MiniHttpRequest.get("/admin/home", session));

        assertEquals(302, response.getStatus());
        assertEquals("/login", response.getRedirectPath());
    }

    // POST /login 应该由 HandlerAdapter 自动绑定 username、password、session 并调用 doLogin。
    @Test
    public void shouldLoginAndRedirectToAdminHome() {
        DispatcherServlet dispatcherServlet = new MiniWebXml(AppConfig.class).buildDispatcherServlet();
        HttpSession session = new HttpSession();

        MiniHttpRequest request = MiniHttpRequest.post("/login", session)
                .param("username", "zimu")
                .param("password", "123456");

        MiniHttpResponse response = dispatcherServlet.service(request);

        assertEquals(302, response.getStatus());
        assertEquals("/admin/home", response.getRedirectPath());
        assertEquals("zimu", session.getAttribute("loginUser"));
    }

    // 登录后再次访问 /admin/home，拦截器应该放行，Controller 返回 admin/home 页面。
    @Test
    public void shouldRenderAdminHomeAfterLogin() {
        DispatcherServlet dispatcherServlet = new MiniWebXml(AppConfig.class).buildDispatcherServlet();
        HttpSession session = new HttpSession();
        session.setAttribute("loginUser", "zimu");

        MiniHttpResponse response = dispatcherServlet.service(MiniHttpRequest.get("/admin/home", session));

        assertEquals(200, response.getStatus());
        assertEquals("/WEB-INF/views/admin/home.jsp", response.getViewPath());
        assertEquals("zimu", response.getModel().get("username"));
    }

    // 登录失败时，Controller 返回 login 视图，并带上错误信息。
    @Test
    public void shouldReturnLoginPageWhenPasswordWrong() {
        DispatcherServlet dispatcherServlet = new MiniWebXml(AppConfig.class).buildDispatcherServlet();
        HttpSession session = new HttpSession();

        MiniHttpRequest request = MiniHttpRequest.post("/login", session)
                .param("username", "zimu")
                .param("password", "wrong");

        MiniHttpResponse response = dispatcherServlet.service(request);

        assertEquals(200, response.getStatus());
        assertEquals("/WEB-INF/views/login.jsp", response.getViewPath());
        assertEquals("用户名或密码错误", response.getModel().get("error"));
    }
}
