package com.zimu.mvc.handler;

import com.zimu.mvc.annotation.GetMapping;
import com.zimu.mvc.annotation.PostMapping;
import com.zimu.mvc.http.MiniHttpRequest;
import com.zimu.spring.annotation.Controller;
import com.zimu.spring.context.ApplicationContext;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

// 基于注解的 HandlerMapping。
//
// 它模拟 Spring MVC 里的 RequestMappingHandlerMapping：
// 容器启动时扫描所有 @Controller Bean，
// 找到方法上的 @GetMapping / @PostMapping，
// 然后提前建立好 “请求 -> 方法” 的映射表。
//
// 为什么要启动时扫描？
// 因为每次请求来了再全项目反射扫描会很慢。
// 提前建好 Map，请求来了就能快速按 key 查找。
public class AnnotationHandlerMapping implements HandlerMapping {

    // 映射表。
    //
    // key 示例：
    // GET /login
    // POST /login
    private final Map<String, HandlerMethod> handlerMethodMap = new HashMap<>();

    // 创建时立即扫描 Controller。
    public AnnotationHandlerMapping(ApplicationContext applicationContext) {
        registerControllerMethods(applicationContext);
    }

    // 根据请求查找 Handler。
    @Override
    public HandlerMethod getHandler(MiniHttpRequest request) {
        return handlerMethodMap.get(buildKey(request.getMethod(), request.getPath()));
    }

    // 注册所有 Controller 方法。
    private void registerControllerMethods(ApplicationContext applicationContext) {
        // 从 mini Spring 容器里拿到所有 @Controller Bean。
        Map<String, Object> controllers = applicationContext.getBeansWithAnnotation(Controller.class);

        // 遍历每一个 Controller。
        for (Object controller : controllers.values()) {
            // 遍历 Controller 声明的方法。
            for (Method method : controller.getClass().getDeclaredMethods()) {
                registerGetMapping(controller, method);
                registerPostMapping(controller, method);
            }
        }
    }

    // 注册 @GetMapping 方法。
    private void registerGetMapping(Object controller, Method method) {
        if (!method.isAnnotationPresent(GetMapping.class)) {
            return;
        }

        String path = method.getAnnotation(GetMapping.class).value();
        registerHandlerMethod("GET", path, controller, method);
    }

    // 注册 @PostMapping 方法。
    private void registerPostMapping(Object controller, Method method) {
        if (!method.isAnnotationPresent(PostMapping.class)) {
            return;
        }

        String path = method.getAnnotation(PostMapping.class).value();
        registerHandlerMethod("POST", path, controller, method);
    }

    // 真正放入映射表。
    private void registerHandlerMethod(String httpMethod, String path, Object controller, Method method) {
        String key = buildKey(httpMethod, path);

        if (handlerMethodMap.containsKey(key)) {
            throw new IllegalStateException("重复的 MVC 映射: " + key);
        }

        handlerMethodMap.put(key, new HandlerMethod(controller, method));
    }

    // 构造映射 key。
    private String buildKey(String httpMethod, String path) {
        return httpMethod.toUpperCase() + " " + path;
    }
}
