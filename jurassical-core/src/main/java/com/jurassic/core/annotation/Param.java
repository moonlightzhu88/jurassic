package com.jurassic.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ����ע��
 * ��ǲ���ʹ�õ�bean����
 *
 * @author yzhu
 */
@Target(value= {ElementType.PARAMETER})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Param {
    String name();
}
