package com.jurassic.core.dataflow.handler.impl;

import com.jurassic.core.bus.impl.EBus;
import com.jurassic.core.dataflow.handler.BatchEventHandler;
import com.jurassic.core.event.Event;
import com.jurassic.core.time.TimeStaticsInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 事件执行信息统计处理器
 *
 * @author yzhu
 */
public class StaticsHandler extends BatchEventHandler<Event> {

    private static final Logger logger = LoggerFactory.getLogger(StaticsHandler.class);

    // 各个处理器执行情况的统计信息
    private final Map<String, TimeStaticsInfo> _statics = new HashMap<>();
    private long _printTime = 0;// 记录上次的打印输出时间

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
     * 打印处理器执行统计信息
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
            // 输出完每个handler的执行信息后，将这些信息重置
            time.reset();
        }
        if (logger.isDebugEnabled())
            logger.debug(buf.toString());
    }

    public void handle(List<Event> events) throws Throwable {
        // 将各个事件按照key进行分组
        Map<String, List<Event>> eventGroup = new HashMap<>();
        // 统计每个事件的执行信息
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
            // 将各个事件的执行信息添加进统计信息中
            statics.finishEvent(eventGroup.get(eventKey));
        }
        long time = System.currentTimeMillis();
        if (time > this._printTime + 10000) {
            // 距离上次信息输出超过10s，则输出一次统计信息
            this.printStatics();
            this._printTime = time;
        }
    }
}
