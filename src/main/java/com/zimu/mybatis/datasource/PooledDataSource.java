package com.zimu.mybatis.datasource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// 池化数据源：close 不是真的关闭连接，而是把连接还回池子。
public class PooledDataSource implements MiniDataSource {

    // 真正负责创建物理连接的非池化数据源。
    private final UnpooledDataSource unpooledDataSource;

    // 空闲连接列表。
    private final List<PooledConnection> idleConnections = new ArrayList<>();

    // 正在使用中的连接列表。
    private final List<PooledConnection> activeConnections = new ArrayList<>();

    // 最大活跃连接数。
    private final int maximumActiveConnections;

    // 最大空闲连接数。
    private final int maximumIdleConnections;

    // 构造器接收非池化数据源和池大小。
    public PooledDataSource(UnpooledDataSource unpooledDataSource, int maximumActiveConnections, int maximumIdleConnections) {
        this.unpooledDataSource = unpooledDataSource;
        this.maximumActiveConnections = maximumActiveConnections;
        this.maximumIdleConnections = maximumIdleConnections;
    }

    // 从连接池中借一个连接。
    @Override
    public synchronized Connection getConnection() {
        PooledConnection pooledConnection;

        // 1. 有空闲连接，直接复用。
        if (!idleConnections.isEmpty()) {
            pooledConnection = idleConnections.remove(0);
        } else if (activeConnections.size() < maximumActiveConnections) {
            // 2. 没有空闲连接，但还没到上限，就创建新的物理连接。
            pooledConnection = new PooledConnection(unpooledDataSource.getConnection(), this);
        } else {
            // 3. 教学版先不做等待队列，超过上限就直接报错。
            throw new RuntimeException("连接池已满，当前最大活跃连接数: " + maximumActiveConnections);
        }

        pooledConnection.checkout();
        activeConnections.add(pooledConnection);
        return pooledConnection.getProxyConnection();
    }

    // 归还连接。这个方法由 PooledConnection 的 close 代理调用。
    synchronized void pushConnection(PooledConnection pooledConnection) {
        activeConnections.remove(pooledConnection);

        if (idleConnections.size() < maximumIdleConnections) {
            pooledConnection.checkin();
            idleConnections.add(pooledConnection);
            return;
        }

        // 空闲连接太多时，真的关闭物理连接。
        pooledConnection.closeRealConnection();
    }

    // 关闭连接池里的所有真实物理连接。
    public synchronized void forceCloseAll() {
        for (PooledConnection pooledConnection : idleConnections) {
            pooledConnection.closeRealConnection();
        }
        for (PooledConnection pooledConnection : activeConnections) {
            pooledConnection.closeRealConnection();
        }
        idleConnections.clear();
        activeConnections.clear();
    }

    // 返回空闲连接数量，方便 demo 打印观察。
    public synchronized int getIdleConnectionCount() {
        return idleConnections.size();
    }

    // 返回活跃连接数量，方便 demo 打印观察。
    public synchronized int getActiveConnectionCount() {
        return activeConnections.size();
    }

    // 判断物理连接是否还能用。
    static boolean isRealConnectionValid(Connection realConnection) {
        try {
            return realConnection != null && !realConnection.isClosed();
        } catch (SQLException exception) {
            return false;
        }
    }
}
