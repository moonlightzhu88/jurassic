package com.jurassic.core.deploy;

/**
 * 感知组件包卸载
 *
 * @author yzhu
 */
public interface UnDeployAware {

    /**
     * 组件包卸载
     */
    void shutdown(DeployContext context);
}
