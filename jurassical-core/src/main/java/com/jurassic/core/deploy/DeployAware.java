package com.jurassic.core.deploy;

/**
 * ��֪���������
 *
 * @author yzhu
 */
public interface DeployAware {

    /**
     * ���������
     */
    void startup(DeployContext context);
}
