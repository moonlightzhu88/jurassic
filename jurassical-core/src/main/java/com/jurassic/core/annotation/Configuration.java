package com.jurassic.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ���classΪ���ö���
 * ����ĸ���������������ϵͳ���õ��ض�����
 *
 * @author yzhu
 */
@Target(value= {ElementType.TYPE})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Configuration {
}
