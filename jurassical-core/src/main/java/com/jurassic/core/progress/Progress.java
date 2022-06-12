package com.jurassic.core.progress;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.jurassic.core.progress.handler.pin.Pin;
import com.jurassic.core.notification.ResultNotification;
import com.jurassic.core.progress.task.EndTask;
import com.jurassic.core.progress.task.StartTask;
import com.jurassic.core.progress.task.Task;


/**
 * ҵ�����̶���
 * ����������������ǰ���������
 * ����ҵ��������߼�
 * ����ʼ��start����,������end����
 *
 * @author yzhu
 */
public class Progress {
	public final static int S_INIT = 0;// ��ʼ״̬,���̸ոմ���
	public final static int S_RUN = 1;// ����ִ��start����,��ʼִ��
	public final static int S_INTERRUPTED = 2;// ����ִ�й�����,��ĳЩ�����Ϸ����������жϽ���
	public final static int S_END = 3;// ������������
	private int _status;// ���̵�״̬

	// ���̵�������Ϣ
	private final String _packageKey;// ���������������key
	private final String _progressKey;// ҵ�����̵�����
	private final List<Task> _tasks = new ArrayList<>();// �������е�����
	private final StartTask _startTask;// ��ʼ����
	private final EndTask _endTask;// ��������

	// ÿ�����̿���ѡ���Ե�����ҵ��Ľ�����
	// ҵ���������ָ�����������ҵ�����������ݵ������
	// ����ִ�е�������󴥷�֪ͨ����,��ҵ���������첽֪ͨ������
	private Task _endPoint;// ����notification������
	private ResultNotification _notification;// �ص��ӿ�
	private Pin _result;// ��ȡ���̽���Ľӿ�

	// ���̵��쳣��������
	// �쳣�����,ÿ����Ŀ��Ϊ3��Ԫ��:
	// ǰ2������˵�����쳣���������Χ,������ָ�����쳣������������
	private final List<Task[]> _catchTbl;

	private final AtomicInteger _runningNum;// ���̵����м�����
	private Progress _parent;// ������

	private ProgressMonitor _monitor;// ���̵ļ����

	public Progress(String packageKey, String progressKey, AtomicInteger runningNum) {
		this._packageKey = packageKey;
		this._progressKey = progressKey;
		this._runningNum = runningNum;
		this._status = S_INIT;
		this._catchTbl = new ArrayList<>();
		// ��ʼ���������̵�ʱ���Զ��������̵���ʼ�ͽ�������
		this._startTask = new StartTask(this);
		this._endTask = new EndTask(this);
	}

	public void setMonitor(ProgressMonitor monitor) {
		this._monitor = monitor;
	}

	public void setParent(Progress parent){
		this._parent = parent;
	}
	
	public Progress getParent(){
		return this._parent;
	}

	/**
	 * ���ݷ�������������ҵ���֮��Ӧ���쳣��������
	 */
	public Task findErrorEntry(Task task) {
		for (Task[] entry : this._catchTbl) {
			// �����������������ڲ���Χ֮��,�򷵻��쳣�����������
			// ÿһ��catch entry�����˼���쳣����ķ�Χ������������Ͻ�,�½���쳣�����������
			if (entry[0].hasChild(task) && task.hasChild(entry[1])) {
				return entry[2];
			}
		}
		return null;
	}

	/**
	 * ������̵��쳣�������
	 */
	public void addCatchTable(Task start, Task end, Task errorEntry) {
		if (start == null || end == null || errorEntry == null)
			return;
		_catchTbl.add(new Task[] { start, end, errorEntry });
	}

	public String getPackageKey() {
		return this._packageKey;
	}

	/**
	 * ������̵��������
	 */
	protected Object getResult() {
		return this._result != null ?
				this._result.getData() : null;
	}

	/**
	 * �������̵����
	 * ���̵�������ʾ������ִ�е���Ӧ�����ʱ�򣬿��Ի�����̵Ľ�����ݲ�����
	 */
	public void setEndpoint(Task endPoint, Pin result) {
		// ���ûص�������д�����task�Ͷ�Ӧ����
		if (endPoint == null)
			return;
		this._endPoint = endPoint;
		this._result = result;
	}

	/**
	 * �������̵�����ӿ�
	 */
	public void setNotification(ResultNotification notification) {
		this._notification = notification;
	}

	/**
	 * ֪ͨ����������
	 */
	public void notifyResult() {
		if (this._notification == null)
			return;
		// ������
		Object result = this.getResult();
		this._notification.notify(result);
		// һ������ֻ֪ͨһ��������
		this._notification = null;
	}

	/**
	 * ֪ͨ�����쳣
	 */
	public void notifyError(Throwable ex) {
		if (this._notification == null)
			return;
		// ������
		this._notification.notify(ex);
		// һ������ֻ֪ͨһ��������
		this._notification = null;
	}

	public Task getEndpoint() {
		return this._endPoint;
	}

	/**
	 * ҵ����������
	 * ͨ����start�������ʱ��ִ��
	 */
	public void run() {
		this._status = S_RUN;
		// ������������������ͳ����Ϣ
		if (this._runningNum != null) {
			this._runningNum.incrementAndGet();
		}
		// ������̵Ŀ�ʼ
		if (this._monitor != null) {
			this._monitor.serviceStart(this);
		}
	}

	/**
	 * ���̷����쳣�ж���
	 */
	public void happenException() {
		this._status = S_INTERRUPTED;
	}

	/**
	 * ҵ�����̽���
	 * ͨ����end�������ʱ��ִ��
	 */
	public void end() {
		// �����������������̱��״̬Ϊend,����Ϊinterrupted����
		if (this._status != S_INTERRUPTED)
			this._status = S_END;
		// ������ͬʱ������������������ͳ����Ϣ
		if (this._runningNum != null) {
			this._runningNum.decrementAndGet();
		}
		// ������̵Ľ���
		if (this._monitor != null) {
			this._monitor.serviceEnd(this);
		}
	}

	/**
	 * �ж�һ��ҵ�������Ƿ��Ѿ��ж���
	 */
	public boolean isInterrupted() {
		return this._status == S_INTERRUPTED;
	}

	/**
	 * ����TaskChain�����������������ִ�н�ֹʱ��
	 */
	public void setCutOffTime(long time) {
		for (Task t : this._tasks) {
			t.setCutOffTime(time);
		}
	}

	public String getProgressKey() {
		return _progressKey;
	}

	/**
	 * �½������ʱ�򣬽��½�������ӵ�����
	 */
	public void addTask(Task task) {
		this._tasks.add(task);
	}

	public Task getEndTask() {
		return this._endTask;
	}

	public Task getStartTask() {
		return this._startTask;
	}

}
