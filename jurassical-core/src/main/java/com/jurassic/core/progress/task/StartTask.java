package com.jurassic.core.progress.task;

import com.jurassic.core.progress.handler.pin.Pin;
import com.jurassic.core.progress.Progress;

/**
 * ���̵���������
 * name��key���Զ�������������+_start����ʽ
 * �������ָ�����ͬ���̵Ŀ�ʼ����
 *
 * @author yzhu
 */
public class StartTask extends Task {
	// ����ҵ�������и��Բ�ͬ��Start�ڵ�,���ǵ�key��_start��β,������ҵ�����̵�key��Ϊ��ͷ
	public static final String START_SUFFIX = "_start";

	public StartTask(Progress progress) {
		super(progress, "");
	}

	public boolean isAuto() {
		return false;
	}

	/**
	 * û������ܽ�
	 */
	public void input(Pin... pins) {
	}

	/**
	 * û������ܽ�
	 */
	public Pin output(int pinIdx) {
		return null;
	}

	public String getPackageKey() {
		return this._progress.getPackageKey();
	}

	public String getEventKey() {
		return this._progress.getProgressKey() + START_SUFFIX;
	}
}
