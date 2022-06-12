package com.jurassic.core.bus;

import com.jurassic.core.bus.error.ComponentNotFoundErr;
import com.jurassic.core.bus.error.EventBusNotStartedErr;
import com.jurassic.core.bus.error.EventNotFiredErr;
import com.jurassic.core.deploy.DeployContext;
import com.jurassic.core.event.Event;

/**
 * �¼�����,Jurassicϵͳ�ĺ��Ĳ���
 * ���������ת�������п��
 * ����������еĴ�����,�ڴ�����֮�䴫��������Ϣ
 * ������������ִ��
 * 
 * @author yzhu
 */
public interface EventBus {

	String GLOBAL_KEY = "event_bus";//���ߵ�id

	/**
	 * ���¼����͸���Ӧ�Ĵ��������д���
	 */
	void fire(String packageKey, String handler, Event event) throws EventBusNotStartedErr,
			ComponentNotFoundErr, EventNotFiredErr;

	/**
	 * �ӳٵ����¼�
	 * ����eventָ���ĵ������ô����¼���ִ��
	 */
	void schedule(Event event);

	/**
	 * ��������
	 */
	boolean start();

	/**
	 * �ر�����
	 */
	void shutdown();

	/**
	 * ����Ӧ�ð�
	 */
	boolean deploy(DeployContext ctx);

	/**
	 * ж��Ӧ�ð�
	 */
	void undeploy(String packageKey);

}
