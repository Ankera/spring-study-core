package com.zimu;

// 导入 JUnit 测试注解。
import org.junit.jupiter.api.Test;

// 这个测试类只是一个很薄的包装。
// 这样做的目的，是让 Maven 能识别到标准测试类，同时继续复用 Main.java 里的 test() 方法。
public class MainTest {

    // 直接调用 Main 里的测试方法。
    @Test
    public void runMainTestMethod() {
        new Main().test();
    }


}
