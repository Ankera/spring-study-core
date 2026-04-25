package com.zimu.mybatis.builder;

// 导入配置对象。
import com.zimu.mybatis.config.Configuration;
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
                default -> throw new IllegalArgumentException("不认识的数据库配置项: " + name);
            }
        }
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
