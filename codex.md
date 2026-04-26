# Codex 对话学习记录

这份文档是我和 Codex 在这个项目里的学习过程总结。

目的很简单：

- 换电脑之后还能快速接上
- 不用重新翻全部聊天记录
- 知道这个项目现在已经做到哪了
- 知道下一步最适合继续学什么

---

## 1. 这个项目一开始做了什么

一开始，这个项目被做成了一个“迷你版 Spring 核心流程”学习项目。

已经实现过这些注解和核心机制：

- `@Component`
- `@Controller`
- `@Service`
- `@Repository`
- `@Configuration`
- `@Bean`
- `@Value`
- `@Autowired`

还手写了一个最小可运行的 IoC 容器，大概包括：

- 包扫描
- 判断哪些类属于组件
- 创建对象
- 构造器注入
- 字段 `@Autowired` 注入
- 字段 `@Value` 注入
- 处理 `@Configuration + @Bean`

根目录已经有一份 Spring 学习笔记：

- `README.md`

它里面已经写了：

- `@Configuration` 为什么存在
- `@Bean`、`@Service`、`@Configuration` 的区别
- 容器启动流程图
- 对应源码入口

---

## 2. 后面又继续做了什么

在这个基础上，又继续手写了一个“迷你版 MyBatis 学习实现”。

注意：

这里没有引入 MyBatis 相关三方库。

底层还是我们自己手写逻辑。

只是额外用了一个 H2 内存数据库做演示，这样不用本地先装 MySQL。

这个 mini MyBatis 当前已经具备这些能力：

1. 读取 `mybatis-config.xml`
2. 读取 `mapper.xml`
3. 解析 `select`
4. 解析 `insert`
5. 根据 `namespace + id` 找到 SQL
6. 通过 JDK 动态代理生成 mapper 接口实现
7. 把 `#{}` 解析成 JDBC 的 `?`
8. 根据参数名从对象中取值
9. 绑定到 `PreparedStatement`
10. 执行查询
11. 执行插入
12. 把结果集映射成 Java 对象

---

## 3. mini MyBatis 里最重要的理解

### 3.1 MyBatis 底层还是 JDBC

这是反复强调过的重点。

MyBatis 不是不用 JDBC。

它只是帮我们把这些重复工作包装起来了：

- 写 SQL
- 绑定参数
- 执行查询
- 取结果
- 封装对象

### 3.2 `SqlSessionFactory`、`SqlSession`、`Mapper` 的关系

白话理解：

- `SqlSessionFactory`：像工厂，负责生产 `SqlSession`
- `SqlSession`：像会话入口，负责拿 mapper、执行 SQL
- `Mapper`：像程序员平时用的数据库接口

### 3.3 `getMapper()` 为什么能返回 `UserMapper`

这是对话里专门解释过的一点。

关键不是它返回了 `SqlSession`。

而是：

`sqlSession.getMapper(UserMapper.class)` 会创建一个“实现了 `UserMapper` 接口的动态代理对象”。

所以左边可以这样接：

```java
UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
```

本质上相当于：

```java
UserMapper userMapper = (UserMapper) Proxy.newProxyInstance(...);
```

也就是说：

- 返回的不是 `SqlSession`
- 返回的是“代理对象”
- 这个代理对象实现了 `UserMapper`

### 3.4 JDK 动态代理和 MyBatis 的关系

中间专门讲过一个对比：

普通 JDK 代理 demo：

- 代理对象背后调用真实实现类方法

mini MyBatis：

- 代理对象背后不是调用某个 `UserMapperImpl`
- 而是根据接口名 + 方法名去找 SQL
- 然后执行数据库操作

所以一句话总结：

普通代理：

“代理后去调真实 Java 方法”

MyBatis 代理：

“代理后去调 SQL”

---

## 4. mini MyBatis 当前有哪些核心文件

### 4.1 配置和工厂

- `src/main/resources/mybatis-config.xml`
- `src/main/java/com/zimu/mybatis/session/SqlSessionFactoryBuilder.java`
- `src/main/java/com/zimu/mybatis/session/SqlSessionFactory.java`
- `src/main/java/com/zimu/mybatis/session/DefaultSqlSessionFactory.java`
- `src/main/java/com/zimu/mybatis/session/SqlSession.java`
- `src/main/java/com/zimu/mybatis/session/DefaultSqlSession.java`

### 4.2 XML 解析

- `src/main/java/com/zimu/mybatis/builder/XmlConfigBuilder.java`
- `src/main/java/com/zimu/mybatis/builder/XmlMapperBuilder.java`

### 4.3 Mapper 代理

- `src/main/java/com/zimu/mybatis/binding/MapperProxy.java`
- `src/main/java/com/zimu/mybatis/binding/MapperProxyFactory.java`

### 4.4 SQL 解析和执行

