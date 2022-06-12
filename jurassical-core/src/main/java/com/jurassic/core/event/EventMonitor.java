package com.jurassic.core.event;

/**
 * 事件的监控接口
 *
 * @author yzhu
 */
public interface EventMonitor {

    /**
     * 事件的启动
     */
    void eventStart(Event event);

    /**
     * 事件结束
     */
    void eventEnd(Event event);
}
