package com.jurassic.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记方法的返回对象使用singleton进行注册
 *
 * @author yzhu
 */
@Target(value= {ElementType.METHOD})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Bean {
    String name() default "";
}
