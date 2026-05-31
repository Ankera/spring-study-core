package com.zimu.mybatis.executor;

// 导入配置对象。
import com.zimu.mybatis.config.Configuration;
// 导入绑定后的 SQL。
import com.zimu.mybatis.mapping.BoundSql;
// 导入映射语句。
import com.zimu.mybatis.mapping.MappedStatement;
// 导入 MySQL 分页拦截器。
import com.zimu.mybatis.plugin.page.MySqlPageInterceptor;
// 导入 PageHelper。
import com.zimu.mybatis.plugin.page.PageHelper;
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
// 导入数组列表。
import java.util.ArrayList;
// 导入列表接口。
import java.util.List;

// 这个类负责真正执行 SQL。
public class SimpleExecutor {

    // 保存教学版分页拦截器。
    private final MySqlPageInterceptor pageInterceptor = new MySqlPageInterceptor();

    // 根据映射语句和参数对象执行查询。
    public <T> T query(Configuration configuration, MappedStatement mappedStatement, Object parameterObject) {
        // 先把 #{...} 解析成 JDBC 可执行的 SQL。
        BoundSql boundSql = prepareQueryBoundSql(mappedStatement.getSql());

        // 获取结果类型。
        Class<T> resultType = getResultType(mappedStatement.getResultType());

        // 从数据源获取连接并执行查询。
        try (
                Connection connection = ConnectionFactory.getConnection(configuration);
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

    // 根据映射语句和参数对象执行列表查询。
    public <T> List<T> queryList(Configuration configuration, MappedStatement mappedStatement, Object parameterObject) {
        // 先把 #{...} 解析成 JDBC 可执行的 SQL，再让 PageHelper 有机会追加 limit。
        BoundSql boundSql = prepareQueryBoundSql(mappedStatement.getSql());

        // 获取结果类型。
        Class<T> resultType = getResultType(mappedStatement.getResultType());

        // 从数据源获取连接并执行查询。
        try (
                Connection connection = ConnectionFactory.getConnection(configuration);
                PreparedStatement preparedStatement = connection.prepareStatement(boundSql.getJdbcSql())
        ) {
            // 先把方法参数绑定到 PreparedStatement 中。
            setParameters(preparedStatement, boundSql, parameterObject);

            // 执行查询，拿到结果集。
            ResultSet resultSet = preparedStatement.executeQuery();

            // 用列表保存每一行映射出来的对象。
            List<T> resultList = new ArrayList<>();

            // 循环读取每一行。
            while (resultSet.next()) {
                resultList.add(ResultSetMapper.mapRow(resultSet, resultType));
            }

            // 返回完整列表。
            return resultList;
        } catch (Exception exception) {
            // 如果执行失败，就抛异常。
            throw new RuntimeException("执行列表 SQL 失败: " + mappedStatement.getStatementId(), exception);
        }
    }

    // 根据映射语句和参数对象执行更新语句。
    public int update(Configuration configuration, MappedStatement mappedStatement, Object parameterObject) {
        // 先把 #{...} 解析成 JDBC 可执行的 SQL。
        BoundSql boundSql = buildBoundSql(mappedStatement.getSql());

        // 从数据源获取连接并执行更新。
        try (
                Connection connection = ConnectionFactory.getConnection(configuration);
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

    // 构建查询 SQL，并允许 PageHelper 改写成 MySQL 分页 SQL。
    private BoundSql prepareQueryBoundSql(String originalSql) {
        try {
            // 先把 #{...} 解析成 JDBC 可执行的 SQL。
            BoundSql boundSql = buildBoundSql(originalSql);

            // 再让分页拦截器判断是否需要追加 limit。
            return pageInterceptor.intercept(boundSql);
        } finally {
            // PageHelper.startPage 只影响紧跟着的下一次查询。
            // 查询 SQL 准备完成后就清理，避免污染后续 SQL。
            PageHelper.clearPage();
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

        // 继续绑定框架额外追加的固定参数，比如 PageHelper 的 offset 和 pageSize。
        for (int index = 0; index < boundSql.getAdditionalParameterValues().size(); index++) {
            // 计算当前 JDBC 参数下标。
            int parameterIndex = boundSql.getParameterNames().size() + index + 1;

            // 取出追加参数值。
            Object value = boundSql.getAdditionalParameterValues().get(index);

            // 绑定追加参数。
            preparedStatement.setObject(parameterIndex, value);
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
