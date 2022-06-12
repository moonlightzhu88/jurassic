package com.jurassic.core.dataflow.filter;

import com.jurassic.core.event.Event;

/**
 * 事件过滤器
 * 可以对流进或者流出Handler的事件进行过滤处理
 * 从而改变事件的流转方向
 *
 * @author yzhu
 */
public interface Filter {

    /**
     * 对事件进行过滤
     * 如果过滤器返回null，则表示该事件event
     * 将不复存在，无需后续的处理
     */
    Event filter(Event event);

    /**
     * 返回filter应用的Handler
     */
    String getHandlerKey();

    /**
     * filter是否放在Handler之前
     */
    boolean isBefore();
}
