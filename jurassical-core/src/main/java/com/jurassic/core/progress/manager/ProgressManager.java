package com.jurassic.core.progress.manager;

import com.jurassic.core.event.Event;
import com.jurassic.core.progress.Progress;
import com.jurassic.core.progress.handler.pin.Pin;

/**
 * ���̹���
 * 
 * @author yzhu
 * 
 */
public interface ProgressManager {

	String GLOBAL_KEY = "progress_manager";

	/**
	 * ����һ��Ӧ������
	 */
	Progress createProgress(String packageKey,
							String progressKey,
							Pin[] params,
							Progress parent);

	Event createEvent(String packageKey, String eventKey, Object[] params);

}
