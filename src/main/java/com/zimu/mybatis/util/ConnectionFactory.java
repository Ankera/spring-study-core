package com.zimu.mybatis.util;

// 导入配置对象。
import com.zimu.mybatis.config.Configuration;

import java.sql.Connection;

// 这个工具类专门负责创建数据库连接。
public class ConnectionFactory {

    // 根据配置对象从数据源里获取连接。
    public static Connection getConnection(Configuration configuration) {
        if (configuration.getDataSource() == null) {
            throw new IllegalStateException("数据源还没有初始化");
        }
        return configuration.getDataSource().getConnection();
    }
}