- `src/main/java/com/zimu/mybatis/mapping/MappedStatement.java`
- `src/main/java/com/zimu/mybatis/mapping/BoundSql.java`
- `src/main/java/com/zimu/mybatis/mapping/SqlCommandType.java`
- `src/main/java/com/zimu/mybatis/util/GenericTokenParser.java`
- `src/main/java/com/zimu/mybatis/util/ConnectionFactory.java`
- `src/main/java/com/zimu/mybatis/executor/SimpleExecutor.java`

### 4.5 参数和结果映射

- `src/main/java/com/zimu/mybatis/reflection/ParameterResolver.java`
- `src/main/java/com/zimu/mybatis/reflection/ResultSetMapper.java`

### 4.6 示例业务

- `src/main/java/com/zimu/demo/mybatis/entity/User.java`
- `src/main/java/com/zimu/demo/mybatis/mapper/UserMapper.java`
- `src/main/resources/com/zimu/demo/mybatis/mapper/UserMapper.xml`
- `src/main/java/com/zimu/demo/mybatis/support/DatabaseInitializer.java`
- `src/main/java/com/zimu/demo/mybatis/support/PlaceholderBindingDemo.java`

---

## 5. `#{username}`、`#{password}` 这类占位符是怎么回事

这个问题也专门展开讲过。

核心过程可以记成 3 步：

### 第 1 步：先把 `#{...}` 变成 `?`

比如：

```sql
insert into t_user(username, password, age)
values (#{username}, #{password}, #{age})
```

会先被解析成：

```sql
insert into t_user(username, password, age)
values (?, ?, ?)
```

同时还会记住参数顺序：

```text
[username, password, age]
```

### 第 2 步：再去参数对象里按名字取值

比如参数对象是：

```java
User user = new User();
user.setUsername("wangwu");
user.setPassword("pw123");
user.setAge(22);
```

那框架就会按顺序去取：

- `username -> wangwu`
- `password -> pw123`
- `age -> 22`

### 第 3 步：最后绑定到 `PreparedStatement`

最终本质上做的是：

```java
preparedStatement.setObject(1, "wangwu");
preparedStatement.setObject(2, "pw123");
preparedStatement.setObject(3, 22);
```

一句最短理解：

`#{字段名}` 的意思就是：

“稍后请从参数对象里取出这个字段的值，并安全地绑定到 SQL 的问号上。”

---

## 6. `Example` / `Criteria` 这块也专门讲过

后来又继续聊到了：

```java
Example example = new Example(Users.class);
Example.Criteria criteria = example.createCriteria();
```

这个东西的白话理解是：

- `Example` = 整个查询对象
- `Criteria` = 查询对象里的条件对象
- `createCriteria()` = 开始写 where 条件

也就是说它本质上是在做这件事：

“先用 Java 对象收集查询条件，最后再统一翻译成 SQL。”

为了方便理解，还专门写了一个迷你版演示类：

- `src/main/java/com/zimu/demo/mybatis/support/ExampleMiniDemo.java`

根目录也有一份单独文档：

- `mybatis-example.md`

---

## 7. 根目录现在有哪些学习文档

### 7.1 `README.md`

这是 mini Spring 的白话笔记。

内容包括：

- Spring 核心注解
- `@Configuration`、`@Bean`、`@Service`
- 容器启动流程
- 源码对照阅读

### 7.2 `mybatis.md`

这是 mini MyBatis 的白话笔记。

内容包括：

- MyBatis 和 JDBC 的关系
- `SqlSessionFactory`、`SqlSession`
- mapper 代理
- `namespace`
- `id`
- `parameterType`
- `resultType`
- 为什么 `resources` 放 mapper XML
- 为什么说约定大于配置
- 为什么 `#{}` 最后会变成 `?`

### 7.3 `mybatis-example.md`

这是专门讲 `Example / Criteria` 的笔记。

内容包括：

- `Example` 到底是什么
- `Criteria` 到底是什么
- 为什么写成 `Example.Criteria`
- 它最后怎么变成 SQL

---

## 8. 现在已经验证通过的内容

目前已经验证过这些都能跑：

1. mini Spring 正常运行
2. mini MyBatis 的 `select` 正常
3. mini MyBatis 的 `insert` 正常
4. 插入之后再查能查回来
5. `PlaceholderBindingDemo` 能清楚打印 `#{}` 的解析过程
6. `ExampleMiniDemo` 能打印对象条件最终拼成的 SQL

---

## 9. 换电脑之后建议怎么继续看

建议按这个顺序继续：

### 第一步：先看文档

1. `README.md`
2. `mybatis.md`
3. `mybatis-example.md`
4. `codex.md`

### 第二步：再看源码

