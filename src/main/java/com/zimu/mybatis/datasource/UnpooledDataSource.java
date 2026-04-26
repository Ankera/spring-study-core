package com.zimu.mybatis.datasource;

import java.sql.Connection;
import java.sql.DriverManager;

// 非池化数据源：每次调用 getConnection 都新建一个物理连接。
public class UnpooledDataSource implements MiniDataSource {

    // 保存 JDBC 驱动类名。
    private final String driver;

    // 保存 JDBC 地址。
    private final String url;

    // 保存用户名。
    private final String username;

    // 保存密码。
    private final String password;

    // 构造器接收数据库连接信息。
    public UnpooledDataSource(String driver, String url, String username, String password) {
        this.driver = driver;
        this.url = url;
        this.username = username;
        this.password = password;
        loadDriver();
    }

    // 每次都通过 DriverManager 创建一个真正的新连接。
    @Override
    public Connection getConnection() {
        try {
            return DriverManager.getConnection(url, username, password);
        } catch (Exception exception) {
            throw new RuntimeException("创建数据库连接失败", exception);
        }
    }

    // 加载 JDBC 驱动。
    private void loadDriver() {
        try {
            Class.forName(driver);
        } catch (Exception exception) {
            throw new RuntimeException("加载数据库驱动失败: " + driver, exception);
        }
    }
}
