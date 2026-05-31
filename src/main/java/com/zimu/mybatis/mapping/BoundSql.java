package com.zimu.mybatis.mapping;

// 导入列表接口。
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// 这个类表示“已经把 #{...} 处理过”的 SQL。
public class BoundSql {

    // 处理后的 SQL，比如把 #{id} 变成 ?。
    private final String jdbcSql;

    // 按顺序保存参数名，比如 [id]。
    private final List<String> parameterNames;

    // 保存框架额外追加的参数值，比如 PageHelper 追加的 offset 和 pageSize。
    private final List<Object> additionalParameterValues;

    // 通过构造器初始化这两个字段。
    public BoundSql(String jdbcSql, List<String> parameterNames) {
        this(jdbcSql, parameterNames, Collections.emptyList());
    }

    // 通过构造器初始化 SQL、原始参数名、额外参数值。
    public BoundSql(String jdbcSql, List<String> parameterNames, List<Object> additionalParameterValues) {
        this.jdbcSql = jdbcSql;
        this.parameterNames = parameterNames;
        this.additionalParameterValues = additionalParameterValues;
    }

    // 返回处理后的 JDBC SQL。
    public String getJdbcSql() {
        return jdbcSql;
    }

    // 返回参数名列表。
    public List<String> getParameterNames() {
        return parameterNames;
    }

    // 返回框架额外追加的参数值。
    public List<Object> getAdditionalParameterValues() {
        return additionalParameterValues;
    }

    // 基于当前 BoundSql 追加一批固定参数。
    public BoundSql appendSqlAndParameters(String extraSql, List<Object> extraParameterValues) {
        // 复制一份额外参数列表，避免外部继续修改影响 BoundSql。
        List<Object> mergedAdditionalValues = new ArrayList<>(additionalParameterValues);

        // 追加新的固定参数。
        mergedAdditionalValues.addAll(extraParameterValues);

        // 返回一个新的 BoundSql，原对象保持不变。
        return new BoundSql(jdbcSql + extraSql, parameterNames, mergedAdditionalValues);
    }
}
