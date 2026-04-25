package com.zimu.demo.mybatis.support;

// 导入数组列表。
import java.util.ArrayList;
// 导入列表接口。
import java.util.List;

// 这个类是一个“超迷你版 Example 演示”。
// 目的不是完整复刻 tk.mybatis，而是帮你看懂它背后的思想。
public class ExampleMiniDemo {

    // 用一个简单实体类占位，模拟真实项目里的 Users.class。
    static class Users {
    }

    // 这个类就像 tk.mybatis 里的 Example。
    // 它代表“整张查询说明书”。
    static class Example {

        // 保存当前这个查询是针对哪个实体类的。
        private final Class<?> entityClass;

        // 保存当前查询里的条件对象。
        private final List<Criteria> criteriaList = new ArrayList<>();

        // 构造器里传入实体类型。
        public Example(Class<?> entityClass) {
            // 保存实体类型。
            this.entityClass = entityClass;
        }

        // 创建一组查询条件。
        public Criteria createCriteria() {
            // 创建新的条件对象。
            Criteria criteria = new Criteria();

            // 把它放进 Example 里保存起来。
            criteriaList.add(criteria);

            // 返回给外面继续追加条件。
            return criteria;
        }

        // 把当前 Example 翻译成一条最简单的 SQL。
        public String toSql() {
            // 先根据实体类名简单推导出表名。
            String tableName = entityClass.getSimpleName().toLowerCase();

            // 先拼 select 开头。
            StringBuilder sqlBuilder = new StringBuilder("select * from " + tableName);

            // 如果根本没有条件，就直接返回整表查询 SQL。
            if (criteriaList.isEmpty()) {
                return sqlBuilder.toString();
            }

            // 当前示例只演示第一组条件。
            Criteria criteria = criteriaList.get(0);

            // 如果这组条件里没有任何条件项，也直接返回整表查询 SQL。
            if (criteria.conditions.isEmpty()) {
                return sqlBuilder.toString();
            }

            // 开始拼 where。
            sqlBuilder.append(" where ");

            // 逐个把条件拼进去。
            for (int index = 0; index < criteria.conditions.size(); index++) {
                // 取出当前条件。
                Condition condition = criteria.conditions.get(index);

                // 不是第一个条件时，前面加 and。
                if (index > 0) {
                    sqlBuilder.append(" and ");
                }

                // 拼字段名。
                sqlBuilder.append(condition.fieldName).append(" ");

                // 拼操作符。
                sqlBuilder.append(condition.operator).append(" ");

                // 拼值。
                sqlBuilder.append(formatValue(condition.value));
            }

            // 返回最终 SQL。
            return sqlBuilder.toString();
        }

        // 把不同类型的值转成适合 SQL 展示的字符串。
        private String formatValue(Object value) {
            // 如果是字符串，就补上单引号。
            if (value instanceof String) {
                return "'" + value + "'";
            }

            // 其他类型先直接转字符串。
            return String.valueOf(value);
        }

        // 这个内部类就像 tk.mybatis 里的 Example.Criteria。
        // 它代表“where 条件区域”。
        static class Criteria {

            // 保存所有条件项。
            private final List<Condition> conditions = new ArrayList<>();

            // 添加等于条件。
            public Criteria andEqualTo(String fieldName, Object value) {
                // 记录一条 field = value 条件。
                conditions.add(new Condition(fieldName, "=", value));

                // 返回自己，方便链式调用。
                return this;
            }

            // 添加大于条件。
            public Criteria andGreaterThan(String fieldName, Object value) {
                // 记录一条 field > value 条件。
                conditions.add(new Condition(fieldName, ">", value));

                // 返回自己，方便链式调用。
                return this;
            }

            // 添加 like 条件。
            public Criteria andLike(String fieldName, Object value) {
                // 记录一条 field like value 条件。
                conditions.add(new Condition(fieldName, "like", value));

                // 返回自己，方便链式调用。
                return this;
            }
        }
    }

    // 这个类表示一条最小条件项。
    static class Condition {

        // 字段名。
        private final String fieldName;

        // 操作符。
        private final String operator;

        // 条件值。
        private final Object value;

        // 通过构造器初始化三部分信息。
        public Condition(String fieldName, String operator, Object value) {
            this.fieldName = fieldName;
            this.operator = operator;
            this.value = value;
        }
    }

    // main 方法用来直接演示 Example 的工作过程。
    public static void main(String[] args) {
        // 第一步：创建 Example，表示我要查 Users 对应的表。
        Example example = new Example(Users.class);

        // 第二步：创建条件对象。
        Example.Criteria criteria = example.createCriteria();

        // 第三步：不断往条件对象里加 where 条件。
        criteria.andEqualTo("name", "张三")
                .andGreaterThan("age", 18)
                .andLike("address", "%上海%");

        // 第四步：最后由 Example 统一拼出 SQL。
        String sql = example.toSql();

        // 打印最终效果。
        System.out.println(sql);
    }
}
