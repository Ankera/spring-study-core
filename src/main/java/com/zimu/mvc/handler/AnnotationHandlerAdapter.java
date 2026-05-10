package com.zimu.mvc.handler;

import com.zimu.mvc.annotation.RequestParam;
import com.zimu.mvc.http.HttpSession;
import com.zimu.mvc.http.MiniHttpRequest;
import com.zimu.mvc.http.MiniHttpResponse;
import com.zimu.mvc.view.ModelAndView;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

// 基于注解和反射的 HandlerAdapter。
//
// 它模拟 Spring MVC 自动调用 Controller 方法的过程：
//
// doLogin(String username, String password, HttpSession session)
//
// 需要被转换成：
//
// method.invoke(loginController, "zhangsan", "123456", session)
//
// 也就是说，HandlerAdapter 的重点不是找方法，而是：
// - 看方法需要哪些参数
// - 从 request/session/response 中准备这些参数
// - 用反射真正调用 Controller 方法
public class AnnotationHandlerAdapter implements HandlerAdapter {

    @Override
    public ModelAndView handle(MiniHttpRequest request, MiniHttpResponse response, HandlerMethod handlerMethod) {
        try {
            Method method = handlerMethod.getMethod();

            // 根据方法参数列表，组装反射调用需要的参数数组。
            Object[] args = resolveMethodArguments(method, request, response);

            // 允许访问非 public 方法，方便教学 demo。
            method.setAccessible(true);

            // 真正调用 Controller 方法。
            Object returnValue = method.invoke(handlerMethod.getBean(), args);

            // 把 Controller 的返回值统一转成 ModelAndView。
            return adaptReturnValue(returnValue);
        } catch (Exception exception) {
            throw new RuntimeException("调用 MVC Handler 失败: " + handlerMethod.getMethod().getName(), exception);
        }
    }

    // 解析 Controller 方法参数。
    private Object[] resolveMethodArguments(Method method, MiniHttpRequest request, MiniHttpResponse response) {
        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];

        for (int index = 0; index < parameters.length; index++) {
            Parameter parameter = parameters[index];
            Class<?> parameterType = parameter.getType();

            // 如果参数类型是 HttpSession，直接把当前 session 传进去。
            if (parameterType == HttpSession.class) {
                args[index] = request.getSession();
                continue;
            }

            // 如果参数类型是 MiniHttpRequest，直接把 request 传进去。
            if (parameterType == MiniHttpRequest.class) {
                args[index] = request;
                continue;
            }

            // 如果参数类型是 MiniHttpResponse，直接把 response 传进去。
            if (parameterType == MiniHttpResponse.class) {
                args[index] = response;
                continue;
            }

            // 普通参数从请求参数里取。
            // 优先读 @RequestParam("username")。
            // 如果没有写 @RequestParam，就尝试使用 Java 反射里的参数名。
            String parameterName = resolveRequestParameterName(parameter);
            String rawValue = request.getParameter(parameterName);

            args[index] = convertValue(rawValue, parameterType);
        }

        return args;
    }

    // 解析请求参数名。
    private String resolveRequestParameterName(Parameter parameter) {
        if (parameter.isAnnotationPresent(RequestParam.class)) {
            return parameter.getAnnotation(RequestParam.class).value();
        }

        return parameter.getName();
    }

    // 把字符串参数转成目标类型。
    private Object convertValue(String rawValue, Class<?> targetType) {
        if (targetType == String.class) {
            return rawValue;
        }

        if (targetType == int.class || targetType == Integer.class) {
            return Integer.parseInt(rawValue);
        }

        if (targetType == long.class || targetType == Long.class) {
            return Long.parseLong(rawValue);
        }

        throw new IllegalArgumentException("暂不支持的 MVC 方法参数类型: " + targetType.getName());
    }

    // 统一适配 Controller 返回值。
    private ModelAndView adaptReturnValue(Object returnValue) {
        if (returnValue == null) {
            return new ModelAndView("");
        }

        // Controller 直接返回 ModelAndView。
        if (returnValue instanceof ModelAndView modelAndView) {
            return modelAndView;
        }

        // Controller 返回 String 时，把它当成逻辑视图名。
        if (returnValue instanceof String viewName) {
            return new ModelAndView(viewName);
        }

        throw new IllegalArgumentException("暂不支持的 MVC 返回值类型: " + returnValue.getClass().getName());
    }
}
