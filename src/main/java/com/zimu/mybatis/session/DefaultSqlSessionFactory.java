package com.zimu.mybatis.session;

// 导入配置对象。
import com.zimu.mybatis.config.Configuration;

// 这是默认的 SqlSessionFactory 实现类。
public class DefaultSqlSessionFactory implements SqlSessionFactory {

    // 保存全局配置对象。
    private final Configuration configuration;

    // 构造器接收配置对象。
    public DefaultSqlSessionFactory(Configuration configuration) {
        this.configuration = configuration;
    }

    // 每次调用都返回一个新的会话对象。
    @Override
    public SqlSession openSession() {
        return new DefaultSqlSession(configuration);
    }

    // 返回当前配置对象。
    @Override
    public Configuration getConfiguration() {
        return configuration;
    }
}
