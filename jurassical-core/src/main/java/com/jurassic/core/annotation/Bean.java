package com.jurassic.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ��Ƿ����ķ��ض���ʹ��singleton����ע��
 *
 * @author yzhu
 */
@Target(value= {ElementType.METHOD})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Bean {
    String name() default "";
}
