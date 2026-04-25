package com.zimu.mybatis.reflection;

// 导入字段类。
import java.lang.reflect.Field;

// 这个工具类负责从参数对象中取出真正要绑定到 SQL 的值。
public class ParameterResolver {

    // 根据参数对象和参数名，取出对应的值。
    public static Object resolveValue(Object parameterObject, String parameterName) {
        // 如果调用方没有传参数，就直接返回 null。
        if (parameterObject == null) {
            return null;
        }

        // 如果本身就是基本包装类型或字符串，我们就直接把整个对象当参数值。
        if (isSimpleType(parameterObject.getClass())) {
            return parameterObject;
        }

        try {
            // 如果是普通 Java 对象，就按字段名去反射取值。
            Field field = parameterObject.getClass().getDeclaredField(parameterName);

            // 允许访问 private 字段。
            field.setAccessible(true);

            // 返回字段值。
            return field.get(parameterObject);
        } catch (Exception exception) {
            // 如果取值失败，就抛异常。
            throw new RuntimeException("从参数对象中取值失败，字段名: " + parameterName, exception);
        }
    }

    // 判断一个类型是不是简单类型。
    private static boolean isSimpleType(Class<?> clazz) {
        return clazz == String.class
                || clazz == Long.class
                || clazz == Integer.class
                || clazz == Short.class
                || clazz == Double.class
                || clazz == Float.class
                || clazz == Boolean.class
                || clazz == Byte.class
                || clazz == Character.class
                || clazz.isPrimitive();
    }
}
