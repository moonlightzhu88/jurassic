package com.jurassic.core.progress.task;

import com.jurassic.core.progress.handler.pin.Pin;
import com.jurassic.core.progress.Progress;

import java.util.List;

/**
 * ��ת��֧�Ľ�������
 * ���еķ�֧��������������ӵ�������
 * JumpEndTask������֮������JumpStartTask����֮ǰ��̬��ת��·������
 * ���ж��Ƿ����еķ�֧·���������
 * ��Ҳ��һ���Զ�ִ������,�����ض��Ĵ�����
 *
 * @author yzhu
 */
public class JumpEndTask extends Task {
	
	public static final String KEY = "jmp_end";
	private JumpStartTask _startOfJmpTask;// ��֮��Ӧ����ת������ʼ

	public JumpEndTask(Progress progress, String desc) {
		super(progress, desc);
	}

	public void setJumpStartTask(JumpStartTask start) {
		this._startOfJmpTask = start;
	}

	public boolean isAuto() {
		return true;
	}

	/**
	 * ���ڷ�֧·���ĳ���isReady�ж�,��Ҫ�ҵ�ʵ���ߵ�������֧��״̬���
	 */
	public boolean isReady() {
		if (this._status != 0)
			return false;

		for (Task prev : this._prevTasks) {
			if (this._startOfJmpTask.isBranchTask(prev)) {
				if (prev.getStatus() != S_END)
					return false;
			}
		}

		return true;
	}

	/**
	 * ��������
	 */
	public void input(Pin... pins) {
	}

	/**
	 * �������
	 */
	public Pin output(int pinIdx) {
		return null;
	}

	public String getPackageKey() {
		return this._progress.getPackageKey();
	}

	public String getEventKey() {
		return KEY;
	}
}
