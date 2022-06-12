package com.jurassic.core.annotation;

import java.lang.annotation.*;

/**
 * 组件定义
 * 标记的class以singleton的形式注册在DeployContext中
 *
 * @author yzhu
 */
@Target(value= {ElementType.TYPE})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Component {
    // 组件的名称
    String name() default "";
}
