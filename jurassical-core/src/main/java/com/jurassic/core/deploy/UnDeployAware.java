package com.jurassic.core.deploy;

/**
 * ��֪�����ж��
 *
 * @author yzhu
 */
public interface UnDeployAware {

    /**
     * �����ж��
     */
    void shutdown(DeployContext context);
}
