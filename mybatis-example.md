# mybatis Example 白话笔记

这份文档专门讲一个容易把人看懵的东西：

```java
Example example = new Example(Users.class);
Example.Criteria criteria = example.createCriteria();
```

很多人第一次看到它，都会有几个疑问：

- `Example` 到底是什么
- `Criteria` 又是什么
- 为什么要 `Example.Criteria`
- 为什么不直接写 SQL
- 它最后到底怎么变成 SQL

这份文档就是专门解决这些问题的。

---

## 1. 先用一句人话理解

如果直接说术语，很容易绕晕。

所以先用最白话的话讲：

`Example` 本质上就是一张“查询说明书”。

`Criteria` 本质上就是这张说明书里的“条件部分”。

所以这段代码：

```java
Example example = new Example(Users.class);
Example.Criteria criteria = example.createCriteria();
```

白话翻译就是：

1. 我准备查 `Users` 这张表
2. 我先拿一张查询说明书
3. 然后我再开始往说明书里写查询条件

---

## 2. 为什么看起来这么绕

因为它不是直接写 SQL。

它是在做另一件事：

“先用 Java 对象把查询条件收集起来，最后再统一翻译成 SQL。”

所以你看到的不是：

```sql
select * from users where name = '张三'
```

而是：

```java
criteria.andEqualTo("name", "张三");
```

也就是说：

- 你写的是 Java 条件对象
- 框架负责把它翻译成 SQL

这就是它最核心的思想。

---

## 3. `Example` 到底是什么

你可以把 `Example` 理解成“整张查询单”。

比如你准备查用户表：

```java
Example example = new Example(Users.class);
```

这句话不是在查数据库。

它只是先创建了一个对象，这个对象里面会记住：

- 当前查的是哪个实体类
- 之后会往里面添加哪些条件
- 最终这些条件要如何拼成 SQL

所以 `Example` 更像一个“容器”。

它装的是整次查询的信息。

---

## 4. `Criteria` 到底是什么

`Criteria` 是 `Example` 里面的条件区域。

你可以把它理解成：

“专门用来收集 where 条件的对象。”

比如：

```java
Example.Criteria criteria = example.createCriteria();
criteria.andEqualTo("name", "张三");
criteria.andGreaterThan("age", 18);
```

白话就是：

- name = 张三
- age > 18

最后这些条件会被拼成：

```sql
where name = '张三' and age > 18
```

所以你可以记住：

- `Example` 管整张查询
- `Criteria` 管 where 条件

---

## 5. 为什么要写成 `Example.Criteria`

因为 `Criteria` 往往不是一个独立类。

它通常是 `Example` 里面定义的内部类。

就像这样：

```java
class Example {
    static class Criteria {
    }
}
```

既然它是 `Example` 里面的类，那用的时候就要写：

```java
Example.Criteria
```

这和你平时见到的：

```java
Map.Entry
```

是一个道理。

所以这个写法本身不用怕。

它不是特殊语法，它只是“外部类.内部类”。

---

## 6. 它到底是怎么一步步工作的

可以把整个过程拆成 4 步。

### 第 1 步：创建查询对象

```java
Example example = new Example(Users.class);
```

这一步的作用是：

告诉程序：

“我要查的是 `Users` 对应的表。”

它此时还没有 SQL。

只是创建了一个查询容器。

### 第 2 步：创建条件对象

```java
Example.Criteria criteria = example.createCriteria();
```

这一步的作用是：

告诉程序：

“现在我要开始写 where 条件了。”

### 第 3 步：不断追加条件

```java
criteria.andEqualTo("name", "张三");
criteria.andGreaterThan("age", 18);
criteria.andLike("address", "%上海%");
```

这一步相当于不停往条件区域里塞内容：

- `name = '张三'`
- `age > 18`
- `address like '%上海%'`

### 第 4 步：最后统一转成 SQL

框架会把前面收集的对象信息，统一翻译成 SQL。

比如：

```sql
select * from users
where name = '张三'
  and age > 18
  and address like '%上海%'
```

---

## 7. 我给你写的迷你版是怎么实现的

我在项目里写了一个最小可读版：

`src/main/java/com/zimu/demo/mybatis/support/ExampleMiniDemo.java`

它不是完整框架，只是把 `Example / Criteria` 的核心思想拆开给你看。

### 7.1 `Example`

这里的 `Example` 负责保存整次查询的信息：

- 查哪个实体类
- 当前有哪些条件

关键代码在：

- `Example` 类定义
- `createCriteria()`
- `toSql()`

### 7.2 `Criteria`

这里的 `Criteria` 负责保存条件项。

它提供了这些方法：

- `andEqualTo()`
- `andGreaterThan()`
- `andLike()`

这些方法本质上都是：

“记录一条条件到列表里。”

### 7.3 `Condition`

我还拆了一个最小的 `Condition` 类。

它只做一件事：

保存一条条件的三部分信息：

- 字段名
- 操作符
- 值

比如：

```java
new Condition("name", "=", "张三")
```

就表示：

```sql
name = '张三'
```

---

## 8. 最关键的思维转换

你以后看到这种代码，不要第一反应去想：

“这怎么不像 SQL？”

你要换成这个思路：

“哦，它是在先收集条件对象，最后再翻译成 SQL。”

也就是说：

- 不是直接写 SQL
- 而是先写查询条件对象

这个思维一旦切过来，就不容易晕了。

---

## 9. 用一句最短的话记住

你可以直接背这个：

- `Example` = 整个查询对象
- `Criteria` = 查询对象里的条件对象
- `createCriteria()` = 开始写 where 条件
- `andEqualTo()` 这些方法 = 往 where 里追加条件

---

## 10. 对照代码怎么读

建议你按这个顺序看：

1. 先看 `ExampleMiniDemo.main()`
2. 再看 `Example` 的 `createCriteria()`
3. 再看 `Criteria.andEqualTo()` 这些方法
4. 最后看 `Example.toSql()`

这样你会很容易看出来：

“原来不是魔法，只是先存条件，再拼 SQL。”

---

## 11. 一个完整的白话例子

这段代码：

```java
Example example = new Example(Users.class);
Example.Criteria criteria = example.createCriteria();
criteria.andEqualTo("name", "张三")
        .andGreaterThan("age", 18)
        .andLike("address", "%上海%");
```

你脑子里应该自动翻译成：

“我要查 users 表，
条件是 name = 张三，
并且 age > 18，
并且 address like '%上海%'。”

最后大概就是：

```sql
select * from users where name = '张三' and age > 18 and address like '%上海%'
```

---

## 12. 一句话收尾

`Example / Criteria` 并不神秘。

它本质上就是：

“用 Java 对象来描述 SQL 条件，再由框架帮你拼成真正的 SQL。”
