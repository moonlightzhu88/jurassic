package com.jurassic.core.dataflow.handler;

import com.jurassic.core.dataflow.filter.Filter;
import com.jurassic.core.event.Event;
import com.jurassic.core.handler.AbstractHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * 事件处理器的基类
 *
 * @author yzhu
 * @param <T>
 */
public abstract class BaseEventHandler<T extends Event> extends AbstractHandler {

    protected List<Filter> _beforeFilters;// handler处理之前的过滤器
    protected List<Filter> _afterFilters;// handler处理之后的过滤器
    protected ThreadLocal<List<Event>> _outputEvents = new ThreadLocal<>();// 处理器的输出事件

    public BaseEventHandler() {
        super();
    }

    /**
     * 添加处理器下一步需要处理的事件（处理器的输出事件）
     */
    public void addOutputEvent(Event event) {
        List<Event> outputs = this._outputEvents.get();
        if (outputs == null) {
            outputs = new ArrayList<>();
            this._outputEvents.set(outputs);
        }
        outputs.add(event);
    }

    /**
     * 获得处理器的输出事件
     */
    public List<Event> getOutputEvents() {
        return this._outputEvents.get();
    }

    /**
     * 清空处理器的输出事件
     * 通常在处理器处理完一次事件后清空
     */
    public void clearOutputEvents() {
        List<Event> events = this._outputEvents.get();
        if (events != null)
            events.clear();
    }

    /**
     * 添加在处理器前端执行的Filter
     */
    public void addBeforeFilter(Filter filter) {
        if (this._beforeFilters == null)
            this._beforeFilters = new ArrayList<>();
        this._beforeFilters.add(filter);
    }

    /**
     * 添加在处理器后端执行的Filter
     */
    public void addAfterFilter(Filter filter) {
        if (this._afterFilters == null)
            this._afterFilters = new ArrayList<>();
        this._afterFilters.add(filter);
    }

    /**
     * 判断是否使用了before Filter
     */
    public boolean hasBeforeFilters() {
        return this._beforeFilters != null && !this._beforeFilters.isEmpty();
    }

    /**
     * 判断是否使用了after Filter
     */
    public boolean hasAfterFilters() {
        return this._afterFilters != null && !this._afterFilters.isEmpty();
    }

    /**
     * handler处理器的过滤操作
     * 对输入事件进行过滤
     */
    public Event beforeHandler(Event event) {
        Event tmp = event;
        for (Filter filter : this._beforeFilters) {
            tmp = filter.filter(tmp);
            if (tmp == null)// 如果过滤器将某个事件过滤掉则后续不做处理
                return null;
        }
        return tmp;
    }

    /**
     * handler处理器的过滤操作
     * 对输出事件进行过滤
     */
    public Event afterHandler(Event event) {
        Event tmp = event;
        for (Filter filter : this._afterFilters) {
            tmp = filter.filter(tmp);
            if (tmp == null)// 如果过滤器将某个事件过滤掉则后续不做处理
                return null;
        }
        return tmp;
    }

    /**
     * 创建处理器对应的事件
     * 用于一些对外发布的事件过程
     */
    public abstract Event createEvent(Object[] params);
}
