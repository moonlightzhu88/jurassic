package com.jurassic.core.annotation;

import java.lang.annotation.*;

/**
 * �������
 * ��ǵ�class��singleton����ʽע����DeployContext��
 *
 * @author yzhu
 */
@Target(value= {ElementType.TYPE})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Component {
    // ���������
    String name() default "";
}
