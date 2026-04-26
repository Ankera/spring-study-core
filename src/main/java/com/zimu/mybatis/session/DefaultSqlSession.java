package com.zimu.mybatis.session;

// 导入代理工厂。
import com.zimu.mybatis.binding.MapperProxyFactory;
// 导入配置对象。
import com.zimu.mybatis.config.Configuration;
// 导入执行器。
import com.zimu.mybatis.executor.SimpleExecutor;
// 导入映射语句。
import com.zimu.mybatis.mapping.MappedStatement;

// 这个类是 mini MyBatis 里的核心门面对象。
public class DefaultSqlSession implements SqlSession {

    // 保存全局配置。
    private final Configuration configuration;

    // 保存执行器对象。
    private final SimpleExecutor simpleExecutor = new SimpleExecutor();

    // 构造器接收配置对象。
    public DefaultSqlSession(Configuration configuration) {
        this.configuration = configuration;
    }

    // 返回 mapper 代理对象。
    @Override
    public <T> T getMapper(Class<T> mapperType) {
        // 创建代理工厂。
        MapperProxyFactory<T> mapperProxyFactory = new MapperProxyFactory<>(mapperType);

        // 返回代理实例。
        return mapperProxyFactory.newInstance(this);
    }

    // 根据 statementId 执行单条查询。
    @Override
    public <T> T selectOne(String statementId, Object parameter) {
        // 先根据 statementId 找到对应的映射语句。
        MappedStatement mappedStatement = configuration.getMappedStatement(statementId);

        // 如果没有配置这条语句，就直接报错。
        if (mappedStatement == null) {
            throw new IllegalArgumentException("找不到对应的 MappedStatement: " + statementId);
        }

        // 交给执行器真正执行。
        return simpleExecutor.query(configuration, mappedStatement, parameter);
    }

    // 根据 statementId 执行一条 insert。
    @Override
    public int insert(String statementId, Object parameter) {
        // 先根据 statementId 找到对应的映射语句。
        MappedStatement mappedStatement = configuration.getMappedStatement(statementId);

        // 如果没有配置这条语句，就直接报错。
        if (mappedStatement == null) {
            throw new IllegalArgumentException("找不到对应的 MappedStatement: " + statementId);
        }

        // 交给执行器真正执行更新语句。
        return simpleExecutor.update(configuration, mappedStatement, parameter);
    }

    // 根据 statementId 获取映射语句。
    @Override
    public MappedStatement getMappedStatement(String statementId) {
        return configuration.getMappedStatement(statementId);
    }

    // 当前示例没有长期持有连接，所以这里先留空。
    @Override
    public void close() {
    }
}
