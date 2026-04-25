package com.zimu.spring.util;

// 导入文件类。
import java.io.File;
// 导入 URL 类。
import java.net.URL;
// 导入解码工具。
import java.net.URLDecoder;
// 导入字符集常量。
import java.nio.charset.StandardCharsets;
// 导入数组列表。
import java.util.ArrayList;
// 导入枚举类型。
import java.util.Enumeration;
// 导入列表接口。
import java.util.List;

// 这个工具类专门负责“扫描某个包下面有哪些 class 文件”。
public class ClassScanner {

    // 对外提供一个静态方法，传入包名，返回这个包下面的所有 Class。
    public static List<Class<?>> scan(String basePackage) {
        // 创建结果列表，用来存放扫描到的类。
        List<Class<?>> classes = new ArrayList<>();

        // 把 com.zimu.demo 这种包名换成 com/zimu/demo 这种路径。
        String packagePath = basePackage.replace(".", "/");

        try {
            // 通过当前线程的类加载器去找这个路径下的资源。
            Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources(packagePath);

            // 一个包路径可能对应多个资源，所以这里用 while 遍历。
            while (resources.hasMoreElements()) {
                // 取出其中一个资源地址。
                URL resource = resources.nextElement();

                // 把 URL 中可能存在的中文或空格编码还原。
                String filePath = URLDecoder.decode(resource.getFile(), StandardCharsets.UTF_8);

                // 递归扫描这个目录里的 class 文件。
                findClasses(new File(filePath), basePackage, classes);
            }
        } catch (Exception exception) {
            // 如果扫描出错，直接抛出更容易理解的运行时异常。
            throw new RuntimeException("扫描包失败: " + basePackage, exception);
        }

        // 返回扫描结果。
        return classes;
    }

    // 这个私有方法负责递归往下找 class 文件。
    private static void findClasses(File directory, String packageName, List<Class<?>> classes) throws ClassNotFoundException {
        // 如果目录不存在，直接结束。
        if (!directory.exists()) {
            return;
        }

        // 读取当前目录下的所有文件。
        File[] files = directory.listFiles();

        // 如果读不到文件，也直接结束。
        if (files == null) {
            return;
        }

        // 遍历当前目录下的所有文件和子目录。
        for (File file : files) {
            // 如果当前是目录，就继续递归扫描。
            if (file.isDirectory()) {
                // 递归时，要把包名拼上子目录名字。
                findClasses(file, packageName + "." + file.getName(), classes);
                // 处理完目录后继续下一个文件。
                continue;
            }

            // 如果文件不是 .class 结尾，就跳过。
            if (!file.getName().endsWith(".class")) {
                continue;
            }

            // 如果是内部类文件，就先跳过，避免让示例太复杂。
            if (file.getName().contains("$")) {
                continue;
            }

            // 去掉 .class 后缀，得到简单类名。
            String simpleClassName = file.getName().replace(".class", "");

            // 拼出完整类名，比如 com.zimu.demo.service.UserService。
            String fullClassName = packageName + "." + simpleClassName;

            // 用反射把类真正加载进来，并放进结果列表。
            classes.add(Class.forName(fullClassName));
        }
    }
}
