package com.zimu.mybatis.plugin.page;

// 这个类保存一次分页查询需要的信息。
public class Page {

    // 当前页码，从 1 开始。
    private final int page;

    // 每页条数。
    private final int pageSize;

    // 通过构造器创建分页对象。
    public Page(int page, int pageSize) {
        // 页码不能小于 1。
        if (page < 1) {
            throw new IllegalArgumentException("page 必须从 1 开始");
        }

        // 每页条数必须是正数。
        if (pageSize < 1) {
            throw new IllegalArgumentException("pageSize 必须大于 0");
        }

        this.page = page;
        this.pageSize = pageSize;
    }

    // 返回页码。
    public int getPage() {
        return page;
    }

    // 返回每页条数。
    public int getPageSize() {
        return pageSize;
    }

    // 计算 MySQL limit 的起始位置。
    public int getOffset() {
        return (page - 1) * pageSize;
    }
}
