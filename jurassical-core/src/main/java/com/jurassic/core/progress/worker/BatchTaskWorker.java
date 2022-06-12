package com.jurassic.core.progress.worker;

import com.jurassic.core.bus.EventBus;
import com.jurassic.core.bus.impl.EBus;
import com.jurassic.core.event.Event;
import com.jurassic.core.event.EventWrapper;
import com.jurassic.core.global.GlobalInstRegisterTable;
import com.jurassic.core.handler.AbstractHandler;
import com.jurassic.core.processor.EventHandler;
import com.jurassic.core.progress.handler.BatchTaskHandler;
import com.jurassic.core.progress.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * ������task��worker
 *
 * @author yzhu
 */
public class BatchTaskWorker<T extends Task> implements EventHandler<EventWrapper<T>> {

    private BatchTaskHandler<T> _handler;// ҵ������
    private final EventBus _eventBus;// ����

    private final List<EventWrapper<T>> _wrapperBuf = new ArrayList<>();
    private final List<T> _taskBuf = new ArrayList<>();// ���������¼��Ļ�����

    protected static Logger logger = LoggerFactory.getLogger(BatchTaskWorker.class);

    public BatchTaskWorker() {
        this._eventBus = GlobalInstRegisterTable.getInst(
                EventBus.GLOBAL_KEY, EventBus.class);
    }

    private void doBatchEvents() {
        // ������ʼ
        Throwable err = null;
        try {
            this._handler.handle(this._taskBuf);
        } catch (Throwable ex) {
            err = ex.getCause() != null ? ex.getCause() : ex;
            logger.error("event " + this._handler.getHandlerKey() + " happen error:"
                    + err.getMessage(), err);
        } finally {
            for (int i = 0; i < this._taskBuf.size(); i++) {
                // ������ɺ��¼�����
                this._wrapperBuf.get(i).reset();
            }
        }
        // ��Ҫ�����֪ͨ������,�������������������߼�
        if (err != null) {
            for (T task : this._taskBuf) {
                task.end(err);
                this.doNext(task);
            }
        } else {
            for (T task : this._taskBuf) {
                // ����������,����¼�����ͣ,��������Ը��¼����д���
                // ֱ���������Ը��¼��������������Ĳ���
                if (task.getStatus() != Event.S_HALT) {
                    task.end(null);
                    this.doNext(task);
                }
            }
        }
        // ��ջ�����
        this._taskBuf.clear();
        this._wrapperBuf.clear();
    }

    public void onEvent(EventWrapper<T> wrapper, long sequence,
                        boolean endOfBatch) {
        try {
            T task = wrapper.getEvent();
            // �¼���ʼ����
            task.run();
            // �Ƚ��¼����뵽������
            this._taskBuf.add(task);
            this._wrapperBuf.add(wrapper);
            if (endOfBatch) {
                this.doBatchEvents();
            }
        } catch (Throwable ex) {
            logger.warn(ex.getMessage(), ex);
        }
    }

    /**
     * ��ǰ����ִ����ϣ����۳ɹ����쳣��
     * ִ����һ������
     */
    private void doNext(Task task) {
        try {
            // ����ǰtask����epu���������̵���һ��
            if (task.getProgress() != null)
                this._eventBus.fire(
                        task.getProgress().getPackageKey(),
                        "epu", task);
        } catch(Throwable ex) {
            logger.warn(ex.getMessage(), ex);
        }
        try {
            // ����ǰ������static��������ִ����Ϣ��ͳ��
            this._eventBus.fire(
                    null, EBus.COMPONENT_KEY_STATICS, task);
        } catch(Throwable ex) {
            logger.warn(ex.getMessage(), ex);
        }
    }

    @SuppressWarnings("unchecked")
    public void setHandler(AbstractHandler handler) {
        this._handler = (BatchTaskHandler<T>) handler;
    }
}
