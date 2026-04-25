package com.zimu.mybatis.util;

// 导入输入流。
import java.io.InputStream;

// 这个工具类负责从 classpath 里读取资源文件。
public class Resources {

    // 根据资源路径返回输入流。
    public static InputStream getResourceAsStream(String resource) {
        // 用当前线程的类加载器去 classpath 中找资源。
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);

        // 如果找不到，就直接报错，方便学习时尽快发现问题。
        if (inputStream == null) {
            throw new IllegalArgumentException("找不到资源文件: " + resource);
        }

        // 返回输入流。
        return inputStream;
    }
}
