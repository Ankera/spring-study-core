# PageHelper 和 MySQL 分页白话笔记

这份文档专门讲这句代码背后的事情：

```java
PageHelper.startPage(dto.getPage(), dto.getPageSize());
```

这次项目里没有直接引入真正的 PageHelper。

我加的是一个“教学版 PageHelper”，目的不是追求完整功能，而是把最核心的源码思路拆开：

- `startPage()` 先保存分页参数
- mapper 查询照常执行
- 查询真正执行前，拦截器把 SQL 改成 MySQL 的 `limit ?, ?`
- 最后通过 `PreparedStatement` 把普通参数和分页参数一起绑定进去

---

## 1. 先记一句话

`PageHelper.startPage(page, pageSize)` 本身不查数据库。

它只是提前告诉 MyBatis：

“下一条 select 查询要分页。”

真正和 MySQL 结合的地方，是后面的 SQL 被改写成了：

```sql
select ...
from ...
order by ...
limit ?, ?
```

然后把两个问号绑定成：

```text
offset = (page - 1) * pageSize
pageSize = pageSize
```

比如：

```java
PageHelper.startPage(2, 2);
```

会变成：

```text
offset = 2
pageSize = 2
```

对应 MySQL：

```sql
limit 2, 2
```

意思是：

“跳过前 2 条，再取 2 条。”

---

## 2. 这次新增了哪些源码

### 2.1 教学版 PageHelper

新增文件：

```text
src/main/java/com/zimu/mybatis/plugin/page/PageHelper.java
src/main/java/com/zimu/mybatis/plugin/page/Page.java
src/main/java/com/zimu/mybatis/plugin/page/MySqlPageInterceptor.java
```

`PageHelper` 负责保存分页参数：

```java
PageHelper.startPage(2, 2);
```

核心代码是：

```java
private static final ThreadLocal<Page> LOCAL_PAGE = new ThreadLocal<>();
```

为什么用 `ThreadLocal`？

因为 Web 项目里每个请求通常由一个线程处理。

把分页参数放在线程里，就能做到：

- 当前请求设置当前请求的分页
- 其他请求不受影响
- mapper 方法不用多传 `page`、`pageSize`

### 2.2 MySQL 分页拦截器

新增文件：

```text
src/main/java/com/zimu/mybatis/plugin/page/MySqlPageInterceptor.java
```

它负责把原 SQL 改写成 MySQL 分页 SQL。

关键逻辑是：

```java
return boundSql.appendSqlAndParameters(
        " limit ?, ?",
        List.of(page.getOffset(), page.getPageSize())
);
```

比如原 SQL 是：

```sql
select id, username, password, age
from t_user
order by id
```

分页后变成：

```sql
select id, username, password, age
from t_user
order by id
limit ?, ?
```

这里故意保留 `?`，不是直接拼成 `limit 2, 2`。

原因是它可以继续走 JDBC 的 `PreparedStatement` 参数绑定。

### 2.3 BoundSql 支持追加参数

修改文件：

```text
src/main/java/com/zimu/mybatis/mapping/BoundSql.java
```

原来的 `BoundSql` 只保存两样东西：

- JDBC SQL
- `#{...}` 解析出来的参数名列表

这次又加了：

```java
private final List<Object> additionalParameterValues;
```

它用来保存 PageHelper 追加出来的固定参数：

```text
[offset, pageSize]
```

这样普通业务参数和分页参数就可以统一绑定到 `PreparedStatement`。

---

## 3. 完整调用链

以这段代码为例：

```java
PageHelper.startPage(2, 2);
List<User> users = userMapper.selectAll();
```

完整过程是：

```text
PageHelper.startPage(2, 2)
        |
        v
ThreadLocal 保存 Page(page=2, pageSize=2)
        |
        v
userMapper.selectAll()
        |
        v
MapperProxy.invoke()
        |
        v
SqlSession.selectList()
        |
        v
SimpleExecutor.queryList()
        |
        v
GenericTokenParser 处理 #{...}
        |
        v
MySqlPageInterceptor 追加 limit ?, ?
        |
        v
PreparedStatement 绑定参数
        |
        v
MySQL 执行分页 SQL
```

这里最关键的是：

`startPage()` 必须写在 mapper 查询前面。

因为它影响的是“紧跟着的下一次 select 查询”。

---

## 4. 这次项目里的演示代码

新增 demo：

```text
src/main/java/com/zimu/demo/mybatis/support/PageHelperMiniDemo.java
```

可以直接运行它。

核心代码：

```java
PageHelper.startPage(2, 2);
List<User> pageUsers = userMapper.selectAll();
```

`UserMapper` 里新增：

```java
List<User> selectAll();
```

`UserMapper.xml` 里新增：

```xml
<select id="selectAll" resultType="com.zimu.demo.mybatis.entity.User">
    select id, username, password, age
    from t_user
    order by id
</select>
```

注意 XML 里没有写 `limit`。

`limit` 是 PageHelper 在执行前动态加上的。

---

## 5. 为什么 PageHelper 后面不需要手动传参数

平时你可能会写：

```java
PageHelper.startPage(dto.getPage(), dto.getPageSize());
List<User> users = userMapper.selectAll();
```

你会发现：

`selectAll()` 方法没有接收 `page` 和 `pageSize`。

这是因为分页参数不是通过 mapper 方法参数传进去的。

它是通过 `ThreadLocal` 暂存在当前线程里的。

所以 mapper 查询执行到 SQL 层时，分页拦截器还能从当前线程里取到这两个值。

---

## 6. 和 MySQL 的真正结合点

PageHelper 和 MySQL 结合，不是靠什么神秘语法。

核心就是 MySQL 本身支持分页：

```sql
limit offset, pageSize
```

PageHelper 做的是：

1. 拿到你原来的 select SQL
2. 计算 offset
3. 在 SQL 后面追加 `limit ?, ?`
4. 把 offset 和 pageSize 作为 JDBC 参数绑定进去
5. 交给 MySQL 执行

所以 MyBatis / PageHelper / MySQL 的关系可以这样理解：

```text
MyBatis 负责找到 SQL、绑定参数、执行 JDBC
PageHelper 负责在执行前改写分页 SQL
MySQL 负责真正执行 limit 分页
```

---

## 7. 本项目和真实 PageHelper 的区别

这个项目里的实现是教学版。

它只保留了核心流程：

- `startPage`
- `ThreadLocal`
- 拦截查询
- 追加 MySQL `limit`
- 绑定分页参数
- 查询后清理分页参数

真正的 PageHelper 还会处理很多复杂场景，比如：

- 自动 count 总数
- 返回 PageInfo
- 支持不同数据库方言
- 处理复杂 SQL
- 接入 MyBatis 官方插件机制
- 处理 RowBounds
- 处理更多边界情况

但你先把这个教学版看懂，真正 PageHelper 的源码就不会那么吓人了。
