package com.zimu.mybatis.datasource;

import org.junit.jupiter.api.Test;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

// 专门测试 mini MyBatis 自己写的连接池。
//
// 为什么要单独测连接池？
// 因为 MainTest 只能证明“SQL 能跑通”，不能证明连接池真的在复用连接。
// 连接池最重要的行为是状态变化：
// - 借出后 active +1
// - close 后 active -1，idle +1
// - 再次借出时优先从 idle 复用
// - 达到最大连接数时不能无限创建
public class PooledDataSourceTest {

    // close 之后，连接应该从 active 列表回到 idle 列表。
    //
    // 这个测试验证连接池最核心的语义：
    // 业务代码调用 connection.close()，并不是真的关闭物理连接，
    // 而是把连接归还给池子。
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
    //
    // assertSame(firstConnection, secondConnection) 验证的是：
    // 第二次拿到的还是同一个代理连接对象。
    // 这说明连接不是每次都新建，而是从 idleConnections 里复用了。
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
    //
    // 这个测试验证 maximumActiveConnections 的意义：
    // 连接池必须限制同时借出的连接数量，避免无限创建数据库连接。
    //
    // 真正连接池通常会等待一段时间；
    // 当前教学版为了简单，池满时直接抛异常。
    @Test
    public void shouldRejectWhenActiveConnectionLimitReached() throws Exception {
        PooledDataSource pooledDataSource = newPooledDataSource(1, 1);

        Connection connection = pooledDataSource.getConnection();

        assertThrows(RuntimeException.class, pooledDataSource::getConnection);

        connection.close();
    }

    // 重复 close 同一个代理连接，不应该重复放回 idle 列表。
    //
    // 如果不处理重复 close，同一个 PooledConnection 可能被放进 idleConnections 两次。
    // 那后面就可能出现两个业务流程借到同一条真实连接的问题。
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
    //
    // 这里仍然开启 MODE=MySQL，是为了保持和项目主 demo 一样的数据库语法风格。
    // 测试重点不是 H2，而是验证连接池对 java.sql.Connection 的管理逻辑。
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
