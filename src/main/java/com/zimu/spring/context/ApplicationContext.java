package com.zimu.spring.context;

// 导入自动注入注解。
import com.zimu.spring.annotation.Autowired;
// 导入 Bean 注解。
import com.zimu.spring.annotation.Bean;
// 导入组件注解。
import com.zimu.spring.annotation.Component;
// 导入包扫描注解。
import com.zimu.spring.annotation.ComponentScan;
// 导入配置类注解。
import com.zimu.spring.annotation.Configuration;
// 导入控制器注解。
import com.zimu.spring.annotation.Controller;
// 导入仓库注解。
import com.zimu.spring.annotation.Repository;
// 导入服务注解。
import com.zimu.spring.annotation.Service;
// 导入值注入注解。
import com.zimu.spring.annotation.Value;
// 导入类扫描工具。
import com.zimu.spring.util.ClassScanner;

// 导入输入流。
import java.io.InputStream;
// 导入反射构造器。
import java.lang.reflect.Constructor;
// 导入反射字段。
import java.lang.reflect.Field;
// 导入反射方法。
import java.lang.reflect.Method;
// 导入反射参数。
import java.lang.reflect.Parameter;
// 导入修饰符工具类。
import java.lang.reflect.Modifier;
// 导入哈希映射。
import java.util.HashMap;
// 导入列表接口。
import java.util.List;
// 导入映射接口。
import java.util.Map;
// 导入属性类。
import java.util.Properties;

// 这是一个极简版 IoC 容器，用来模仿 Spring 的核心流程。
public class ApplicationContext {

    // 这个 Map 按名字保存 Bean。
    private final Map<String, Object> beanByName = new HashMap<>();

    // 这个 Map 按类型保存 Bean。
    private final Map<Class<?>, Object> beanByType = new HashMap<>();

    // 这个对象专门用来保存 application.properties 中的配置。
    private final Properties properties = new Properties();

    // 创建容器时，需要传入一个配置类。
    public ApplicationContext(Class<?> configClass) {
        // 第一步，先把配置文件加载进来。
        loadProperties();

        // 第二步，解析配置类上的扫描路径。
        String basePackage = resolveBasePackage(configClass);

        // 第三步，把扫描到的类都拿出来。
        List<Class<?>> classes = ClassScanner.scan(basePackage);

        // 第四步，先创建组件类和配置类对象。
        createComponentBeans(classes);

        // 第五步，处理配置类中的 @Bean 方法。
        createBeansFromConfiguration(classes);
    }

