package com.jurassic.core.dataflow.worker;

import com.jurassic.core.bus.EventBus;
import com.jurassic.core.bus.impl.EBus;
import com.jurassic.core.dataflow.handler.BatchEventHandler;
import com.jurassic.core.event.Event;
import com.jurassic.core.event.EventWrapper;
import com.jurassic.core.global.GlobalInstRegisterTable;
import com.jurassic.core.handler.AbstractHandler;
import com.jurassic.core.processor.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * �����������Ĺ����߳�
 *
 * @param <T>
 */
public class BatchEventWorker<T extends Event> implements EventHandler<EventWrapper<T>> {

    private static final Logger logger
            = LoggerFactory.getLogger(BatchEventWorker.class);

    private BatchEventHandler<T> _handler;// ҵ������
    private final EventBus _eventBus;// ����

    private final List<EventWrapper<T>> _wrapperBuf = new ArrayList<>();// �洢��������������¼���װ��
    private final List<T> _eventBuf = new ArrayList<>();// ����������¼�

    public BatchEventWorker() {
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
        if (!this._handler.getHandlerKey().equals(EBus.COMPONENT_KEY_STATICS)) {
            try {
                // 4��Ĭ�ϵĴ�������packageKeyΪ�գ����ύ����������������������Ĵ���
                this._eventBus.fire(null, EBus.COMPONENT_KEY_STATICS, event);
            } catch (Throwable ex) {
                logger.warn(ex.getMessage());
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void doBatchEvents() {
        Throwable err = null;
        List<T> filterEvents = null;// �洢��Ҫ������¼�
        if (this._handler.hasBeforeFilters()) {
            filterEvents = new ArrayList<>();
            for (T event : this._eventBuf) {
                // ���ζ�ÿһ����Ҫ����������¼�����Filter
                Event filterEvent = this._handler.beforeHandler(event);
                if (filterEvent != null) {
                    if (!filterEvent.equals(event)) {
                        // ���Filter������¼������˱仯
                        // ����������¼�����ת��������˺���¼�
                        try {
                            this._eventBus.fire(
                                    filterEvent.getPackageKey(),
                                    filterEvent.getEventKey(),
                                    filterEvent);
                        } catch (Throwable ex) {
                            logger.warn(ex.getMessage());
                        }
                    } else {
                        // ��������¼�û�з����仯���������������
                        filterEvents.add((T) filterEvent);
                    }
                }
            }
        }
        List<T> dealEvents = filterEvents != null ? filterEvents : this._eventBuf;
        try {
            if (filterEvents != null) {
                // ���ִ�й����˴���������˺���¼�
                if (!filterEvents.isEmpty()) {
                    this._handler.handle(filterEvents);
                }
            } else {
                // ������ԭ���¼�
                this._handler.handle(this._eventBuf);
            }
        } catch (Throwable ex) {
            err = ex.getCause() != null ? ex.getCause() : ex;
            logger.error(ex.getMessage(), ex);
        }
        // ����ԭ�е������¼�
        for (EventWrapper<T> _wrapper : this._wrapperBuf) {
            this.finish(_wrapper, err);
        }
        if (!this._handler.getHandlerKey().equals(EBus.COMPONENT_KEY_STATICS)) {
            // �Դ�����¼��������þ����Ƿ���Ҫ���µ���ִ�и��¼�
            for (T _event : dealEvents) {
                if (_event.getScheduleType() == Event.RETRY_ON_ERROR) {
                    if (err != null) {
                        // ʧ����ִ�У����µ��ȸ��¼�
                        this._eventBus.schedule(_event);
                    }
                } else if (_event.getScheduleType() == Event.MULTIPLE) {
                    // ��ε��ȣ����ӱ���ִ�н�����
                    int retryNum = _event.getRetryNum();
                    if (retryNum > 0) {
                        // �Լ��������м�1������ֱ������0����ִ��
                        _event.setRetryNum(--retryNum);
                        this._eventBus.schedule(_event);
                    } else if (retryNum == -1) {
                        // ���ڴ���Ϊ-1����ʾ��Զ����ִ����ȥ
                        this._eventBus.schedule(_event);
                    }
                }
            }
        }
        this._eventBuf.clear();
        this._wrapperBuf.clear();
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

    public void onEvent(EventWrapper<T> wrapper, long sequence,
                        boolean endOfBatch) {
        try {
            T event = wrapper.getEvent();
            // �¼���ʼ����
            event.run();
            // ���¼����뵽����������
            this._eventBuf.add(event);
            this._wrapperBuf.add(wrapper);
            if (endOfBatch) {
                // һ���ν��������Կ�ʼ������
                this.doBatchEvents();
            }
        } catch (Throwable ex) {
            logger.warn(ex.getMessage(), ex);
        }
    }

    @SuppressWarnings("unchecked")
    public void setHandler(AbstractHandler handler) {
        this._handler = (BatchEventHandler<T>) handler;
    }
}
