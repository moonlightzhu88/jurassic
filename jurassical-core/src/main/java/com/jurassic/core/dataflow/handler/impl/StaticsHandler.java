package com.jurassic.core.dataflow.handler.impl;

import com.jurassic.core.bus.impl.EBus;
import com.jurassic.core.dataflow.handler.BatchEventHandler;
import com.jurassic.core.event.Event;
import com.jurassic.core.time.TimeStaticsInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * �¼�ִ����Ϣͳ�ƴ�����
 *
 * @author yzhu
 */
public class StaticsHandler extends BatchEventHandler<Event> {

    private static final Logger logger = LoggerFactory.getLogger(StaticsHandler.class);

    // ����������ִ�������ͳ����Ϣ
    private final Map<String, TimeStaticsInfo> _statics = new HashMap<>();
    private long _printTime = 0;// ��¼�ϴεĴ�ӡ���ʱ��

    public StaticsHandler() {
        super();
    }

    public String getHandlerKey() {
        return EBus.COMPONENT_KEY_STATICS;
    }

    public Event createEvent(Object[] params) {
        return null;
    }

    /**
     * ��ӡ������ִ��ͳ����Ϣ
     */
    public void printStatics() {
        StringBuilder buf = new StringBuilder();
        buf.append("\nEventHandler\tFinish\tErr\tWaitTime(ms)\tExeTime(ms)\n");
        for (Map.Entry<String, TimeStaticsInfo> e : this._statics.entrySet()) {
            String key = e.getKey();
            TimeStaticsInfo time = e.getValue();
            buf.append(key).append("\t");
            buf.append(time.getFinishNum()).append("\t");
            buf.append(time.getErrNum()).append("\t");
            buf.append(time.getAvgWaitingTime()).append("\t");
            buf.append(time.getAvgExecTime()).append("\n");
            // �����ÿ��handler��ִ����Ϣ�󣬽���Щ��Ϣ����
            time.reset();
        }
        if (logger.isDebugEnabled())
            logger.debug(buf.toString());
    }

    public void handle(List<Event> events) throws Throwable {
        // �������¼�����key���з���
        Map<String, List<Event>> eventGroup = new HashMap<>();
        // ͳ��ÿ���¼���ִ����Ϣ
        for (Event event : events) {
            String eventKey = event.getEventKey();
            List<Event> list =
                    eventGroup.computeIfAbsent(eventKey, k -> new ArrayList<>());
            list.add(event);
        }
        for (String eventKey : eventGroup.keySet()) {
            TimeStaticsInfo statics = this._statics.get(eventKey);
            if (statics == null) {
                statics = new TimeStaticsInfo();
                this._statics.put(eventKey, statics);
            }
            // �������¼���ִ����Ϣ��ӽ�ͳ����Ϣ��
            statics.finishEvent(eventGroup.get(eventKey));
        }
        long time = System.currentTimeMillis();
        if (time > this._printTime + 10000) {
            // �����ϴ���Ϣ�������10s�������һ��ͳ����Ϣ
            this.printStatics();
            this._printTime = time;
        }
    }
}
