package com.zimu.demo.repository;

// 导入仓库注解。
import com.zimu.spring.annotation.Repository;
// 导入值注入注解。
import com.zimu.spring.annotation.Value;

// 标记这是持久层组件。
@Repository
// 持久层负责和“数据源”打交道。
public class UserRepository {

    // 这里演示从配置文件里读取数据库名字。
    @Value("${db.name}")
    private String databaseName;

    // 模拟查询数据库的方法。
    public String findUserNameById(Long userId) {
        // 打印一行，表示已经进入持久层。
        System.out.println("3. Repository 正在查询数据库: " + databaseName + "，用户 id: " + userId);

        // 这里不连真实数据库，直接返回模拟数据。
        return "小白同学";
    }
}
