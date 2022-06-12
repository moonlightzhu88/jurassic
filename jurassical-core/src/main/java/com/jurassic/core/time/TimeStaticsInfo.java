package com.jurassic.core.time;

import com.jurassic.core.event.Event;

import java.util.List;

/**
 * ����ִ�е�ʱ��ͳ��
 * �ռ��˸�����������һ���̶�ʱ�����ִ����������
 * �����������������,�ȴ�����������ʱ�����Ϣ
 * 
 * @author yzhu
 */
public class TimeStaticsInfo {

	// ����¼�����
	private int _finishNum;
	// ������������
	private int _errNum;
	// �¼�ƽ���ȴ�ִ��ʱ��,����
	private long _avgWaitingTime;
	// �¼�ƽ��ִ��ʱ��,����
	private long _avgExecTime;

	/**
	 * ���ʱ���ִ����Ϣͳ��
	 */
	public void finishEvent(List<Event> events) {
		long waitingTime = 0;
		long executeTime = 0;
		for (Event t : events) {
			// ָ��ȴ���ʱ��
			waitingTime += t.getDealTime() - t.getPublishTime();
			// ָ��ִ�е�ʱ��
			executeTime += t.getFinishTime() - t.getDealTime();

			this._finishNum++;
			if (t.getError() != null)
				this._errNum++;
		}

		this._avgWaitingTime += (waitingTime - this._avgWaitingTime * events.size())
				/ this._finishNum;
		this._avgExecTime += (executeTime - this._avgExecTime * events.size())
				/ this._finishNum;

	}

	/**
	 * ����ͳ����Ϣ
	 */
	public void reset() {
		this._avgExecTime = 0;
		this._avgWaitingTime = 0;
		this._errNum = 0;
		this._finishNum = 0;
	}

	public int getFinishNum() {
		return _finishNum;
	}

	public int getErrNum() {
		return _errNum;
	}

	public long getAvgWaitingTime() {
		return _avgWaitingTime;
	}

	public long getAvgExecTime() {
		return _avgExecTime;
	}

}
