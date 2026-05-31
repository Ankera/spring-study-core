package com.zimu.mybatis.plugin.page;

// 这是一个教学版 PageHelper。
// 它只保留最核心的思想：startPage 先把分页信息放到当前线程，下一次查询时再取出来改写 SQL。
public class PageHelper {

    // ThreadLocal 可以给每个线程单独保存一份分页参数。
    // 这样 A 请求设置的分页，不会串到 B 请求里。
    private static final ThreadLocal<Page> LOCAL_PAGE = new ThreadLocal<>();

    // 开启分页。
    public static void startPage(int page, int pageSize) {
        // 把分页信息保存到当前线程。
        LOCAL_PAGE.set(new Page(page, pageSize));
    }

    // 取出当前线程的分页信息。
    public static Page getLocalPage() {
        return LOCAL_PAGE.get();
    }

    // 清理当前线程的分页信息。
    public static void clearPage() {
        LOCAL_PAGE.remove();
    }
}
