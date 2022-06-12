package com.jurassic.core.progress.task;

import com.jurassic.core.bus.EventBus;
import com.jurassic.core.global.GlobalInstRegisterTable;
import com.jurassic.core.notification.ResultNotification;
import com.jurassic.core.progress.Progress;
import com.jurassic.core.progress.handler.pin.ObjectPin;
import com.jurassic.core.progress.handler.pin.Pin;


/**
 * �����̵���Task
 *
 * @author yzhu
 */
public class SubProgressInvokeTask extends Task implements ResultNotification {
    public static final String KEY = "sub_progress_invoke";
    private final String _subPackageKey;// ���������ڵİ�
    private final String _subProgressKey;// ������key
    private Pin[] _params;// �����̵����ݲ����ܽ�
    private ObjectPin _output = new ObjectPin();// �����̵����

    public SubProgressInvokeTask(Progress progress, String subPackageKey,
                                 String subProgressKey) {
        super(progress, "");
        this._subPackageKey = subPackageKey;
        this._subProgressKey = subProgressKey;
    }

    public String getSubPackageKey() {
        return this._subPackageKey;
    }

    public String getSubProgressKey() {
        return this._subProgressKey;
    }

    public Pin[] getParams() {
        return this._params;
    }

    public String getPackageKey() {
        return this._progress.getPackageKey();
    }

    public String getEventKey() {
        return KEY;
    }

    public boolean isAuto() {
        return false;
    }

    public void input(Pin... pins) {
        this._params = pins;
    }

    public Pin output(int pinIdx) {
        return this._output;
    }

    public void notify(Object result) {
        // result����Ϊ��������������쳣��Ϣ
        this._output.setData(result);
        // ��������׼������ִ��
        this.startResume();
        // �����߷��͵��ý���¼�������֮ǰ�������̵���task
        EventBus eventBus = GlobalInstRegisterTable.getInst(
                EventBus.GLOBAL_KEY, EventBus.class
        );
        try {
            eventBus.fire(this.getPackageKey(), KEY, this);
        } catch (Throwable ignored) {}
    }
}
