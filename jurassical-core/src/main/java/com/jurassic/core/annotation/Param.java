package com.jurassic.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 参数注释
 * 标记参数使用的bean定义
 *
 * @author yzhu
 */
@Target(value= {ElementType.PARAMETER})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Param {
    String name();
}
