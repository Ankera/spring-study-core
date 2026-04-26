package com.zimu.demo.mybatis.mapper;

// 导入用户实体类。
import com.zimu.demo.mybatis.entity.User;

// 这个接口没有实现类。
// 在真正的 MyBatis 里，它会在运行时通过动态代理生成实现。
// 我们这里也会用同样的思路生成代理对象。
public interface UserMapper {

    // 按主键查询用户。
    User selectById(Long id);

    // 按用户名查询用户。
    User selectByUsername(String username);

    // 插入一个新用户。
    int insertUser(User user);
}
