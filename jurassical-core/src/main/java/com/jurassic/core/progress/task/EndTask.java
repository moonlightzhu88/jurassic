package com.jurassic.core.progress.task;

import com.jurassic.core.progress.handler.pin.Pin;
import com.jurassic.core.progress.Progress;

/**
 * ���̵Ľ�������
 * name��key���Զ�������������+_end����ʽ
 * �������ָ�����ͬ���̵Ŀ�ʼ����
 *
 * @author yzhu
 */
public class EndTask extends Task {
	// ����ҵ�������и��Բ�ͬ��End�ڵ�,���ǵ�key��_end��β,������ҵ�����̵�key��Ϊ��ͷ
	public static final String END_SUFFIX = "_end";

	public EndTask(Progress progress) {
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
		return this._progress.getProgressKey() + END_SUFFIX;
	}
}
