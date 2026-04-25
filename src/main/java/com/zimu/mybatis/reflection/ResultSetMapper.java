package com.zimu.mybatis.reflection;

// 导入反射字段。
import java.lang.reflect.Field;
// 导入结果集。
import java.sql.ResultSet;
// 导入结果集元数据。
import java.sql.ResultSetMetaData;

// 这个工具类负责把 JDBC 查询结果映射成 Java 对象。
public class ResultSetMapper {

    // 把结果集的当前行映射成目标对象。
    public static <T> T mapRow(ResultSet resultSet, Class<T> resultType) {
        try {
            // 先创建结果对象。
            T resultObject = resultType.getDeclaredConstructor().newInstance();

            // 读取结果集元数据，拿到列名和列数量。
            ResultSetMetaData metaData = resultSet.getMetaData();

            // 遍历每一列，把列值塞到对象里。
            for (int columnIndex = 1; columnIndex <= metaData.getColumnCount(); columnIndex++) {
                // 优先使用列标签，也就是 SQL 里的别名。
                String columnLabel = metaData.getColumnLabel(columnIndex);

                // 拿到当前列的值。
                Object columnValue = resultSet.getObject(columnIndex);

                // 把列名转换成字段名。
                String fieldName = toCamelCase(columnLabel);

                // 通过字段名找到对应字段。
                Field field = resultType.getDeclaredField(fieldName);

                // 允许访问 private 字段。
                field.setAccessible(true);

                // 把当前列值设置进对象字段。
                field.set(resultObject, columnValue);
            }

            // 返回组装好的结果对象。
            return resultObject;
        } catch (NoSuchFieldException exception) {
            // 如果某个字段对不上，就抛异常，帮助学习时尽早发现问题。
            throw new RuntimeException("结果映射失败，实体类缺少对应字段", exception);
        } catch (Exception exception) {
            // 其他异常统一包装。
            throw new RuntimeException("结果集映射对象失败", exception);
        }
    }

    // 把数据库字段名转成 Java 常见的驼峰字段名。
    private static String toCamelCase(String text) {
        // 先把数据库列名统一转成小写，避免像 H2 这样默认返回大写列名。
        text = text.toLowerCase();

        // 用来拼接最终结果。
        StringBuilder builder = new StringBuilder();

        // 记录下一个字母是否需要大写。
        boolean upperCaseNext = false;

        // 逐个字符处理。
        for (char currentChar : text.toCharArray()) {
            // 遇到下划线时，说明下一个字符需要转大写。
            if (currentChar == '_') {
                upperCaseNext = true;
                continue;
            }

            // 如果需要转大写，就把当前字符变成大写。
            if (upperCaseNext) {
                builder.append(Character.toUpperCase(currentChar));
                upperCaseNext = false;
                continue;
            }

            // 否则直接追加当前字符。
            builder.append(currentChar);
        }

        // 返回转换结果。
        return builder.toString();
    }
}
