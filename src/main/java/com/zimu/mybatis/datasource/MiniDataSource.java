package com.zimu.mybatis.datasource;

import java.sql.Connection;

// mini MyBatis 自己定义的极简数据源接口。
//
// 为什么要抽一个接口？
// 因为执行 SQL 的代码只应该关心“给我一个 Connection”，不应该关心连接到底怎么来的。
//
// 这样 SimpleExecutor 就可以保持稳定：
// - 想每次新建连接，就传 UnpooledDataSource
// - 想复用连接，就传 PooledDataSource
//
// 这就是 MyBatis 里 DataSource 这一层最核心的设计味道：
// 把“获取连接”这件事抽出去，让执行器专心执行 SQL。
public interface MiniDataSource {

    // 获取一个数据库连接。
    //
    // 注意：
    // 这里返回的是 java.sql.Connection 接口。
    // 如果是非池化数据源，它可能是真实物理连接。
    // 如果是池化数据源，它可能是一个代理连接，close() 已经被改造成“归还连接”。
    Connection getConnection();
}
