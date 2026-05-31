package com.zimu.demo.mybatis.support;

// 导入用户实体类。
import com.zimu.demo.mybatis.entity.User;
// 导入用户 mapper。
import com.zimu.demo.mybatis.mapper.UserMapper;
// 导入 mini MyBatis 配置。
import com.zimu.mybatis.config.Configuration;
// 导入教学版 PageHelper。
import com.zimu.mybatis.plugin.page.PageHelper;
// 导入会话接口。
import com.zimu.mybatis.session.SqlSession;
// 导入会话工厂。
import com.zimu.mybatis.session.SqlSessionFactory;
// 导入会话工厂构建器。
import com.zimu.mybatis.session.SqlSessionFactoryBuilder;

// 导入列表接口。
import java.util.List;

// 这个类专门演示 PageHelper.startPage 是如何影响下一次 MyBatis 查询的。
public class PageHelperMiniDemo {

    // main 方法可以直接运行这个分页示例。
    public static void main(String[] args) {
        // 先根据配置文件创建会话工厂。
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build("mybatis-config.xml");

        // 拿到配置对象，用来初始化演示数据库。
        Configuration configuration = sqlSessionFactory.getConfiguration();

        // 初始化用户表和测试数据。
        DatabaseInitializer.initialize(configuration);

        // try-with-resources 可以保证会话最终被关闭。
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            // 从会话中拿到 mapper 代理对象。
            UserMapper userMapper = sqlSession.getMapper(UserMapper.class);

            // 不分页时，selectAll 会查出全部数据。
            List<User> allUsers = userMapper.selectAll();

            // 这里就对应你平时写的：
            // PageHelper.startPage(dto.getPage(), dto.getPageSize());
            //
            // 它本身不会立刻查数据库，只是把分页参数保存到 ThreadLocal。
            PageHelper.startPage(2, 2);

            // 紧跟着的第一次 select 查询会被分页拦截器改写。
            // 原 SQL：
            // select id, username, password, age from t_user order by id
            //
            // 执行前会变成 MySQL 风格：
            // select id, username, password, age from t_user order by id limit ?, ?
            //
            // page=2，pageSize=2，所以参数是 offset=2，pageSize=2。
            List<User> pageUsers = userMapper.selectAll();

            // 再查一次，分页参数已经被清理，所以这里又是全部数据。
            List<User> usersAfterPage = userMapper.selectAll();

            // 打印全部数据数量。
            System.out.println("全部用户数量: " + allUsers.size());

            // 打印分页数据。
            System.out.println("第 2 页，每页 2 条: " + pageUsers);

            // 打印分页后再次查询的数据数量。
            System.out.println("分页后再次查询数量: " + usersAfterPage.size());
        }
    }
}
