package com.zimu.demo.service;

// 导入仓库类。
import com.zimu.demo.repository.UserRepository;
// 导入服务注解。
import com.zimu.spring.annotation.Service;

// 标记这是业务层组件。
@Service
// 业务层负责处理业务逻辑。
public class UserService {

    // 业务层依赖持久层对象。
    private final UserRepository userRepository;

    // 这里只有一个构造器，所以容器会自动注入 UserRepository。
    public UserService(UserRepository userRepository) {
        // 保存依赖对象。
        this.userRepository = userRepository;
    }

    // 这是业务方法。
    public String getUserInfo() {
        // 打印一行，表示已经进入业务层。
        System.out.println("2. Service 开始处理业务逻辑");

        // 调用持久层拿到数据。
        String userName = userRepository.findUserNameById(1L);

        // 拼出一个更像业务返回值的字符串。
        return "用户信息查询成功，用户名是: " + userName;
    }
}
