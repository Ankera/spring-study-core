package com.zimu.mybatis.datasource;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;

// 包装一个真实 JDBC 连接，并用动态代理拦截 close 方法。
public class PooledConnection implements InvocationHandler {

    // 真实物理连接。
    private final Connection realConnection;

    // 所属连接池。
    private final PooledDataSource pooledDataSource;

    // 对外暴露的代理连接。
    private final Connection proxyConnection;

    // 标记当前连接是否正在被借出。
    private boolean valid;

    // 构造器接收真实连接和连接池。
    public PooledConnection(Connection realConnection, PooledDataSource pooledDataSource) {
        this.realConnection = realConnection;
        this.pooledDataSource = pooledDataSource;
        this.proxyConnection = (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class[]{Connection.class},
                this
        );
    }

    // 拿到代理连接。
    public Connection getProxyConnection() {
        return proxyConnection;
    }

    // 连接被借出。
    public void checkout() {
        if (!PooledDataSource.isRealConnectionValid(realConnection)) {
            throw new RuntimeException("真实数据库连接已经失效");
        }
        valid = true;
    }

    // 连接被归还。
    public void checkin() {
        valid = false;
    }

    // 真的关闭物理连接。
    public void closeRealConnection() {
        valid = false;
        try {
            realConnection.close();
        } catch (Exception exception) {
            throw new RuntimeException("关闭真实数据库连接失败", exception);
        }
    }

    // 所有对 Connection 的调用都会先进入这里。
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // close 方法不关闭真实连接，而是还给连接池。
        if ("close".equals(method.getName())) {
            if (valid) {
                pooledDataSource.pushConnection(this);
            }
            return null;
        }

        if (!valid) {
            throw new RuntimeException("连接已经归还到连接池，不能继续使用");
        }

        // 其他方法还是交给真实连接处理。
        return method.invoke(realConnection, args);
    }
}
