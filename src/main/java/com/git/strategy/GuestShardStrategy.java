package com.git.strategy;

import com.git.dao.pojo.Guest;
import com.git.threadlocal.ShardTableIdThreadLocal;
import com.google.code.shardbatis.strategy.ShardStrategy;
import org.apache.commons.lang3.StringUtils;

public class GuestShardStrategy implements ShardStrategy {
    /**
     * 方法说明:
     *
     * @param tableName 逻辑表名
     * @param param     mybatis执行某个statement时使用的参数
     * @param mapperId  mybatis执行的某个statement的id
     */
    @Override
    public String getTargetTableName(String tableName, Object param, String mapperId) {
        if (ShardTableIdThreadLocal.integerThreadLocal.get() != null) {
            return getTable(tableName, ShardTableIdThreadLocal.integerThreadLocal.get());
        }


        String cardId = getCardId(param);
//        if (StringUtils.isBlank(cardId)) {
//            throw new RuntimeException("查询数据没有cardId");
//        }

        int suffix = getSuffix(cardId);
        return getTable(tableName, suffix);
    }

    private String getTable(String tableName, Integer shardTableId) {
        return tableName + "_" + shardTableId;
    }

    private String getCardId(Object param) {
        if (param == null) {
            return "";
        }
        if (param instanceof Guest) {
            Guest guest = (Guest) param;
            return StringUtils.defaultString(guest.getCardNo(), "");
        }
        return "";
    }

    public static int getSuffix(String cardId) {
        if (StringUtils.isEmpty(cardId)) {
            // 如果没有hotel, 就在第一张表
            return 0;
        }
        // 会得到0-99的数
        return Math.abs(cardId.hashCode() & 99);
    }
}