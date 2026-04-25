package com.zimu.mybatis.mapping;

// 这个类表示一条已经被解析好的 SQL 映射语句。
public class MappedStatement {

    // 保存 namespace，也就是 mapper 接口的全限定名。
    private String namespace;

    // 保存方法 id，也就是 mapper XML 里的 select id。
    private String id;

    // 保存原始 SQL。
    private String sql;

    // 保存参数类型名。
    private String parameterType;

    // 保存结果类型名。
    private String resultType;

    // 返回 namespace。
    public String getNamespace() {
        return namespace;
    }

    // 设置 namespace。
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    // 返回 id。
    public String getId() {
        return id;
    }

    // 设置 id。
    public void setId(String id) {
        this.id = id;
    }

    // 返回 SQL。
    public String getSql() {
        return sql;
    }

    // 设置 SQL。
    public void setSql(String sql) {
        this.sql = sql;
    }

    // 返回参数类型。
    public String getParameterType() {
        return parameterType;
    }

    // 设置参数类型。
    public void setParameterType(String parameterType) {
        this.parameterType = parameterType;
    }

    // 返回结果类型。
    public String getResultType() {
        return resultType;
    }

    // 设置结果类型。
    public void setResultType(String resultType) {
        this.resultType = resultType;
    }

    // 返回完整的 statementId。
    public String getStatementId() {
        return namespace + "." + id;
    }
}
