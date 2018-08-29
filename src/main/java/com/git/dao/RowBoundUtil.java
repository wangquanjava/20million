package com.git.dao;

import org.apache.ibatis.session.RowBounds;

/**
 * Created by wqquan.wang on 2018/8/30.
 */
public class RowBoundUtil {
    /**
     * 拼接RowBound
     *
     * @param pageNum  页码,从1开始
     * @param pageSize 每页大小, 默认10
     * @return
     */
    public static RowBounds getRowBounds(int pageNum, int pageSize) {
        if (pageNum <= 0) {
            pageNum = 1;
        }
        if (pageSize <= 0) {
            pageSize = 500;
        }
        return new RowBounds((pageNum - 1) * pageSize, pageSize);
    }
    /**
     * 拼接RowBound
     *
     * @param offset  页码,从0开始
     * @param pageSize 每页大小, 默认10
     * @return
     */
    public static RowBounds getRowBoundsByOffset(int offset, int pageSize) {
        if (offset < 0) {
            offset = 0;
        }
        if (pageSize <= 0) {
            pageSize = 500;
        }
        return new RowBounds(offset, pageSize);
    }
}
