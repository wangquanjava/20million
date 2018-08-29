package com.git.utils;

import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 说明：List工具类
 *
 * @author changhao.shao
 * @date 2016/3/15
 */
public class ListUtil {

    /**
     * 方法描述：返回列表中第一项
     *
     * @param list
     * @return
     * @author changhao.shao
     */
    public static <T> T first(List<T> list) {
        if (CollectionUtils.isNotEmpty(list)) {
            return list.get(0);
        } else {
            return null;
        }
    }

    /**
     * 方法描述：返回列表中最后一项
     *
     * @param list
     * @return
     * @author mbf
     */
    public static <T> T last(List<T> list) {
        if (CollectionUtils.isNotEmpty(list)) {
            return list.get(list.size() - 1);
        } else {
            return null;
        }
    }

    /**
     * 过滤重复元素 排除空元素 保持原有顺序
     *
     * @param list
     * @param <T>
     * @return
     */
    public static <T> List<T> unique(List<T> list) {
        if (CollectionUtils.isEmpty(list)) {
            return list;
        }

        final List<T> resultList = Lists.newArrayList();

        for (T t : list) {
            if (t != null) {
                boolean found = false;
                for (T item : resultList) {
                    if (Objects.equals(t, item)) {
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    resultList.add(t);
                }
            }
        }
        return resultList;
    }

    /**
     * 方法描述：分隔string
     *
     * @param nos
     * @return
     * @author changhao.shao
     */
    public static List<String> splitToList(String nos) {
        if (StringUtils.isBlank(nos)) {
            return Collections.emptyList();
        }

        return Splitter.on(",").trimResults().omitEmptyStrings().splitToList(nos);
    }

    /**
     * 过滤符合条件的数据
     *
     * @param list
     * @param predicate
     * @param <T>
     * @return
     */
    public static <T> List<T> filter(List<T> list, Predicate<T> predicate) {
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }

        List<T> filteredList = Lists.newArrayList();
        for (T obj : list) {
            if (predicate.apply(obj)) {
                filteredList.add(obj);
            }
        }
        return filteredList;
    }
}
