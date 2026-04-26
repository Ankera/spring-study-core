package com.zimu.mybatis.datasource;

import org.junit.jupiter.api.Test;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

// 专门测试 mini MyBatis 自己写的连接池。
public class PooledDataSourceTest {

    // close 之后，连接应该从 active 列表回到 idle 列表。
    @Test
    public void closeShouldReturnConnectionToIdlePool() throws Exception {
        PooledDataSource pooledDataSource = newPooledDataSource(2, 2);

        Connection connection = pooledDataSource.getConnection();

        assertEquals(1, pooledDataSource.getActiveConnectionCount());
        assertEquals(0, pooledDataSource.getIdleConnectionCount());

        connection.close();

        assertEquals(0, pooledDataSource.getActiveConnectionCount());
        assertEquals(1, pooledDataSource.getIdleConnectionCount());
    }

    // 归还后的连接，再次借出时应该优先复用。
    @Test
    public void shouldReuseReturnedConnection() throws Exception {
        PooledDataSource pooledDataSource = newPooledDataSource(2, 2);

        Connection firstConnection = pooledDataSource.getConnection();
        firstConnection.close();

        Connection secondConnection = pooledDataSource.getConnection();

        assertSame(firstConnection, secondConnection);
        assertEquals(1, pooledDataSource.getActiveConnectionCount());
        assertEquals(0, pooledDataSource.getIdleConnectionCount());

        secondConnection.close();
    }

    // 达到最大活跃连接数之后，教学版连接池会直接报错。
    @Test
    public void shouldRejectWhenActiveConnectionLimitReached() throws Exception {
        PooledDataSource pooledDataSource = newPooledDataSource(1, 1);

        Connection connection = pooledDataSource.getConnection();

        assertThrows(RuntimeException.class, pooledDataSource::getConnection);

        connection.close();
    }

    // 重复 close 同一个代理连接，不应该重复放回 idle 列表。
    @Test
    public void repeatedCloseShouldOnlyReturnConnectionOnce() throws Exception {
        PooledDataSource pooledDataSource = newPooledDataSource(2, 2);

        Connection connection = pooledDataSource.getConnection();

        connection.close();
        connection.close();

        assertEquals(0, pooledDataSource.getActiveConnectionCount());
        assertEquals(1, pooledDataSource.getIdleConnectionCount());
    }

    // 创建一个使用 H2 内存库的连接池，避免测试依赖本地 MySQL。
    private PooledDataSource newPooledDataSource(int maximumActiveConnections, int maximumIdleConnections) {
        UnpooledDataSource unpooledDataSource = new UnpooledDataSource(
                "org.h2.Driver",
                "jdbc:h2:mem:pooled_data_source_test;DB_CLOSE_DELAY=-1;MODE=MySQL",
                "sa",
                ""
        );

        return new PooledDataSource(
                unpooledDataSource,
                maximumActiveConnections,
                maximumIdleConnections
        );
    }
}
