package com.zimu.mybatis.plugin.page;

// 导入绑定后的 SQL。
import com.zimu.mybatis.mapping.BoundSql;

// 导入列表工具。
import java.util.List;

// 这是一个教学版 MySQL 分页拦截器。
// 真正的 PageHelper 会接入 MyBatis 插件机制；我们这里直接在执行器里调用它，方便看清楚 SQL 如何变化。
public class MySqlPageInterceptor {

    // 根据当前线程里的分页参数，决定是否改写 SQL。
    public BoundSql intercept(BoundSql boundSql) {
        // 从 PageHelper 拿当前线程保存的分页参数。
        Page page = PageHelper.getLocalPage();

        // 如果没有调用 PageHelper.startPage，就不做任何处理。
        if (page == null) {
            return boundSql;
        }

        // MySQL 分页语法是 limit offset, pageSize。
        //
        // 这里故意使用 limit ?, ?，而不是直接拼数字。
        // 这样可以继续走 PreparedStatement 参数绑定，和普通 #{...} 参数保持同一套安全模型。
        return boundSql.appendSqlAndParameters(" limit ?, ?", List.of(page.getOffset(), page.getPageSize()));
    }
}
