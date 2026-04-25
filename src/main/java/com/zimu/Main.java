package com.zimu;

// 导入我们自己写的用户实体类。
import com.zimu.demo.mybatis.entity.User;
// 导入我们自己写的 mapper 接口。
import com.zimu.demo.mybatis.mapper.UserMapper;
// 导入数据库初始化工具。
import com.zimu.demo.mybatis.support.DatabaseInitializer;
// 导入我们自己写的配置类。
import com.zimu.demo.config.AppConfig;
// 导入我们自己写的控制器类。
import com.zimu.demo.controller.UserController;
// 导入我们自己写的普通 Bean 类。
import com.zimu.demo.bean.SystemReporter;
// 导入 mini MyBatis 的工厂构建器。
import com.zimu.mybatis.config.Configuration;
import com.zimu.mybatis.session.SqlSessionFactoryBuilder;
// 导入 mini MyBatis 的会话接口。
import com.zimu.mybatis.session.SqlSession;
// 导入 mini MyBatis 的会话工厂接口。
import com.zimu.mybatis.session.SqlSessionFactory;
// 导入我们自己写的容器类。
import com.zimu.spring.context.ApplicationContext;
// 导入 JUnit 的测试注解。
import org.junit.jupiter.api.Test;

// 这是程序启动入口。
public class Main {

    // main 方法就是 Java 程序最先执行的地方。
    public static void main(String[] args) {
        // 先演示我们自己写的 mini Spring。
        runSpringDemo();

        // 再演示我们自己写的 mini MyBatis。
        runMyBatisDemo();
    }

    // 这个方法单独负责演示 mini Spring。
    private static void runSpringDemo() {
        // 创建我们自己的容器，并把配置类传进去。
        ApplicationContext applicationContext = new ApplicationContext(AppConfig.class);

        // 从容器里拿到控制器对象。
        UserController userController = applicationContext.getBean(UserController.class);

        // 调用控制器方法，模拟一次“请求处理”。
        userController.queryUser();

        // 再从容器里拿到 @Bean 创建出来的对象。
        SystemReporter systemReporter = applicationContext.getBean(SystemReporter.class);

        // 调用普通 Bean 的方法，看看 @Bean 和 @Value 是否生效。
        systemReporter.printSummary();
    }

    // 这个方法单独负责演示 mini MyBatis。
    private static void runMyBatisDemo() {
        // 先根据配置文件创建会话工厂。
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build("mybatis-config.xml");

        Configuration configuration = sqlSessionFactory.getConfiguration();
        // 先初始化数据库表和测试数据。
        DatabaseInitializer.initialize(configuration);

        // try-with-resources 可以保证会话最终被关闭。
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            // 从会话中拿到 mapper 代理对象。
            UserMapper userMapper = sqlSession.getMapper(UserMapper.class);

            // 调用接口方法，底层会自动转成 SQL 查询。
            User userById = userMapper.selectById(1L);

            // 再查一条用户名查询，看看另一个 SQL 是否生效。
            User userByUsername = userMapper.selectByUsername("lisi");

            // 打印查询结果。
            System.out.println("6. mini MyBatis 按 id 查询结果: " + userById);
            // 打印第二个查询结果。
            System.out.println("7. mini MyBatis 按用户名查询结果: " + userByUsername);
        }
    }

    // 这个测试方法可以直接跑 mini MyBatis 示例。
    @Test
    public void test() {
        // 在测试里直接复用同一套演示代码。
        runMyBatisDemo();
    }
}
