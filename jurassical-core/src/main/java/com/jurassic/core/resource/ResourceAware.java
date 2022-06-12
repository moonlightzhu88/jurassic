package com.jurassic.core.resource;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 资源注入注解
 * 指定了使用的资源工厂和资源的类型
 *
 * @author yzhu
 */
@Target(value= {ElementType.FIELD})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface ResourceAware {
    // 资源工厂名称
    String resourceFactory();
}
