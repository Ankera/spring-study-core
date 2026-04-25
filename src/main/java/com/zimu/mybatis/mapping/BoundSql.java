package com.zimu.mybatis.mapping;

// 导入列表接口。
import java.util.List;

// 这个类表示“已经把 #{...} 处理过”的 SQL。
public class BoundSql {

    // 处理后的 SQL，比如把 #{id} 变成 ?。
    private final String jdbcSql;

    // 按顺序保存参数名，比如 [id]。
    private final List<String> parameterNames;

    // 通过构造器初始化这两个字段。
    public BoundSql(String jdbcSql, List<String> parameterNames) {
        this.jdbcSql = jdbcSql;
        this.parameterNames = parameterNames;
    }

    // 返回处理后的 JDBC SQL。
    public String getJdbcSql() {
        return jdbcSql;
    }

    // 返回参数名列表。
    public List<String> getParameterNames() {
        return parameterNames;
    }
}
