package com.zimu.mybatis.session;

// 导入配置解析器。
import com.zimu.mybatis.builder.XmlConfigBuilder;
// 导入配置对象。
import com.zimu.mybatis.config.Configuration;

// 这个类专门负责根据配置文件构建会话工厂。
public class SqlSessionFactoryBuilder {

    // 根据配置文件路径构建 SqlSessionFactory。
    public SqlSessionFactory build(String configResource) {
        // 先解析配置文件。
        Configuration configuration = new XmlConfigBuilder().parse(configResource);

        // 再把配置对象交给工厂实现类。
        return new DefaultSqlSessionFactory(configuration);
    }
}
