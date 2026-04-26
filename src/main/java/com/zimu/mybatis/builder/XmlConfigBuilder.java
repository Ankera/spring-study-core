package com.zimu.mybatis.builder;

// 导入配置对象。
import com.zimu.mybatis.config.Configuration;
// 导入池化数据源。
import com.zimu.mybatis.datasource.PooledDataSource;
// 导入非池化数据源。
import com.zimu.mybatis.datasource.UnpooledDataSource;
// 导入资源读取工具。
import com.zimu.mybatis.util.Resources;

// 导入文档对象。
import org.w3c.dom.Document;
// 导入元素对象。
import org.w3c.dom.Element;
// 导入节点列表。
import org.w3c.dom.NodeList;

// 导入文档构建工厂。
import javax.xml.parsers.DocumentBuilderFactory;
// 导入输入流。
import java.io.InputStream;

// 这个类负责解析 mybatis-config.xml。
public class XmlConfigBuilder {

    // 解析总配置文件，并返回配置对象。
    public Configuration parse(String configResource) {
        // 创建配置对象。
        Configuration configuration = new Configuration();

        // 通过类路径读取配置文件。
        try (InputStream inputStream = Resources.getResourceAsStream(configResource)) {
            // 把 XML 解析成 DOM 文档。
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputStream);

            // 拿到根元素。
            Element rootElement = document.getDocumentElement();

            // 先解析数据库连接信息。
            parseEnvironment(rootElement, configuration);

            // 根据配置创建数据源。
            //
            // 为什么在解析配置文件时就创建 DataSource？
            // 因为 SqlSessionFactory 构建完成后，后面的执行流程只需要从 Configuration 拿数据源。
            // 这样执行器不会再关心 driver/url，也不会关心是否启用连接池。
            buildDataSource(configuration);

            // 再解析 mapper XML 列表。
            parseMappers(rootElement, configuration);

            // 返回配置对象。
            return configuration;
        } catch (Exception exception) {
            // 解析失败就抛异常。
            throw new RuntimeException("解析 mybatis-config.xml 失败: " + configResource, exception);
        }
    }

    // 解析数据库连接配置。
    private void parseEnvironment(Element rootElement, Configuration configuration) {
        // 找到 environment 节点。
        Element environmentElement = (Element) rootElement.getElementsByTagName("environment").item(0);

        // 读取里面所有 property 节点。
        NodeList propertyNodes = environmentElement.getElementsByTagName("property");

        // 遍历所有 property。
        for (int index = 0; index < propertyNodes.getLength(); index++) {
            // 取出当前 property 元素。
            Element propertyElement = (Element) propertyNodes.item(index);

            // 读取 name。
            String name = propertyElement.getAttribute("name");

            // 读取 value。
            String value = propertyElement.getAttribute("value");

            // 按名字分别设置到配置对象中。
            switch (name) {
                case "driver" -> configuration.setDriver(value);
                case "url" -> configuration.setUrl(value);
                case "username" -> configuration.setUsername(value);
                case "password" -> configuration.setPassword(value);
                // 是否开启连接池。
                case "poolEnabled" -> configuration.setPoolEnabled(Boolean.parseBoolean(value));
                // 最多允许同时借出去多少个连接。
                case "poolMaximumActiveConnections" -> configuration.setPoolMaximumActiveConnections(Integer.parseInt(value));
                // 最多保留多少个空闲连接等待复用。
                case "poolMaximumIdleConnections" -> configuration.setPoolMaximumIdleConnections(Integer.parseInt(value));
                default -> throw new IllegalArgumentException("不认识的数据库配置项: " + name);
            }
        }
    }

    // 根据配置创建数据源。
    private void buildDataSource(Configuration configuration) {
        // 非池化数据源负责真的创建物理连接。
        //
        // 即使最终启用连接池，也要先有一个能创建真实连接的对象。
        // 连接池只是复用连接，不是凭空变出连接。
        UnpooledDataSource unpooledDataSource = new UnpooledDataSource(
                configuration.getDriver(),
                configuration.getUrl(),
                configuration.getUsername(),
                configuration.getPassword()
        );

        // 如果启用连接池，就用池化数据源包装一层。
        //
        // 这就是装饰/包装的思路：
        // 原本 UnpooledDataSource 只会新建连接；
        // 外面包一层 PooledDataSource 后，就多了“缓存和复用”的能力。
        if (configuration.isPoolEnabled()) {
            configuration.setDataSource(new PooledDataSource(
                    unpooledDataSource,
                    configuration.getPoolMaximumActiveConnections(),
                    configuration.getPoolMaximumIdleConnections()
            ));
            return;
        }

        // 否则每次都新建连接。
        //
        // 保留非池化模式，是为了对比学习：
        // 同一套 Executor 代码，可以在池化/非池化之间切换。
        configuration.setDataSource(unpooledDataSource);
    }

    // 解析所有 mapper 配置。
    private void parseMappers(Element rootElement, Configuration configuration) {
        // 找到 mappers 节点。
        Element mappersElement = (Element) rootElement.getElementsByTagName("mappers").item(0);

        // 读取里面所有 mapper 节点。
        NodeList mapperNodes = mappersElement.getElementsByTagName("mapper");

        // 创建 mapper XML 解析器。
        XmlMapperBuilder xmlMapperBuilder = new XmlMapperBuilder();

        // 遍历所有 mapper 配置。
        for (int index = 0; index < mapperNodes.getLength(); index++) {
            // 取出当前 mapper 元素。
            Element mapperElement = (Element) mapperNodes.item(index);

            // 读取 resource 路径。
            String mapperResource = mapperElement.getAttribute("resource");

            // 把对应的 mapper XML 解析进配置对象。
            xmlMapperBuilder.parse(mapperResource, configuration);
        }
    }
}
