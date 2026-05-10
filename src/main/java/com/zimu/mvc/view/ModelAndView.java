package com.zimu.mvc.view;

import java.util.HashMap;
import java.util.Map;

// Controller 方法的返回值对象。
//
// 它表示：
// - viewName：要去哪个页面
// - model：给页面的数据
//
// 如果 Controller 只返回 String，也可以理解成只返回 viewName。
public class ModelAndView {

    // 逻辑视图名，比如 login 或 home。
    private final String viewName;

    // 页面模型数据。
    private final Map<String, Object> model = new HashMap<>();

    public ModelAndView(String viewName) {
        this.viewName = viewName;
    }

    // 添加模型数据，并返回自身，方便链式调用。
    public ModelAndView addObject(String name, Object value) {
        model.put(name, value);
        return this;
    }

    public String getViewName() {
        return viewName;
    }

    public Map<String, Object> getModel() {
        return model;
    }
}
