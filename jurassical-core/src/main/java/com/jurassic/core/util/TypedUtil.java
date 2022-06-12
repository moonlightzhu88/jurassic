package com.jurassic.core.util;

/**
 * 查找通用Type类型的工具类
 *
 * @author yzhu
 */
public abstract class TypedUtil<T> {

    /**
     * 获得泛型的实际类型
     */
    public abstract String getActualTypeName();
}
