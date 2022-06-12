package com.jurassic.core.event;

/**
 * �¼��Ļ�������
 * 
 * @author yzhu
 */
public abstract class Event {

	public Event() {
	}

	/**
	 * ����¼����ڵİ�
	 */
	public abstract String getPackageKey();

	/**
	 * ����¼�������
	 */
	public abstract String getEventKey();

	public static final int S_START = 1;// ״̬-��ʼ����
	public static final int S_RUN = 2;// ״̬-ִ����
	public static final int S_END = 3;// ״̬-ִ�����
	public static final int S_HALT = 4;// ״̬-��ͣ
	public static final int S_RESUME = 5;// ״̬-�ָ�ִ��
	protected int _status = 0;// ״̬

	public int getStatus() {
		return this._status;
	}

	/**
	 * �����¼��ĸ���״̬��Ϣ
	 */
	public void reset() {
		this._status = 0;
		this._dealTime = -1;
		this._publishTime = -1;
		this._finishTime = -1;
		this._ex = null;
	}

	/**
	 * �¼�����
	 */
	public void publish() {
		if (this._status == 0) {
			this._status = S_START;
			this.setPublishTime(System.currentTimeMillis());
			if (this._monitor != null) {
				// ������¼����м�أ�����¼���ʼ����
				this._monitor.eventStart(this);
			}
		}
	}

	/**
	 * �¼�����
	 */
	public void run() {
		if (this._status == S_START) {
			this.setDealTime(System.currentTimeMillis());
			this._status = S_RUN;
		}
	}

	/**
	 * �¼���ͣ����
	 */
	public void halt() {
		if (this._status == S_RUN) {
			this._status = S_HALT;
		}
	}

	/**
	 * �¼�׼������ִ��
	 */
	public void startResume() {
		if (this._status == S_HALT) {
			this._status = S_RESUME;
		}
	}

	/**
	 * �ж�ʱ���Ƿ�߱����Լ���ִ�е�״̬
	 */
	public boolean isResumed() {
		return this._status == S_RESUME;
	}

	/**
	 * �¼���������
	 */
	public void resume() {
		if (this._status == S_RESUME) {
			this._status = S_RUN;
		}
	}

	/**
	 * �¼��������
	 */
	public void end(Throwable ex) {
		if (this._status == S_RUN) {
			this.setFinishTime(System.currentTimeMillis());
			this._status = S_END;
			this._ex = ex;
			if (this._monitor != null) {
				// ������¼����м�أ�����¼���������
				this._monitor.eventEnd(this);
			}
		}
	}

	// ʱ�����������еĸ���ʱ���
	private long _publishTime = -1;// ����ʱ��
	private long _dealTime = -1;// ����ʱ��
	private long _finishTime = -1;// ���ʱ��
	private Throwable _ex;// �¼��������

	/**
	 * �ж�ʱ���Ƿ������
	 */
	public Throwable getError() {
		return this._ex;
	}

	public long getPublishTime() {
		return _publishTime;
	}

	private void setPublishTime(long publishTime) {
		if (this._publishTime == -1)
			this._publishTime = publishTime;
	}

	public long getDealTime() {
		return _dealTime;
	}

	private void setDealTime(long dealTime) {
		if (this._dealTime ==-1)
			this._dealTime = dealTime;
	}

	public long getFinishTime() {
		return _finishTime;
	}

	private void setFinishTime(long finishTime) {
		if (this._finishTime == -1)
			this._finishTime = finishTime;
	}

	// ��������
	public static final int SINGLE = 0;// ����ִ��
	public static final int RETRY_ON_ERROR = 1;// �����ʱ������
	public static final int MULTIPLE = 2;// ���ִ��

	private int _scheduleType = SINGLE;// ��������
	private long _scheduleSpan = 0;// ִ�м����ms
	private int _retryNum = 0;// ���³��Դ�������������һ��ִ��

	public void setScheduleType(int type) {
		this._scheduleType = type;
	}

	public int getScheduleType() {
		return this._scheduleType;
	}

	public void setScheduleSpan(long span) {
		this._scheduleSpan = span;
	}

	public long getScheduleSpan() {
		return this._scheduleSpan;
	}

	public void setRetryNum(int num) {
		this._retryNum = num;
	}

	public int getRetryNum() {
		return this._retryNum;
	}

	private EventMonitor _monitor;// �¼��ļ����

	public void setMonitor(EventMonitor monitor) {
		this._monitor = monitor;
	}
}
