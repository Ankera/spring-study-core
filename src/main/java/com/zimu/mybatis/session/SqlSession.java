package com.zimu.mybatis.session;

// 导入映射语句对象。
import com.zimu.mybatis.mapping.MappedStatement;

// 导入列表接口。
import java.util.List;

// 这是 mini MyBatis 的会话接口。
public interface SqlSession extends AutoCloseable {

    // 根据 mapper 接口类型获取代理对象。
    <T> T getMapper(Class<T> mapperType);

    // 执行单条查询并返回单个对象。
    <T> T selectOne(String statementId, Object parameter);

    // 执行列表查询并返回多个对象。
    <T> List<T> selectList(String statementId, Object parameter);

    // 执行一条 insert 语句，并返回影响行数。
    int insert(String statementId, Object parameter);

    // 根据 statementId 获取对应的映射语句。
    MappedStatement getMappedStatement(String statementId);

    // 关闭会话。
    @Override
    void close();
}
