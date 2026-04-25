package com.zimu.mybatis.util;

// 导入数组列表。
import java.util.ArrayList;
// 导入列表接口。
import java.util.List;

// 这个类专门负责把 SQL 里的 #{...} 解析出来。
public class GenericTokenParser {

    // 解析 SQL，把 #{字段名} 变成 ?，同时记住字段名顺序。
    public static ParsedSql parse(String originalSql) {
        // 用来收集参数名，比如 id、username。
        List<String> parameterNames = new ArrayList<>();

        // 用来拼接最终的 JDBC SQL。
        StringBuilder jdbcSqlBuilder = new StringBuilder();

        // 从头开始扫描字符串。
        int index = 0;

        // 循环查找下一个 #{ 的位置。
        while (index < originalSql.length()) {
            // 找到下一个参数开始位置。
            int start = originalSql.indexOf("#{", index);

            // 如果已经找不到了，说明后面全是普通文本。
            if (start == -1) {
                jdbcSqlBuilder.append(originalSql.substring(index));
                break;
            }

            // 先把前面的普通 SQL 拼进去。
            jdbcSqlBuilder.append(originalSql, index, start);

            // 继续找 } 的位置。
            int end = originalSql.indexOf("}", start);

            // 如果没有找到闭合大括号，说明 SQL 写错了。
            if (end == -1) {
                throw new IllegalArgumentException("SQL 中存在没有闭合的 #{...} 占位符: " + originalSql);
            }

            // 截出占位符中的名字，比如 id。
            String parameterName = originalSql.substring(start + 2, end).trim();

            // 保存参数名，后面设置 PreparedStatement 参数要按顺序取。
            parameterNames.add(parameterName);

            // JDBC 里真正使用的是问号占位符。
            jdbcSqlBuilder.append("?");

            // 移动扫描位置，继续往后找。
            index = end + 1;
        }

        // 返回解析结果。
        return new ParsedSql(jdbcSqlBuilder.toString(), parameterNames);
    }

    // 这个内部类只是一个简单的数据载体。
    public static class ParsedSql {

        // 保存处理后的 SQL。
        private final String jdbcSql;

        // 保存参数名列表。
        private final List<String> parameterNames;

        // 通过构造器初始化字段。
        public ParsedSql(String jdbcSql, List<String> parameterNames) {
            this.jdbcSql = jdbcSql;
            this.parameterNames = parameterNames;
        }

        // 返回 JDBC SQL。
        public String getJdbcSql() {
            return jdbcSql;
        }

        // 返回参数名列表。
        public List<String> getParameterNames() {
            return parameterNames;
        }
    }
}