1. `src/main/java/com/zimu/Main.java`
2. `src/main/resources/mybatis-config.xml`
3. `src/main/resources/com/zimu/demo/mybatis/mapper/UserMapper.xml`
4. `src/main/java/com/zimu/demo/mybatis/mapper/UserMapper.java`
5. `src/main/java/com/zimu/mybatis/binding/MapperProxy.java`
6. `src/main/java/com/zimu/mybatis/util/GenericTokenParser.java`
7. `src/main/java/com/zimu/mybatis/reflection/ParameterResolver.java`
8. `src/main/java/com/zimu/mybatis/executor/SimpleExecutor.java`
9. `src/main/java/com/zimu/demo/mybatis/support/PlaceholderBindingDemo.java`
10. `src/main/java/com/zimu/demo/mybatis/support/ExampleMiniDemo.java`

---

## 10. 接下来最适合继续学什么

如果继续沿着这条学习路线走，最值得继续补的是：

1. `#{}` 和 `${}` 的区别
2. `update`
3. `delete`
4. 多参数绑定
5. `resultMap`
6. MyBatis 和 Spring 的整合

---

## 11. 一句话总总结

这个项目现在已经变成了一个“可以边跑边学”的小型实验场。

它已经同时包含：

- mini Spring
- mini MyBatis
- 动态代理
- `#{}` 占位符解析
- `Example / Criteria` 条件拼装思路

后面继续扩展，会非常适合用来真正吃透这些框架的底层思路。

---

## 12. 这次继续补了什么：mini MyBatis 连接池

这次在 mini MyBatis 里补了一条新的学习主线：

“MyBatis 怎么通过数据源和连接池管理数据库连接。”

之前的版本是：

```text
每执行一次 SQL -> DriverManager.getConnection() 新建连接 -> SQL 执行完真的 close
```

现在改成：

```text
每执行一次 SQL -> 从 DataSource 获取连接 -> SQL 执行完 close -> 如果是池化连接，就归还给连接池
```

### 12.1 新增的核心类

新增了这些文件：

- `src/main/java/com/zimu/mybatis/datasource/MiniDataSource.java`
- `src/main/java/com/zimu/mybatis/datasource/UnpooledDataSource.java`
- `src/main/java/com/zimu/mybatis/datasource/PooledDataSource.java`
- `src/main/java/com/zimu/mybatis/datasource/PooledConnection.java`
- `src/main/java/com/zimu/demo/mybatis/support/ConnectionPoolDemo.java`

### 12.2 每个类的作用

`MiniDataSource`：

mini MyBatis 自己定义的极简数据源接口，只负责一件事：

```java
Connection getConnection();
```

`UnpooledDataSource`：

非池化数据源，每次都通过 `DriverManager` 新建真实物理连接。

`PooledDataSource`：

池化数据源，内部维护两个列表：

- `idleConnections`：空闲连接
- `activeConnections`：正在使用的连接

借连接时：

1. 先看有没有空闲连接
2. 没有空闲连接，再判断是否还能创建新连接
3. 达到最大活跃连接数，就报错

还连接时：

1. 从活跃列表移除
2. 如果空闲列表没满，就放入空闲列表
3. 如果空闲列表满了，就真的关闭物理连接

`PooledConnection`：

用 JDK 动态代理包装真实 `Connection`。

它最关键的逻辑是拦截 `close()`：

```text
普通 JDBC close = 真的关闭物理连接
连接池 close = 把连接归还给池子
```

### 12.3 配置文件新增了什么

`src/main/resources/mybatis-config.xml` 里新增：

```xml
<property name="poolEnabled" value="true"/>
<property name="poolMaximumActiveConnections" value="3"/>
<property name="poolMaximumIdleConnections" value="2"/>
```

### 12.4 原来的 SQL 执行代码怎么变了

原来 `SimpleExecutor` 是直接创建连接：

```java
ConnectionFactory.createConnection(configuration)
```

现在改成从数据源拿连接：

```java
ConnectionFactory.getConnection(configuration)
```

这样 `SimpleExecutor` 不需要关心背后是：

- 每次新建连接
- 还是从连接池复用连接

这更接近真正 MyBatis 的结构。

### 12.5 怎么观察连接池效果

可以运行：

```bash
mvn exec:java -Dexec.mainClass=com.zimu.demo.mybatis.support.ConnectionPoolDemo
```

会看到类似输出：

```text
借出第 1 个连接 -> active=1, idle=0
借出第 2 个连接 -> active=2, idle=0
归还第 1 个连接 -> active=1, idle=1
再次借出连接 -> active=2, idle=0
全部归还连接 -> active=0, idle=2
```

这说明连接没有被每次真正关闭，而是在空闲池里等待复用。

### 12.6 和 MySQL 的关系

当前项目默认还是使用 H2 内存数据库的 MySQL 模式，方便不用安装 MySQL 也能跑。

但连接池逻辑和具体数据库无关。

以后如果要真的连接 MySQL，只需要：

1. `pom.xml` 加 MySQL JDBC 驱动
2. `mybatis-config.xml` 改成 MySQL 的 driver、url、username、password

连接池这套核心逻辑仍然可以继续复用。
