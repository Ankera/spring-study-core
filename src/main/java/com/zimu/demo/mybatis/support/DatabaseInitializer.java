package com.zimu.demo.mybatis.support;

// 导入配置对象。
import com.zimu.mybatis.config.Configuration;
// 导入连接工厂。
import com.zimu.mybatis.util.ConnectionFactory;

// 导入 JDBC 连接。
import java.sql.Connection;
// 导入 Statement。
import java.sql.Statement;

// 这个工具类专门负责初始化演示数据库。
public class DatabaseInitializer {

    // 根据配置对象初始化内存数据库。
    public static void initialize(Configuration configuration) {
        // 创建连接并执行建表、插数语句。
        try (
                Connection connection = ConnectionFactory.getConnection(configuration);
                Statement statement = connection.createStatement()
        ) {
            // 每次运行前先把表删掉，保证示例结果稳定。
            statement.execute("drop table if exists t_user");

            // 重新创建用户表。
            statement.execute("""
                    create table t_user (
                        id bigint auto_increment primary key,
                        username varchar(64),
                        password varchar(64),
                        age int
                    )
                    """);

            // 插入第一条测试数据。
            statement.execute("insert into t_user(username, password, age) values ('zhangsan', '123456', 18)");

            // 插入第二条测试数据。
            statement.execute("insert into t_user(username, password, age) values ('lisi', 'abcdef', 20)");

            // 插入第三条测试数据。
            statement.execute("insert into t_user(username, password, age) values ('wangwu', 'pw123', 22)");

            // 插入第四条测试数据。
            statement.execute("insert into t_user(username, password, age) values ('zhaoliu', 'hello', 24)");

            // 插入第五条测试数据。
            statement.execute("insert into t_user(username, password, age) values ('sunqi', 'world', 26)");
        } catch (Exception exception) {
            // 如果初始化失败，就抛异常。
            throw new RuntimeException("初始化测试数据库失败", exception);
        }
    }
}
