package com.zimu.mybatis.util;

// 导入配置对象。
import com.zimu.mybatis.config.Configuration;

// 导入 JDBC 连接。
import java.sql.Connection;
// 导入驱动管理器。
import java.sql.DriverManager;

// 这个工具类专门负责创建数据库连接。
public class ConnectionFactory {

    // 根据配置对象创建连接。
    public static Connection createConnection(Configuration configuration) {
        try {
            // 先手动加载驱动类。
            Class.forName(configuration.getDriver());

            // 再根据 JDBC 地址、用户名、密码创建连接。
            return DriverManager.getConnection(
                    configuration.getUrl(),
                    configuration.getUsername(),
                    configuration.getPassword()
            );
        } catch (Exception exception) {
            // 如果连接失败，就抛运行时异常。
            throw new RuntimeException("创建数据库连接失败", exception);
        }
    }
}
