package com.zimu.mybatis.binding;

// 导入会话接口。
import com.zimu.mybatis.session.SqlSession;

// 导入代理工具类。
import java.lang.reflect.Proxy;

// 这个类专门负责创建 mapper 接口的代理对象。
public class MapperProxyFactory<T> {

    // 保存 mapper 接口类型。
    private final Class<T> mapperInterface;

    // 构造器中传入接口类型。
    public MapperProxyFactory(Class<T> mapperInterface) {
        this.mapperInterface = mapperInterface;
    }

    // 创建代理对象。
    @SuppressWarnings("unchecked")
    public T newInstance(SqlSession sqlSession) {
        // 通过 JDK 动态代理生成接口实现类。
        return (T) Proxy.newProxyInstance(
                mapperInterface.getClassLoader(),
                new Class[]{mapperInterface},
                new MapperProxy<>(sqlSession, mapperInterface)
        );
    }
}
