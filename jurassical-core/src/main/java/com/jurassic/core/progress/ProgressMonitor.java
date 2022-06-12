package com.jurassic.core.progress;

/**
 * 流程执行的监控接口
 *
 * @author yzhu
 */
public interface ProgressMonitor {

    /**
     * 流程启动
     */
    void serviceStart(Progress progress);

    /**
     * 流程结束
     */
    void serviceEnd(Progress progress);
}
