package com.jurassic.core.dataflow.worker;

import com.jurassic.core.bus.EventBus;
import com.jurassic.core.bus.impl.EBus;
import com.jurassic.core.dataflow.handler.EventHandler;
import com.jurassic.core.event.Event;
import com.jurassic.core.event.EventWrapper;
import com.jurassic.core.global.GlobalInstRegisterTable;
import com.jurassic.core.handler.AbstractHandler;
import com.jurassic.core.processor.WorkHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * �������Ĺ����߳�
 *
 * @param <T>
 */
public class SingleEventWorker<T extends Event> implements WorkHandler<EventWrapper<T>> {

    private EventHandler<T> _handler;// ������
    private final EventBus _eventBus;// ����

    protected static final Logger logger
            = LoggerFactory.getLogger(SingleEventWorker.class);

    public SingleEventWorker() {
        this._eventBus = GlobalInstRegisterTable.getInst(
                EventBus.GLOBAL_KEY, EventBus.class);
    }

    /**
     * �¼���ɴ���
     * ִ��һЩ�¼�������ɺ����β����
     */
    protected void finish(EventWrapper<T> wrapper, Throwable error) {
        T event = wrapper.getEvent();
        // ����¼������������¼�����쳣���¼�״̬�Լ�ִ����Ϣ��
        event.end(error);
        // ����¼������е�����
        wrapper.reset();
        try {
            // 4��Ĭ�ϵĴ�������packageKeyΪ�գ����ύ����������������������Ĵ���
            this._eventBus.fire(null, EBus.COMPONENT_KEY_STATICS, event);
        } catch (Throwable ex) {
            logger.warn(ex.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void doEvent(EventWrapper<T> wrapper) {
        T event = wrapper.getEvent();
        // �¼���ʼ����
        event.run();

        // ��¼ָ�ʼ����ʱ��
        Throwable err = null;
        Event filterEvent = event;
        if (this._handler.hasBeforeFilters()) {
            // �������¼����й��˴���
            filterEvent = this._handler.beforeHandler(filterEvent);
            if (filterEvent == null) {
                // ����¼���Filter�����ˣ���ֱ�ӽ���
                this.finish(wrapper, null);
                return;
            } else {
                if (!filterEvent.equals(event)) {
                    // ���Filter������¼������˱仯
                    // ����������¼�����ת��������˺���¼�
                    this.finish(wrapper, null);
                    try {
                        this._eventBus.fire(
                                filterEvent.getPackageKey(),
                                filterEvent.getEventKey(),
                                filterEvent);
                    } catch (Throwable ex) {
                        logger.warn(ex.getMessage());
                    }
                    return;
                }
            }
        }
        try {
            this._handler.handle((T)filterEvent);
        } catch (Throwable ex) {
            err = ex.getCause() != null ? ex.getCause() : ex;
            logger.error(ex.getMessage(), ex);
        }
        // ������event����������¼�
        this.finish(wrapper, err);

        // �����¼��������������Ƿ���Ҫ���µ���ִ�и��¼�
        if (event.getScheduleType() == Event.RETRY_ON_ERROR) {
            if (err != null) {
                // ʧ����ִ�У����µ��ȸ��¼�
                this._eventBus.schedule(event);
            }
        } else if (event.getScheduleType() == Event.MULTIPLE) {
            // ��ε��ȣ����ӱ���ִ�н�����
            int retryNum = event.getRetryNum();
            if (retryNum > 0) {
                // ���ȴ�������0�Ŀ����ٴε��ȣ��������
                event.setRetryNum(--retryNum);
                this._eventBus.schedule(event);
            } else if (retryNum == -1) {
                // ���ڴ���Ϊ-1����ʾ��Զ����ִ����ȥ
                this._eventBus.schedule(event);
            }
        }
        if (err != null) {
            // �������������������������������¼���֮ǰ������ʲô��������ᱻ������
            this._handler.clearOutputEvents();
            return;
        }
        // ��������¼�
        List<Event> outputEvents = this._handler.getOutputEvents();
        if (outputEvents != null && !outputEvents.isEmpty()) {
            for (Event outputEvent : outputEvents) {
                try {
                    // ������¼����й���
                    if (this._handler.hasAfterFilters()) {
                        outputEvent = this._handler.afterHandler(outputEvent);
                    }
                    if (outputEvent != null) {
                        this._eventBus.fire(
                                outputEvent.getPackageKey(),
                                outputEvent.getEventKey(),
                                outputEvent);
                    }
                } catch (Throwable ex) {
                    logger.warn(ex.getMessage(), ex);
                }
            }
            this._handler.clearOutputEvents();
        }
    }

    public void onEvent(EventWrapper<T> wrapper) {
        try {
            this.doEvent(wrapper);
        } catch (Throwable ex) {
            logger.warn(ex.getMessage(), ex);
        }
    }

    @SuppressWarnings("unchecked")
    public void setHandler(AbstractHandler handler) {
        this._handler = (EventHandler<T>) handler;
    }
}
