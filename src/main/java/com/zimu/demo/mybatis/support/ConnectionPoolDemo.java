package com.zimu.demo.mybatis.support;

// 导入配置对象。
import com.zimu.mybatis.config.Configuration;
// 导入池化数据源。
import com.zimu.mybatis.datasource.PooledDataSource;
// 导入会话工厂构建器。
import com.zimu.mybatis.session.SqlSessionFactoryBuilder;

// 导入 JDBC 连接。
import java.sql.Connection;

// 这个 demo 用来观察连接池的借出和归还。
public class ConnectionPoolDemo {

    // 单独运行这个 main 方法，就能看到连接池数量变化。
    public static void main(String[] args) throws Exception {
        // 读取 mini MyBatis 配置。
        Configuration configuration = new SqlSessionFactoryBuilder()
                .build("mybatis-config.xml")
                .getConfiguration();

        // 当前配置打开了连接池，所以这里可以转成 PooledDataSource 观察状态。
        PooledDataSource pooledDataSource = (PooledDataSource) configuration.getDataSource();

        // 第一次借连接。
        Connection firstConnection = pooledDataSource.getConnection();
        printPoolState("借出第 1 个连接", pooledDataSource);

        // 第二次借连接。
        Connection secondConnection = pooledDataSource.getConnection();
        printPoolState("借出第 2 个连接", pooledDataSource);

        // close 不会真的关闭物理连接，而是把连接还回池子。
        firstConnection.close();
        printPoolState("归还第 1 个连接", pooledDataSource);

        // 再借一次，会优先复用刚才归还的空闲连接。
        Connection thirdConnection = pooledDataSource.getConnection();
        printPoolState("再次借出连接", pooledDataSource);

        // 清理演示用连接。
        secondConnection.close();
        thirdConnection.close();
        printPoolState("全部归还连接", pooledDataSource);
    }

    // 打印连接池当前状态。
    private static void printPoolState(String action, PooledDataSource pooledDataSource) {
        System.out.println(action
                + " -> active=" + pooledDataSource.getActiveConnectionCount()
                + ", idle=" + pooledDataSource.getIdleConnectionCount());
    }
}
