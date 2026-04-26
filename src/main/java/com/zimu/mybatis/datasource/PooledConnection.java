package com.zimu.mybatis.datasource;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;

// 包装一个真实 JDBC 连接，并用动态代理拦截 close 方法。
//
// 这是连接池里最关键、也最值得学习的类。
//
// 业务代码写的是：
//
// try (Connection connection = dataSource.getConnection()) {
//     ...
// }
//
// try-with-resources 结束时一定会调用 connection.close()。
//
// 如果把真实 Connection 直接返回给业务代码，close() 就会真的关闭物理连接，
// 那连接池就失去了复用连接的意义。
//
// 所以这里返回一个“代理 Connection”：
// - 其他方法，比如 prepareStatement，还是交给真实连接执行
// - close 方法被特殊处理，改成归还连接池
//
// 这就是为什么连接池可以让你继续正常写 close，
// 但底层却没有真的把物理连接关掉。
public class PooledConnection implements InvocationHandler {

    // 真实物理连接。
    //
    // 这个对象才是真的连着数据库的连接。
    // 业务代码不应该直接拿到它，否则 close 无法被连接池接管。
    private final Connection realConnection;

    // 所属连接池。
    //
    // close 被拦截后，需要把自己归还给这个池子。
    private final PooledDataSource pooledDataSource;

    // 对外暴露的代理连接。
    //
    // 业务代码拿到的是它。
    // 它实现了 java.sql.Connection 接口，所以使用方式和真实连接一样。
    private final Connection proxyConnection;

    // 标记当前连接是否正在被借出。
    //
    // valid = true：连接已经被借出，可以使用。
    // valid = false：连接已经归还池子，业务代码不应该再继续调用它。
    private boolean valid;

    // 构造器接收真实连接和连接池。
    public PooledConnection(Connection realConnection, PooledDataSource pooledDataSource) {
        this.realConnection = realConnection;
        this.pooledDataSource = pooledDataSource;

        // 创建 JDK 动态代理对象。
        //
        // 为什么用动态代理？
        // 因为 java.sql.Connection 是接口，JDK 动态代理正好可以生成一个实现了 Connection 的对象。
        // 这样我们不用手写几十个 Connection 方法，只需要在 invoke 里统一拦截。
        this.proxyConnection = (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class[]{Connection.class},
                this
        );
    }

    // 拿到代理连接。
    //
    // 连接池借出连接时，返回这个代理对象，而不返回 realConnection。
    public Connection getProxyConnection() {
        return proxyConnection;
    }

    // 连接被借出。
    //
    // 每次从池子里拿出去之前，都标记为可用。
    // 同时检查真实物理连接是不是已经被关闭。
    public void checkout() {
        if (!PooledDataSource.isRealConnectionValid(realConnection)) {
            throw new RuntimeException("真实数据库连接已经失效");
        }
        valid = true;
    }

    // 连接被归还。
    //
    // 标记为不可用，是为了避免这种错误用法：
    //
    // Connection c = dataSource.getConnection();
    // c.close();
    // c.prepareStatement(...); // 已经归还池子了，不应该继续用
    public void checkin() {
        valid = false;
    }

    // 真的关闭物理连接。
    //
    // 注意区分两个 close：
    // - proxyConnection.close()：归还连接池
    // - realConnection.close()：真的断开数据库物理连接
    //
    // 只有连接池决定不要缓存这个连接，或者应用关闭时，才调用这里。
    public void closeRealConnection() {
        valid = false;
        try {
            realConnection.close();
        } catch (Exception exception) {
            throw new RuntimeException("关闭真实数据库连接失败", exception);
        }
    }

    // 所有对 Connection 的调用都会先进入这里。
    //
    // 动态代理的核心就是这个 invoke 方法。
    // 业务代码调用 proxyConnection.prepareStatement(...)，
    // 实际上会先进到这里，再由这里决定怎么处理。
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // close 方法不关闭真实连接，而是还给连接池。
        //
        // 这是连接池最核心的设计点：
        // 让业务代码保持 JDBC 习惯，正常 close；
        // 但连接池偷偷把 close 的语义改成“归还”。
        if ("close".equals(method.getName())) {
            // valid 用来防止重复 close 导致同一个连接被重复放进 idleConnections。
            if (valid) {
                pooledDataSource.pushConnection(this);
            }
            return null;
        }

        // 如果连接已经归还池子，就不允许业务代码继续使用。
        // 否则可能出现同一条物理连接同时被两段代码使用的危险。
        if (!valid) {
            throw new RuntimeException("连接已经归还到连接池，不能继续使用");
        }

        // 其他方法还是交给真实连接处理。
        //
        // 比如 prepareStatement、commit、rollback 等，
        // 连接池不改变它们的语义，只改变 close 的语义。
        return method.invoke(realConnection, args);
    }
}
