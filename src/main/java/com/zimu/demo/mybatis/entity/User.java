package com.zimu.demo.mybatis.entity;

// 这个类表示数据库里的用户表记录。
public class User {

    // 对应数据库里的 id 列。
    private Long id;

    // 对应数据库里的 username 列。
    private String username;

    // 对应数据库里的 age 列。
    private Integer age;

    // 无参构造器给反射创建对象使用。
    public User() {
    }

    // 返回 id。
    public Long getId() {
        return id;
    }

    // 设置 id。
    public void setId(Long id) {
        this.id = id;
    }

    // 返回用户名。
    public String getUsername() {
        return username;
    }

    // 设置用户名。
    public void setUsername(String username) {
        this.username = username;
    }

    // 返回年龄。
    public Integer getAge() {
        return age;
    }

    // 设置年龄。
    public void setAge(Integer age) {
        this.age = age;
    }

    // 重写 toString，方便打印结果。
    @Override
    public String toString() {
        return "User{id=" + id + ", username='" + username + "', age=" + age + "}";
    }
}
