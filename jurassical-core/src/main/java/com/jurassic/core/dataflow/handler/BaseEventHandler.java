package com.jurassic.core.dataflow.handler;

import com.jurassic.core.dataflow.filter.Filter;
import com.jurassic.core.event.Event;
import com.jurassic.core.handler.AbstractHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * �¼��������Ļ���
 *
 * @author yzhu
 * @param <T>
 */
public abstract class BaseEventHandler<T extends Event> extends AbstractHandler {

    protected List<Filter> _beforeFilters;// handler����֮ǰ�Ĺ�����
    protected List<Filter> _afterFilters;// handler����֮��Ĺ�����
    protected ThreadLocal<List<Event>> _outputEvents = new ThreadLocal<>();// ������������¼�

    public BaseEventHandler() {
        super();
    }

    /**
     * ��Ӵ�������һ����Ҫ������¼���������������¼���
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
     * ��ô�����������¼�
     */
    public List<Event> getOutputEvents() {
        return this._outputEvents.get();
    }

    /**
     * ��մ�����������¼�
     * ͨ���ڴ�����������һ���¼������
     */
    public void clearOutputEvents() {
        List<Event> events = this._outputEvents.get();
        if (events != null)
            events.clear();
    }

    /**
     * ����ڴ�����ǰ��ִ�е�Filter
     */
    public void addBeforeFilter(Filter filter) {
        if (this._beforeFilters == null)
            this._beforeFilters = new ArrayList<>();
        this._beforeFilters.add(filter);
    }

    /**
     * ����ڴ��������ִ�е�Filter
     */
    public void addAfterFilter(Filter filter) {
        if (this._afterFilters == null)
            this._afterFilters = new ArrayList<>();
        this._afterFilters.add(filter);
    }

    /**
     * �ж��Ƿ�ʹ����before Filter
     */
    public boolean hasBeforeFilters() {
        return this._beforeFilters != null && !this._beforeFilters.isEmpty();
    }

    /**
     * �ж��Ƿ�ʹ����after Filter
     */
    public boolean hasAfterFilters() {
        return this._afterFilters != null && !this._afterFilters.isEmpty();
    }

    /**
     * handler�������Ĺ��˲���
     * �������¼����й���
     */
    public Event beforeHandler(Event event) {
        Event tmp = event;
        for (Filter filter : this._beforeFilters) {
            tmp = filter.filter(tmp);
            if (tmp == null)// �����������ĳ���¼����˵��������������
                return null;
        }
        return tmp;
    }

    /**
     * handler�������Ĺ��˲���
     * ������¼����й���
     */
    public Event afterHandler(Event event) {
        Event tmp = event;
        for (Filter filter : this._afterFilters) {
            tmp = filter.filter(tmp);
            if (tmp == null)// �����������ĳ���¼����˵��������������
                return null;
        }
        return tmp;
    }

    /**
     * ������������Ӧ���¼�
     * ����һЩ���ⷢ�����¼�����
     */
    public abstract Event createEvent(Object[] params);
}
