package com.zimu.demo.mybatis.support;

// 导入用户实体类。
import com.zimu.demo.mybatis.entity.User;
// 导入参数解析器。
import com.zimu.mybatis.reflection.ParameterResolver;
// 导入 token 解析器。
import com.zimu.mybatis.util.GenericTokenParser;

// 这个 demo 专门演示 #{username}、#{password} 是怎么被解析的。
public class PlaceholderBindingDemo {

    // 直接运行这个 main，就能看到占位符解析全过程。
    public static void main(String[] args) {
        // 模拟 mapper XML 里的一段 insert SQL。
        String originalSql = "insert into t_user(username, password, age) values (#{username}, #{password}, #{age})";

        // 准备一个参数对象，模拟 mapper 方法传进来的 user。
        User user = new User();
        user.setUsername("zhaoliu");
        user.setPassword("pass999");
        user.setAge(25);

        // 第一步：先把 #{...} 解析成 JDBC 的 ?。
        GenericTokenParser.ParsedSql parsedSql = GenericTokenParser.parse(originalSql);

        // 打印原始 SQL。
        System.out.println("原始 SQL: " + originalSql);

        // 打印替换后的 JDBC SQL。
        System.out.println("解析后 SQL: " + parsedSql.getJdbcSql());

        // 打印参数名顺序。
        System.out.println("参数名顺序: " + parsedSql.getParameterNames());

        // 第二步：根据参数名，去参数对象里一个个取值。
        for (String parameterName : parsedSql.getParameterNames()) {
            // 从 user 对象里取出当前参数对应的值。
            Object value = ParameterResolver.resolveValue(user, parameterName);

            // 打印取值结果。
            System.out.println("参数名 = " + parameterName + "，取出的值 = " + value);
        }
    }
}