    // 读取 classpath 下的 application.properties。
    private void loadProperties() {
        // 使用类加载器读取资源文件。
        try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("application.properties")) {
            // 如果文件存在，就加载它。
            if (inputStream != null) {
                properties.load(inputStream);
            }
        } catch (Exception exception) {
            // 如果读取失败，就抛出运行时异常。
            throw new RuntimeException("加载 application.properties 失败", exception);
        }
    }

    // 解析配置类上的 @ComponentScan。
    private String resolveBasePackage(Class<?> configClass) {
        // 读取配置类上的 @ComponentScan 注解。
        ComponentScan componentScan = configClass.getAnnotation(ComponentScan.class);

        // 如果用户没有写这个注解，就给出明确提示。
        if (componentScan == null) {
            throw new IllegalArgumentException("配置类上必须标注 @ComponentScan");
        }

        // 返回用户写的扫描包路径。
        return componentScan.value();
    }

    // 先创建所有组件类。
    private void createComponentBeans(List<Class<?>> classes) {
        // 遍历所有扫描到的类。
        for (Class<?> clazz : classes) {
            // 如果不是组件类，就跳过。
            if (!isComponentClass(clazz)) {
                continue;
            }

            // 如果已经创建过这个类型的 Bean，就不用重复创建。
            if (beanByType.containsKey(clazz)) {
                continue;
            }

            // 真正创建 Bean。
            createBean(clazz);
        }
    }

    // 处理配置类中的 @Bean 方法。
    private void createBeansFromConfiguration(List<Class<?>> classes) {
        // 遍历扫描到的所有类。
        for (Class<?> clazz : classes) {
            // 只处理标了 @Configuration 的类。
            if (!clazz.isAnnotationPresent(Configuration.class)) {
                continue;
            }

            // 先从容器中拿到配置类对象。
            Object configurationBean = getBean(clazz);

            // 遍历配置类中的所有方法。
            for (Method method : clazz.getDeclaredMethods()) {
                // 只处理标了 @Bean 的方法。
                if (!method.isAnnotationPresent(Bean.class)) {
                    continue;
                }

                // 调用方法并创建 Bean。
                Object bean = invokeBeanMethod(configurationBean, method);

                // 算出这个 Bean 的名字。
                String beanName = resolveBeanMethodName(method);

                // 注册到容器中。
                registerBean(beanName, bean.getClass(), bean);
            }
        }
    }

    // 判断一个类是不是“组件类”。
    private boolean isComponentClass(Class<?> clazz) {
        // 接口不能被实例化，所以跳过。
        if (clazz.isInterface()) {
            return false;
        }

        // 抽象类也不能直接 new，所以跳过。
        if (Modifier.isAbstract(clazz.getModifiers())) {
            return false;
        }

        // 只要标了这些注解中的任意一个，就算组件类。
        return clazz.isAnnotationPresent(Component.class)
                || clazz.isAnnotationPresent(Controller.class)
                || clazz.isAnnotationPresent(Service.class)
                || clazz.isAnnotationPresent(Repository.class)
                || clazz.isAnnotationPresent(Configuration.class);
    }

    // 根据类去创建 Bean。
    private Object createBean(Class<?> clazz) {
        // 如果之前已经创建过了，直接返回已有对象。
        if (beanByType.containsKey(clazz)) {
            return beanByType.get(clazz);
        }

        try {
            // 找到要用的构造器。
            Constructor<?> constructor = resolveConstructor(clazz);

            // 解析构造器参数，也就是依赖注入。
            Object[] constructorArgs = resolveExecutableArguments(constructor.getParameters());

            // 允许访问非 public 构造器。
            constructor.setAccessible(true);

            // 通过反射真正创建对象。
            Object bean = constructor.newInstance(constructorArgs);

            // 创建完对象后，先处理字段上的 @Autowired。
            injectAutowiredFields(bean);

            // 创建完对象后，再处理字段上的 @Value。
            injectValueFields(bean);

            // 算出这个 Bean 的名字。
            String beanName = resolveBeanName(clazz);

            // 注册到容器里。
            registerBean(beanName, clazz, bean);

            // 返回创建好的对象。
            return bean;
        } catch (Exception exception) {
            // 包一层更清楚的异常信息。
            throw new RuntimeException("创建 Bean 失败: " + clazz.getName(), exception);
        }
    }

    // 处理 @Bean 方法。
    private Object invokeBeanMethod(Object configurationBean, Method method) {
        try {
            // 解析方法参数。
            Object[] methodArgs = resolveExecutableArguments(method.getParameters());

            // 允许访问非 public 方法。
            method.setAccessible(true);

            // 调用配置类方法，得到真正的 Bean。
            Object bean = method.invoke(configurationBean, methodArgs);

            // 如果返回了 null，就说明写法有问题。
            if (bean == null) {
                throw new IllegalStateException("@Bean 方法不能返回 null: " + method.getName());
            }

            // 创建出来后，也给它做字段上的 @Value 注入。
            injectValueFields(bean);

            // 返回创建好的 Bean。
            return bean;
        } catch (Exception exception) {
            // 包一层更清楚的异常信息。
            throw new RuntimeException("调用 @Bean 方法失败: " + method.getName(), exception);
        }
    }

    // 统一解析构造器参数和方法参数。
    private Object[] resolveExecutableArguments(Parameter[] parameters) {
        // 创建和参数数量一样长的数组，用来保存解析结果。
        Object[] args = new Object[parameters.length];

        // 逐个处理每一个参数。
        for (int index = 0; index < parameters.length; index++) {
            // 取出当前参数。
            Parameter parameter = parameters[index];

            // 如果参数上标了 @Value，就走配置注入。
            if (parameter.isAnnotationPresent(Value.class)) {
                args[index] = resolveValue(parameter.getType(), parameter.getAnnotation(Value.class).value());
                // 当前参数处理完，继续下一个。
                continue;
            }

            // 否则就按类型去容器里找依赖。
            args[index] = getBean(parameter.getType());
        }

        // 返回参数结果数组。
        return args;
    }

    // 给字段注入 @Value。
    private void injectValueFields(Object bean) {
        // 遍历当前类声明的所有字段。
        for (Field field : bean.getClass().getDeclaredFields()) {
            // 只处理标了 @Value 的字段。
            if (!field.isAnnotationPresent(Value.class)) {
                continue;
            }

            // 读取注解中的表达式。
            String expression = field.getAnnotation(Value.class).value();

            // 根据字段类型，把配置值转成对应类型。
            Object value = resolveValue(field.getType(), expression);

            try {
                // 允许访问 private 字段。
                field.setAccessible(true);

                // 把值真正设置进去。
                field.set(bean, value);
            } catch (Exception exception) {
                // 设置失败就抛异常。
                throw new RuntimeException("字段注入失败: " + field.getName(), exception);
            }
        }
    }

    // 给字段注入 @Autowired。
    private void injectAutowiredFields(Object bean) {
        // 遍历当前类声明的所有字段。
        for (Field field : bean.getClass().getDeclaredFields()) {
            // 只处理标了 @Autowired 的字段。
            if (!field.isAnnotationPresent(Autowired.class)) {
                continue;
            }

            // 按字段类型去容器中查找依赖对象。
            Object dependency = getBean(field.getType());

            try {
                // 允许访问 private 字段。
                field.setAccessible(true);

                // 把依赖对象真正设置进去。
                field.set(bean, dependency);
            } catch (Exception exception) {
                // 设置失败就抛异常。
                throw new RuntimeException("字段自动注入失败: " + field.getName(), exception);
            }
        }
    }

    // 解析 ${xxx} 这种格式的配置值。
    private Object resolveValue(Class<?> targetType, String expression) {
        // 先把表达式原样保存下来。
        String key = expression;

        // 如果是 ${...} 格式，就把前后的符号去掉。
        if (expression.startsWith("${") && expression.endsWith("}")) {
            key = expression.substring(2, expression.length() - 1);
        }

        // 根据 key 去配置文件里找值。
        String rawValue = properties.getProperty(key);

        // 如果找不到配置，就抛异常，方便学习时快速发现问题。
        if (rawValue == null) {
            throw new IllegalArgumentException("找不到配置项: " + key);
        }

        // 按字段目标类型做简单转换。
        if (targetType == String.class) {
            return rawValue;
        }

        // 支持 int 和 Integer。
        if (targetType == int.class || targetType == Integer.class) {
            return Integer.parseInt(rawValue);
        }

        // 支持 boolean 和 Boolean。
        if (targetType == boolean.class || targetType == Boolean.class) {
            return Boolean.parseBoolean(rawValue);
        }

        // 其他类型这里先不扩展，避免示例过重。
        throw new IllegalArgumentException("暂不支持的 @Value 类型: " + targetType.getName());
    }

    // 找到应该使用哪个构造器。
    private Constructor<?> resolveConstructor(Class<?> clazz) {
        // 取出当前类声明的所有构造器。
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();

        // 先记录标了 @Autowired 的构造器。
        Constructor<?> autowiredConstructor = null;

        // 先看看有没有用户明确指定的自动注入构造器。
        for (Constructor<?> constructor : constructors) {
            // 如果当前构造器没有标 @Autowired，就继续下一个。
            if (!constructor.isAnnotationPresent(Autowired.class)) {
                continue;
            }

            // 如果已经找到过一个，再找到第二个就说明写法有歧义。
            if (autowiredConstructor != null) {
                throw new IllegalStateException("一个类只能有一个 @Autowired 构造器: " + clazz.getName());
            }

            // 记录这个被指定的构造器。
            autowiredConstructor = constructor;
        }

        // 如果找到了 @Autowired 构造器，就优先使用它。
        if (autowiredConstructor != null) {
            return autowiredConstructor;
        }

        // 如果只有一个构造器，那就直接用它。
        if (constructors.length == 1) {
            return constructors[0];
        }

        // 如果有无参构造器，就优先使用无参构造器。
        for (Constructor<?> constructor : constructors) {
            if (constructor.getParameterCount() == 0) {
                return constructor;
            }
        }

        // 如果既不是单构造器，也没有无参构造器，就直接报错。
        throw new IllegalStateException("请保证类有唯一构造器，或者提供无参构造器: " + clazz.getName());
    }

    // 计算类级别 Bean 的名字。
    private String resolveBeanName(Class<?> clazz) {
        // 先尝试从不同注解上取用户自定义名字。
        String customName = "";

        // 如果类上标了 @Component，就读它的 value。
        if (clazz.isAnnotationPresent(Component.class)) {
            customName = clazz.getAnnotation(Component.class).value();
        }

        // 如果类上标了 @Controller，就用它覆盖前面的值。
        if (clazz.isAnnotationPresent(Controller.class)) {
            customName = clazz.getAnnotation(Controller.class).value();
        }

        // 如果类上标了 @Service，就用它覆盖前面的值。
        if (clazz.isAnnotationPresent(Service.class)) {
            customName = clazz.getAnnotation(Service.class).value();
        }

        // 如果类上标了 @Repository，就用它覆盖前面的值。
        if (clazz.isAnnotationPresent(Repository.class)) {
            customName = clazz.getAnnotation(Repository.class).value();
        }

        // 如果拿到了自定义名字，就直接返回。
        if (!customName.isBlank()) {
            return customName;
        }

        // 否则就把类名首字母变小写，模拟 Spring 常见默认命名规则。
        String simpleName = clazz.getSimpleName();
        return Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1);
    }

    // 计算 @Bean 方法产生出来的名字。
    private String resolveBeanMethodName(Method method) {
        // 先读取方法上的 @Bean 注解。
        Bean bean = method.getAnnotation(Bean.class);

        // 如果用户在注解里写了名字，就优先使用。
        if (!bean.value().isBlank()) {
            return bean.value();
        }

        // 否则默认使用方法名。
        return method.getName();
    }

    // 把 Bean 放进两个 Map。
    private void registerBean(String beanName, Class<?> beanClass, Object bean) {
        // 按名字注册。
        beanByName.put(beanName, bean);

        // 按具体类型注册。
        beanByType.put(beanClass, bean);
    }

    // 按类型获取 Bean。
    public <T> T getBean(Class<T> type) {
        // 如果已经存在，直接返回。
        Object existingBean = beanByType.get(type);

        // 如果找到了，就直接强转返回。
        if (existingBean != null) {
            return type.cast(existingBean);
        }

        // 如果没找到，但它本身就是组件类，就尝试现场创建。
        if (isComponentClass(type)) {
            return type.cast(createBean(type));
        }

        // 如果按具体类型找不到，就尝试按“父类型或接口”查找。
        for (Object bean : beanByType.values()) {
            if (type.isAssignableFrom(bean.getClass())) {
                return type.cast(bean);
            }
        }

        // 还是找不到，就报错。
        throw new IllegalArgumentException("容器中找不到这个类型的 Bean: " + type.getName());
    }

    // 按名字获取 Bean。
    public Object getBean(String beanName) {
        // 根据名字取值。
        Object bean = beanByName.get(beanName);

        // 如果为空，就说明没找到。
        if (bean == null) {
            throw new IllegalArgumentException("容器中找不到这个名字的 Bean: " + beanName);
        }

        // 返回找到的 Bean。
        return bean;
    }
}
