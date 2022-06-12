package com.jurassic.core.progress.worker;

import com.jurassic.core.bus.EventBus;
import com.jurassic.core.event.EventWrapper;
import com.jurassic.core.global.GlobalInstRegisterTable;
import com.jurassic.core.handler.AbstractHandler;
import com.jurassic.core.processor.EventHandler;
import com.jurassic.core.progress.handler.EPU;
import com.jurassic.core.progress.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * �¼������߼�������EPU
 * �����������¼��������߼�
 *
 * @author yzhu
 */
public class EPUWorker implements EventHandler<EventWrapper<Task>> {

    private EPU _epu;// ���봦����
    private final EventBus _eventBus;// ����

    protected static Logger logger = LoggerFactory.getLogger(EPUWorker.class);

    public EPUWorker() {
        this._eventBus = GlobalInstRegisterTable.getInst(
                EventBus.GLOBAL_KEY, EventBus.class);
    }

    public void onEvent(EventWrapper<Task> wrapper, long sequence,
                        boolean endOfBatch) {
        // epu�������̵���һ������
        Task task = wrapper.getEvent();
        try {
            // �������̺���ִ�е��¼��б�
            this.calculate(task);
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
        } finally {
            wrapper.reset();
        }
    }

    /**
     * ���ݵ�ǰ���¼����������ִ���¼�
     */
    private void calculate(Task task) {
        // ʹ�����봦���������¼��ĺ�������
        List<Task> nextTasks = this._epu.calculate(task);
        // autoEventsͳ�������Զ�ִ�е��¼�
        // ����JumpStartTask֮��
        List<Task> autoTasks = new ArrayList<>();
        if (nextTasks != null && !nextTasks.isEmpty()) {
            // �������̵���ָ��
            for (Task _task : nextTasks) {
                if (_task.isAuto()) {
                    // �Զ�����,��Ҫ�ٴμ���
                    autoTasks.add(_task);
                } else if (_task.getStatus() == 0) {
                    // ���Զ�ִ�е�����,����ŵ�������
                    try {
                        this._eventBus.fire(_task.getPackageKey(),
                                _task.getEventKey(), _task);
                    } catch (Throwable e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }

            if (!autoTasks.isEmpty()) {
                // �ٴμ����Զ��������һ������
                for (Task auto : autoTasks) {
                    // �Զ��������
                    auto.end(null);
                    this.calculate(auto);
                }
            }
        }
    }

    public void setHandler(AbstractHandler handler) {
        this._epu = (EPU) handler;
    }
}
