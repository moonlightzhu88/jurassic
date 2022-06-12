package com.jurassic.core.deploy;

/**
 * 感知组件包发布
 *
 * @author yzhu
 */
public interface DeployAware {

    /**
     * 组件包发布
     */
    void startup(DeployContext context);
}
