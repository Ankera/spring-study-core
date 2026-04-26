package com.zimu.mybatis.datasource;

import java.sql.Connection;
import java.sql.DriverManager;

// 非池化数据源：每次调用 getConnection 都新建一个物理连接。
//
// 为什么先写一个 UnpooledDataSource？
// 因为连接池本身不负责“知道怎么连接 MySQL/H2”。
// 它只负责“缓存和复用连接”。
//
// 真正创建物理连接的动作，还是应该交给一个最朴素的数据源：
//
// driver + url + username + password -> DriverManager.getConnection()
//
// 这样 PooledDataSource 可以复用它来创建新连接，职责就很清楚：
// - UnpooledDataSource：创建真实连接
// - PooledDataSource：管理连接的借出和归还
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
    //
    // 这里创建对象时就加载驱动，是为了让后面的 getConnection 更简单：
    // getConnection 只做“创建连接”这一件事。
    public UnpooledDataSource(String driver, String url, String username, String password) {
        this.driver = driver;
        this.url = url;
        this.username = username;
        this.password = password;
        loadDriver();
    }

    // 每次都通过 DriverManager 创建一个真正的新连接。
    //
    // 这个方法对应最原始的 JDBC 写法。
    // 如果不使用连接池，SQL 每执行一次，就会走到这里新建连接。
    @Override
    public Connection getConnection() {
        try {
            return DriverManager.getConnection(url, username, password);
        } catch (Exception exception) {
            throw new RuntimeException("创建数据库连接失败", exception);
        }
    }

    // 加载 JDBC 驱动。
    //
    // 老版本 JDBC 通常需要手动 Class.forName(driver)。
    // 这里保留这一步，是为了让学习流程更清楚：
    // “先加载驱动，再通过 DriverManager 创建连接”。
    private void loadDriver() {
        try {
            Class.forName(driver);
        } catch (Exception exception) {
            throw new RuntimeException("加载数据库驱动失败: " + driver, exception);
        }
    }
}
