package com.jurassic.core.progress.worker;

import com.jurassic.core.bus.EventBus;
import com.jurassic.core.bus.impl.EBus;
import com.jurassic.core.event.Event;
import com.jurassic.core.event.EventWrapper;
import com.jurassic.core.global.GlobalInstRegisterTable;
import com.jurassic.core.handler.AbstractHandler;
import com.jurassic.core.processor.WorkHandler;
import com.jurassic.core.progress.handler.TaskHandler;
import com.jurassic.core.progress.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * ������Task��worker
 *
 * @param <T>
 */
public class SingleTaskWorker<T extends Task> implements WorkHandler<EventWrapper<T>> {

    private TaskHandler<T> _handler;// ҵ������
    private final EventBus _eventBus;// ����

    protected static Logger logger = LoggerFactory.getLogger(SingleTaskWorker.class);

    public SingleTaskWorker() {
        this._eventBus = GlobalInstRegisterTable.getInst(
                EventBus.GLOBAL_KEY, EventBus.class);
    }

    /**
     * ��������
     */
    private void doTask(EventWrapper<T> wrapper) {
        T task = wrapper.getEvent();
        // �ǼǴ���ʱ�䲢����¼���ʼ����
        task.run();
        Throwable err = null;
        try {
            // ҵ����
            this._handler.handle(task);
        } catch (Throwable ex) {
            err = ex.getCause() != null ? ex.getCause() : ex;
            logger.error(
                    "event " + this._handler.getHandlerKey() + " happen error:"
                            + err.getMessage(), err);
        } finally {
            // ������ɺ��¼�����
            wrapper.reset();
        }
        // ��Ҫ�����֪ͨ������,�������������������߼�
        if (err != null) {
            task.end(err);
            this.doNext(task);
        } else {
            // ����������,����¼�����ͣ,��������Ը��¼����д���
            // ֱ���������Ը��¼��������������Ĳ���
            if (task.getStatus() != Event.S_HALT) {
                task.end(null);
                this.doNext(task);
            }
        }
    }

    public void onEvent(EventWrapper<T> wrapper) {
        try {
            this.doTask(wrapper);
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
        this._handler = (TaskHandler<T>) handler;
    }
}
