package com.zimu.mybatis.binding;

// 导入会话接口。
import com.zimu.mybatis.session.SqlSession;

// 导入调用处理器。
import java.lang.reflect.InvocationHandler;
// 导入方法对象。
import java.lang.reflect.Method;

// 这个类是动态代理真正干活的地方。
public class MapperProxy<T> implements InvocationHandler {

    // 保存当前使用的 SqlSession。
    private final SqlSession sqlSession;

    // 保存 mapper 接口类型。
    private final Class<T> mapperInterface;

    // 构造器中接收会话和接口类型。
    public MapperProxy(SqlSession sqlSession, Class<T> mapperInterface) {
        this.sqlSession = sqlSession;
        this.mapperInterface = mapperInterface;
    }

    // 每次调用 mapper 接口方法，都会走到这里。
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        // 如果是 Object 自带的方法，比如 toString，就直接按普通反射调用。
        if (Object.class.equals(method.getDeclaringClass())) {
            try {
                return method.invoke(this, args);
            } catch (Exception exception) {
                throw new RuntimeException("调用 Object 基础方法失败", exception);
            }
        }

        // 约定 statementId = 接口全限定名 + 方法名。
        String statementId = mapperInterface.getName() + "." + method.getName();

        // 当前示例只演示单参数查询，所以有参数就取第一个，没有就传 null。
        Object parameter = args == null || args.length == 0 ? null : args[0];

        // 调用 SqlSession 执行查询。
        return sqlSession.selectOne(statementId, parameter);
    }
}
