package com.jurassic.core.event;

/**
 * �¼��ļ�ؽӿ�
 *
 * @author yzhu
 */
public interface EventMonitor {

    /**
     * �¼�������
     */
    void eventStart(Event event);

    /**
     * �¼�����
     */
    void eventEnd(Event event);
}
