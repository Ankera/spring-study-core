package com.zimu.mybatis.datasource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// 池化数据源：close 不是真的关闭连接，而是把连接还回池子。
//
// 连接池解决的核心问题：
// 数据库物理连接的创建和销毁都比较重。
// 如果每条 SQL 都新建连接、执行完再销毁，开销会比较大。
//
// 所以连接池把连接保存起来：
// - 用的时候从池子里借
// - 用完 close 时不真的关闭，而是归还池子
// - 下次再用时优先复用空闲连接
//
// 这个类是教学版，所以只保留最核心的几件事：
// - idleConnections：空闲连接
// - activeConnections：正在使用的连接
// - maximumActiveConnections：最多同时借出去几个
// - maximumIdleConnections：最多缓存几个空闲连接
//
// 真正 MyBatis 的 PooledDataSource 还会做等待、超时、ping 检测等更多细节。
public class PooledDataSource implements MiniDataSource {

    // 真正负责创建物理连接的非池化数据源。
    //
    // 为什么连接池内部还要持有 UnpooledDataSource？
    // 因为连接池有时候还是需要创建新连接。
    // 但“怎么创建真实连接”不是连接池自己的主职责，
    // 所以交给 UnpooledDataSource，PooledDataSource 只负责池化管理。
    private final UnpooledDataSource unpooledDataSource;

    // 空闲连接列表。
    //
    // idle = 已经创建过、现在没人用、可以立刻复用的连接。
    // 连接被 close 归还后，会优先进入这里。
    private final List<PooledConnection> idleConnections = new ArrayList<>();

    // 正在使用中的连接列表。
    //
    // active = 已经借出去、业务代码正在使用的连接。
    // 连接池需要知道当前借出去了多少个，才能限制最大连接数。
    private final List<PooledConnection> activeConnections = new ArrayList<>();

    // 最大活跃连接数。
    //
    // 为什么要限制？
    // 防止程序无限创建数据库连接，把数据库或应用自己压垮。
    private final int maximumActiveConnections;

    // 最大空闲连接数。
    //
    // 为什么空闲连接也要限制？
    // 空闲连接虽然没在执行 SQL，但真实物理连接仍然占资源。
    // 缓存太多也会浪费数据库连接数。
    private final int maximumIdleConnections;

    // 构造器接收非池化数据源和池大小。
    //
    // 这里不直接接收 driver/url，是为了让“创建连接”和“管理连接池”解耦。
    public PooledDataSource(UnpooledDataSource unpooledDataSource, int maximumActiveConnections, int maximumIdleConnections) {
        this.unpooledDataSource = unpooledDataSource;
        this.maximumActiveConnections = maximumActiveConnections;
        this.maximumIdleConnections = maximumIdleConnections;
    }

    // 从连接池中借一个连接。
    //
    // synchronized 是教学版最直观的线程安全做法。
    // 因为 idleConnections 和 activeConnections 是共享列表，
    // 多个线程同时借还连接时，必须保证列表状态不会乱。
    @Override
    public synchronized Connection getConnection() {
        PooledConnection pooledConnection;

        // 1. 有空闲连接，直接复用。
        //
        // 这是连接池最重要的收益：
        // 复用已有物理连接，避免频繁 DriverManager.getConnection。
        if (!idleConnections.isEmpty()) {
            pooledConnection = idleConnections.remove(0);
        } else if (activeConnections.size() < maximumActiveConnections) {
            // 2. 没有空闲连接，但还没到上限，就创建新的物理连接。
            //
            // 创建真实连接这件事交给 UnpooledDataSource。
            // 创建出来后再包成 PooledConnection，这样才能拦截 close。
            pooledConnection = new PooledConnection(unpooledDataSource.getConnection(), this);
        } else {
            // 3. 教学版先不做等待队列，超过上限就直接报错。
            //
            // 真正连接池通常会等待一段时间，看有没有连接归还。
            // 这里为了把主线讲清楚，先用报错表达“池满了”。
            throw new RuntimeException("连接池已满，当前最大活跃连接数: " + maximumActiveConnections);
        }

        // 标记连接被借出，防止归还后继续被业务代码使用。
        pooledConnection.checkout();

        // 放入 active 列表，表示当前连接正在被使用。
        activeConnections.add(pooledConnection);

        // 返回代理连接，而不是真实连接。
        // 设计关键点：
        // 业务代码拿到的是 Connection，看起来和 JDBC 一样用；
        // 但 close() 已经被代理拦截，不会真的关闭物理连接。
        return pooledConnection.getProxyConnection();
    }

    // 归还连接。这个方法由 PooledConnection 的 close 代理调用。
    //
    // 用户代码不会直接调用 pushConnection。
    // 用户代码只会写 connection.close()。
    // PooledConnection 拦截 close 后，再回调这个方法。
    synchronized void pushConnection(PooledConnection pooledConnection) {
        // 从 active 中移除，表示这条连接不再被业务使用。
        activeConnections.remove(pooledConnection);

        // 如果空闲池还没满，就把连接放回 idle，等待下次复用。
        if (idleConnections.size() < maximumIdleConnections) {
            pooledConnection.checkin();
            idleConnections.add(pooledConnection);
            return;
        }

        // 空闲连接太多时，真的关闭物理连接。
        //
        // 为什么这里要真的关闭？
        // 因为连接池不是越大越好，空闲连接太多会浪费数据库资源。
        pooledConnection.closeRealConnection();
    }

    // 关闭连接池里的所有真实物理连接。
    //
    // 一般在应用关闭时调用。
    // 平时业务里的 connection.close() 是归还连接；
    // 这里的 forceCloseAll() 才是把池子里的真实物理连接全部释放。
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

    // 返回空闲连接数量，方便 demo 和测试观察。
    //
    // 真实框架一般会提供监控指标。
    // 这里直接暴露数量，是为了学习时能看见连接池状态变化。
    public synchronized int getIdleConnectionCount() {
        return idleConnections.size();
    }

    // 返回活跃连接数量，方便 demo 和测试观察。
    public synchronized int getActiveConnectionCount() {
        return activeConnections.size();
    }

    // 判断物理连接是否还能用。
    //
    // 教学版只检查 null 和 isClosed。
    // 真正连接池还可能执行一条测试 SQL，比如 select 1，确认连接没有被数据库断开。
    static boolean isRealConnectionValid(Connection realConnection) {
        try {
            return realConnection != null && !realConnection.isClosed();
        } catch (SQLException exception) {
            return false;
        }
    }
}
