package com.jurassic.core.processor;

import com.jurassic.core.event.Event;
import com.jurassic.core.handler.AbstractHandler;

import java.util.concurrent.ExecutorService;

/**
 * �¼��Ĵ�����
 * ���ض����Ĵ�����,ִ����Ӧ��ҵ���߼�
 * 
 * @author yzhu
 * @param <T>
 */
public interface EventProcessor<T extends Event>{

	/**
	 * �������
	 */
	boolean start(ExecutorService es);

	/**
	 * ����ر�,��ȷ�����и�����ϵ��¼���ȫ�������
	 */
	void shutdown();

	/**
	 * �ж�����Ƿ���������
	 * �������е�������Խ����¼������ɴ���������
	 */
	boolean isStarted();

	/**
	 * �ж�����Ƿ������ڴ���Ĺ���
	 */
	boolean hasDealingEvent();

	/**
	 * ҵ����
	 */
	boolean doEvent(T event);

	/**
	 * ��ô�����
	 */
	AbstractHandler getHandler();

}
