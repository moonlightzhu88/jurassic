package com.jurassic.core.progress.factory;

import com.jurassic.core.progress.Progress;
import com.jurassic.core.progress.handler.pin.ObjectPin;
import com.jurassic.core.progress.handler.pin.Pin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ���̹���
 * 
 * @author yzhu
 * 
 */
public class ProgressFactory {

	private static final Logger logger = LoggerFactory.getLogger(ProgressFactory.class);

	// ����������������ע���
	private Map<String, ProgressTemplate> _templateTbl = new HashMap<>();
	// ÿ�����̵����м���
	private Map<String, AtomicInteger> _runningProgressNums = new HashMap<>();

	/**
	 * ��������
	 */
	public Progress createProgress(String packageKey, String progressKey,
								   Pin[] params, Progress parent) {
		// �������̵�key�ҵ���Ӧ�Ĺ�����
		ProgressTemplate generator = this._templateTbl.get(progressKey);
		if (generator == null)
			return null;
		else {
			// ��������
			try {
				Progress progress = new Progress(packageKey, progressKey,
						this._runningProgressNums.get(progressKey));
				generator.initProgress(progress, params);
				if (parent != null && progress != null)
					progress.setParent(parent);
				return progress;
			} catch (Throwable ex) {
				logger.warn(ex.getMessage(), ex);
				return null;
			}
		}
	}

	/**
	 * �������������
	 */
	public void addTemplate(ProgressTemplate template) {
		this._templateTbl.put(template.getProgressKey(), template);
		this._runningProgressNums.put(
				template.getProgressKey(), new AtomicInteger(0));
	}

	/**
	 * �ж����̹����Ƿ�����������ģ��
	 */
	public boolean hasProgressTemplate() {
		return !this._templateTbl.isEmpty();
	}

	/**
	 * �ж��Ƿ������е�����
	 */
	public boolean hasRunningProgress() {
		for (Map.Entry<String, AtomicInteger> entry : this._runningProgressNums.entrySet()) {
			AtomicInteger num = entry.getValue();
			if (num.intValue() > 0) {
				logger.warn("progress " + entry.getKey() + " has running");
				return true;
			}
		}
		return false;
	}

	/**
	 * ������Դ
	 */
	public void destroyResource() {
		this._templateTbl.clear();
		this._templateTbl = null;
		this._runningProgressNums.clear();
		this._runningProgressNums = null;
	}
}
