package com.zimu.demo.mvc.controller;

import com.zimu.mvc.annotation.GetMapping;
import com.zimu.mvc.annotation.PostMapping;
import com.zimu.mvc.annotation.RequestParam;
import com.zimu.mvc.http.HttpSession;
import com.zimu.mvc.view.ModelAndView;
import com.zimu.spring.annotation.Controller;

// 登录控制器。
//
// 在这条 mini MVC 链路里，它就是你说的 Handler：
// HandlerMapping 找到的不是一个普通字符串，而是这个类里的某个方法。
//
// 比如：
// POST /login -> doLogin(String username, String password, HttpSession session)
@Controller
public class LoginController {

    // 显示登录页。
    //
    // HandlerMapping 启动时会扫描到：
    // GET + /login -> showLogin()
    @GetMapping("/login")
    public String showLogin() {
        // 返回逻辑视图名。
        // 后面会交给 InternalResourceViewResolver 解析成 /WEB-INF/views/login.jsp。
        return "login";
    }

    // 处理登录提交。
    //
    // HandlerAdapter 会自动做参数绑定：
    // - username 来自请求参数 username
    // - password 来自请求参数 password
    // - session 来自当前请求的 HttpSession
    @PostMapping("/login")
    public Object doLogin(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            HttpSession session
    ) {
        // 为了教学简单，这里写死用户名密码。
        if ("zimu".equals(username) && "123456".equals(password)) {
            // 登录成功后，把用户信息放入 session。
            session.setAttribute("loginUser", username);

            // redirect: 前缀表示重定向。
            return "redirect:/admin/home";
        }

        // 登录失败时，返回登录页并携带错误信息。
        return new ModelAndView("login")
                .addObject("error", "用户名或密码错误");
    }

    // 登录后才能访问的页面。
    //
    // LoginInterceptor 会在 Controller 执行前检查 session。
    @GetMapping("/admin/home")
    public ModelAndView home(HttpSession session) {
        return new ModelAndView("admin/home")
                .addObject("username", session.getAttribute("loginUser"));
    }
}
