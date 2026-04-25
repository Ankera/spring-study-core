package com.zimu.mybatis.session;

// 这是 mini MyBatis 的会话接口。
public interface SqlSession extends AutoCloseable {

    // 根据 mapper 接口类型获取代理对象。
    <T> T getMapper(Class<T> mapperType);

    // 执行单条查询并返回单个对象。
    <T> T selectOne(String statementId, Object parameter);

    // 关闭会话。
    @Override
    void close();
}
