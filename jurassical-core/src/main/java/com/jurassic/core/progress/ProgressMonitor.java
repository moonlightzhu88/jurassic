package com.jurassic.core.progress;

/**
 * ����ִ�еļ�ؽӿ�
 *
 * @author yzhu
 */
public interface ProgressMonitor {

    /**
     * ��������
     */
    void serviceStart(Progress progress);

    /**
     * ���̽���
     */
    void serviceEnd(Progress progress);
}
