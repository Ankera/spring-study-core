package com.zimu.mybatis.config;

// 导入 mini MyBatis 的数据源接口。
import com.zimu.mybatis.datasource.MiniDataSource;
// 导入映射语句类。
import com.zimu.mybatis.mapping.MappedStatement;

// 导入哈希映射。
import java.util.HashMap;
// 导入映射接口。
import java.util.Map;

// 这个类用来保存 mini MyBatis 运行时需要的所有核心配置。
public class Configuration {

    // 保存 JDBC 驱动类名。
    private String driver;

    // 保存 JDBC 连接地址。
    private String url;

    // 保存数据库用户名。
    private String username;

    // 保存数据库密码。
    private String password;

    // 是否启用连接池。
    private boolean poolEnabled = true;

    // 连接池最大活跃连接数。
    private int poolMaximumActiveConnections = 5;

    // 连接池最大空闲连接数。
    private int poolMaximumIdleConnections = 2;

    // 保存最终创建好的数据源。
    private MiniDataSource dataSource;

    // 这个 Map 用来保存所有解析出来的 SQL 语句。
    // key 一般长这样：com.zimu.demo.mybatis.mapper.UserMapper.selectById
    private final Map<String, MappedStatement> mappedStatementMap = new HashMap<>();

    // 返回 JDBC 驱动类名。
    public String getDriver() {
        return driver;
    }

    // 设置 JDBC 驱动类名。
    public void setDriver(String driver) {
        this.driver = driver;
    }

    // 返回 JDBC 地址。
    public String getUrl() {
        return url;
    }

    // 设置 JDBC 地址。
    public void setUrl(String url) {
        this.url = url;
    }

    // 返回用户名。
    public String getUsername() {
        return username;
    }

    // 设置用户名。
    public void setUsername(String username) {
        this.username = username;
    }

    // 返回密码。
    public String getPassword() {
        return password;
    }

    // 设置密码。
    public void setPassword(String password) {
        this.password = password;
    }

    // 返回是否启用连接池。
    public boolean isPoolEnabled() {
        return poolEnabled;
    }

    // 设置是否启用连接池。
    public void setPoolEnabled(boolean poolEnabled) {
        this.poolEnabled = poolEnabled;
    }

    // 返回最大活跃连接数。
    public int getPoolMaximumActiveConnections() {
        return poolMaximumActiveConnections;
    }

    // 设置最大活跃连接数。
    public void setPoolMaximumActiveConnections(int poolMaximumActiveConnections) {
        this.poolMaximumActiveConnections = poolMaximumActiveConnections;
    }

    // 返回最大空闲连接数。
    public int getPoolMaximumIdleConnections() {
        return poolMaximumIdleConnections;
    }

    // 设置最大空闲连接数。
    public void setPoolMaximumIdleConnections(int poolMaximumIdleConnections) {
        this.poolMaximumIdleConnections = poolMaximumIdleConnections;
    }

    // 返回数据源。
    public MiniDataSource getDataSource() {
        return dataSource;
    }

    // 设置数据源。
    public void setDataSource(MiniDataSource dataSource) {
        this.dataSource = dataSource;
    }

    // 注册一条映射语句。
    public void addMappedStatement(String statementId, MappedStatement mappedStatement) {
        mappedStatementMap.put(statementId, mappedStatement);
    }

    // 根据 statementId 获取映射语句。
    public MappedStatement getMappedStatement(String statementId) {
        return mappedStatementMap.get(statementId);
    }
}
