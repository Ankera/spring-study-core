package com.zimu.mybatis.datasource;

import java.sql.Connection;

// mini MyBatis 自己定义的极简数据源接口。
public interface MiniDataSource {

    // 获取一个数据库连接。
    Connection getConnection();
}
