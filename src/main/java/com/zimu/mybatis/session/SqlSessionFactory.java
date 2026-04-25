package com.zimu.mybatis.session;

// 导入配置对象。
import com.zimu.mybatis.config.Configuration;

// 这是会话工厂接口。
public interface SqlSessionFactory {

    // 打开一个新的 SqlSession。
    SqlSession openSession();

    // 返回当前工厂持有的配置对象。
    Configuration getConfiguration();
}
