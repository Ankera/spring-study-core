package com.zimu.mybatis.executor;

// 导入配置对象。
import com.zimu.mybatis.config.Configuration;
// 导入绑定后的 SQL。
import com.zimu.mybatis.mapping.BoundSql;
// 导入映射语句。
import com.zimu.mybatis.mapping.MappedStatement;
// 导入参数解析器。
import com.zimu.mybatis.reflection.ParameterResolver;
// 导入结果集映射器。
import com.zimu.mybatis.reflection.ResultSetMapper;
// 导入连接工厂。
import com.zimu.mybatis.util.ConnectionFactory;
// 导入 token 解析器。
import com.zimu.mybatis.util.GenericTokenParser;

// 导入 JDBC 连接。
import java.sql.Connection;
// 导入预编译语句。
import java.sql.PreparedStatement;
// 导入结果集。
import java.sql.ResultSet;

// 这个类负责真正执行 SQL。
public class SimpleExecutor {

    // 根据映射语句和参数对象执行查询。
    public <T> T query(Configuration configuration, MappedStatement mappedStatement, Object parameterObject) {
        // 先把 #{...} 解析成 JDBC 可执行的 SQL。
        BoundSql boundSql = buildBoundSql(mappedStatement.getSql());

        // 获取结果类型。
        Class<T> resultType = getResultType(mappedStatement.getResultType());

        // 创建数据库连接并执行查询。
        try (
                Connection connection = ConnectionFactory.createConnection(configuration);
                PreparedStatement preparedStatement = connection.prepareStatement(boundSql.getJdbcSql())
        ) {
            // 先把方法参数绑定到 PreparedStatement 中。
            setParameters(preparedStatement, boundSql, parameterObject);

            // 执行查询，拿到结果集。
            ResultSet resultSet = preparedStatement.executeQuery();

            // 如果查到了数据，就把第一行映射成对象返回。
            if (resultSet.next()) {
                return ResultSetMapper.mapRow(resultSet, resultType);
            }

            // 如果没查到数据，就返回 null。
            return null;
        } catch (Exception exception) {
            // 如果执行失败，就抛异常。
            throw new RuntimeException("执行 SQL 失败: " + mappedStatement.getStatementId(), exception);
        }
    }

    // 根据映射语句和参数对象执行更新语句。
    public int update(Configuration configuration, MappedStatement mappedStatement, Object parameterObject) {
        // 先把 #{...} 解析成 JDBC 可执行的 SQL。
        BoundSql boundSql = buildBoundSql(mappedStatement.getSql());

        // 创建数据库连接并执行更新。
        try (
                Connection connection = ConnectionFactory.createConnection(configuration);
                PreparedStatement preparedStatement = connection.prepareStatement(boundSql.getJdbcSql())
        ) {
            // 先绑定参数。
            setParameters(preparedStatement, boundSql, parameterObject);

            // 执行更新，并返回影响行数。
            return preparedStatement.executeUpdate();
        } catch (Exception exception) {
            // 如果执行失败，就抛异常。
            throw new RuntimeException("执行更新 SQL 失败: " + mappedStatement.getStatementId(), exception);
        }
    }

    // 把原始 SQL 转成 BoundSql。
    private BoundSql buildBoundSql(String originalSql) {
        // 先做 token 解析。
        GenericTokenParser.ParsedSql parsedSql = GenericTokenParser.parse(originalSql);

        // 再包装成 BoundSql 对象返回。
        return new BoundSql(parsedSql.getJdbcSql(), parsedSql.getParameterNames());
    }

    // 给 PreparedStatement 设置参数。
    private void setParameters(PreparedStatement preparedStatement, BoundSql boundSql, Object parameterObject) throws Exception {
        // 按顺序遍历每一个占位符参数名。
        for (int index = 0; index < boundSql.getParameterNames().size(); index++) {
            // 取出当前参数名。
            String parameterName = boundSql.getParameterNames().get(index);

            // 根据参数名从方法参数对象里取值。
            Object value = ParameterResolver.resolveValue(parameterObject, parameterName);

            // JDBC 参数下标从 1 开始。
            preparedStatement.setObject(index + 1, value);
        }
    }

    // 根据类名拿到真正的 Class 对象。
    @SuppressWarnings("unchecked")
    private <T> Class<T> getResultType(String resultTypeName) {
        try {
            // 用反射加载结果类型。
            return (Class<T>) Class.forName(resultTypeName);
        } catch (Exception exception) {
            // 如果类找不到，就抛异常。
            throw new RuntimeException("找不到结果类型: " + resultTypeName, exception);
        }
    }
}
