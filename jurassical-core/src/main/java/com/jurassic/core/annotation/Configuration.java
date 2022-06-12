package com.jurassic.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记class为配置定义
 * 该类的各个方法产生用于系统配置的特定对象
 *
 * @author yzhu
 */
@Target(value= {ElementType.TYPE})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Configuration {
}
