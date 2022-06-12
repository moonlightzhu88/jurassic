package com.jurassic.core.resource;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ��Դע��ע��
 * ָ����ʹ�õ���Դ��������Դ������
 *
 * @author yzhu
 */
@Target(value= {ElementType.FIELD})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface ResourceAware {
    // ��Դ��������
    String resourceFactory();
}
