package com.jurassic.core.dataflow.filter;

import com.jurassic.core.event.Event;

/**
 * �¼�������
 * ���Զ�������������Handler���¼����й��˴���
 * �Ӷ��ı��¼�����ת����
 *
 * @author yzhu
 */
public interface Filter {

    /**
     * ���¼����й���
     * �������������null�����ʾ���¼�event
     * ���������ڣ���������Ĵ���
     */
    Event filter(Event event);

    /**
     * ����filterӦ�õ�Handler
     */
    String getHandlerKey();

    /**
     * filter�Ƿ����Handler֮ǰ
     */
    boolean isBefore();
}
