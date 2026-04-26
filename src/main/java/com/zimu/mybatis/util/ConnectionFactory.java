package com.zimu.mybatis.util;

// 导入配置对象。
import com.zimu.mybatis.config.Configuration;

import java.sql.Connection;

// 这个工具类专门负责获取数据库连接。
//
// 以前它直接通过 DriverManager 创建连接。
// 加入连接池后，它变成了一个很薄的门面：
//
// SimpleExecutor -> ConnectionFactory -> MiniDataSource -> Connection
//
// 为什么要这样设计？
// 因为 SimpleExecutor 是执行 SQL 的，不应该知道连接池细节。
// 它只要调用这里拿 Connection，然后正常 close 就行。
public class ConnectionFactory {

    // 根据配置对象从数据源里获取连接。
    //
    // 如果配置的是 UnpooledDataSource，这里拿到的是新建物理连接。
    // 如果配置的是 PooledDataSource，这里拿到的是代理连接。
    // 对执行器来说，二者都是 java.sql.Connection。
    public static Connection getConnection(Configuration configuration) {
        if (configuration.getDataSource() == null) {
            throw new IllegalStateException("数据源还没有初始化");
        }
        return configuration.getDataSource().getConnection();
    }
}
