package com.zimu.mybatis.builder;

// 导入配置对象。
import com.zimu.mybatis.config.Configuration;
// 导入映射语句对象。
import com.zimu.mybatis.mapping.MappedStatement;
// 导入 SQL 类型枚举。
import com.zimu.mybatis.mapping.SqlCommandType;
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

// 这个类负责解析 mapper XML 文件。
public class XmlMapperBuilder {

    // 解析单个 mapper XML。
    public void parse(String mapperResource, Configuration configuration) {
        // 通过类路径读取 mapper XML。
        try (InputStream inputStream = Resources.getResourceAsStream(mapperResource)) {
            // 把 XML 读成 Document。
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputStream);

            // 拿到根节点 <mapper>。
            Element mapperElement = document.getDocumentElement();

            // 读取 namespace，它必须和 mapper 接口全限定名一致。
            String namespace = mapperElement.getAttribute("namespace");

            // 解析所有 select 节点。
            parseStatementNodes(mapperElement, namespace, "select", SqlCommandType.SELECT, configuration);

            // 解析所有 insert 节点。
            parseStatementNodes(mapperElement, namespace, "insert", SqlCommandType.INSERT, configuration);
        } catch (Exception exception) {
            // 解析失败就直接抛异常。
            throw new RuntimeException("解析 mapper XML 失败: " + mapperResource, exception);
        }
    }

    // 统一解析某一类语句节点。
    private void parseStatementNodes(
            Element mapperElement,
            String namespace,
            String tagName,
            SqlCommandType sqlCommandType,
            Configuration configuration
    ) {
        // 先拿到这种标签的所有节点。
        NodeList statementNodes = mapperElement.getElementsByTagName(tagName);

        // 逐个节点解析。
        for (int index = 0; index < statementNodes.getLength(); index++) {
            // 取出当前语句元素。
            Element statementElement = (Element) statementNodes.item(index);

            // 创建映射语句对象。
            MappedStatement mappedStatement = new MappedStatement();

            // 设置 namespace。
            mappedStatement.setNamespace(namespace);

            // 设置方法 id。
            mappedStatement.setId(statementElement.getAttribute("id"));

            // 设置参数类型。
            mappedStatement.setParameterType(statementElement.getAttribute("parameterType"));

            // 设置结果类型。
            mappedStatement.setResultType(statementElement.getAttribute("resultType"));

            // 设置 SQL 类型。
            mappedStatement.setSqlCommandType(sqlCommandType);

            // 设置 SQL 文本。
            mappedStatement.setSql(cleanSql(statementElement.getTextContent()));

            // 注册到配置对象。
            configuration.addMappedStatement(mappedStatement.getStatementId(), mappedStatement);
        }
    }

    // 把换行和多余空格整理成更适合 JDBC 使用的一行 SQL。
    private String cleanSql(String sql) {
        // 把连续空白合并成一个空格，并去掉首尾空格。
        return sql.replaceAll("\\s+", " ").trim();
    }
}
