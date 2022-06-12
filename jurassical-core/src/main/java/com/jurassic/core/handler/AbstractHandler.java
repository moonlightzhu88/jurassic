package com.jurassic.core.handler;

import com.jurassic.core.bus.Constant;
import com.jurassic.core.resource.ResourceAware;
import com.jurassic.core.resource.ResourceHandler;
import com.jurassic.core.resource.ResourceHandlerFactory;

import java.lang.reflect.Field;

/**
 * �������Ļ��ඨ��
 *
 * @author yzhu
 * 
 */
public abstract class AbstractHandler {

	protected int _numOfThread;// �߳�����
	protected int _powerOfBuffer;// ���ݻ�������С��ʹ��2���ݴη��������¼����ָ��

	public AbstractHandler() {
	}

	/**
	 * ���ô�����
	 */
	public void config(int numOfThread, int powerOfBuffer) {
		this._numOfThread = numOfThread > 0 ? numOfThread : Constant.DRPT_WORKER_SIZE;
		this._powerOfBuffer = powerOfBuffer > 0 ? powerOfBuffer : Constant.DRPT_DATA_SIZE_POWER;
		if (this._numOfThread > Constant.DRPT_MAX_WORKER_SIZE)
			this._numOfThread = Constant.DRPT_MAX_WORKER_SIZE;
		if (this._powerOfBuffer > Constant.DRPT_MAX_DATA_SIZE_POWER)
			this._powerOfBuffer = Constant.DRPT_MAX_DATA_SIZE_POWER;
	}


	public abstract String getHandlerKey();

	public int getNumOfThread() { return this._numOfThread; }

	public int getPowerOfBuffer() { return this._powerOfBuffer; }

	/**
	 * ����������Ϣ
	 */
	public void loadProperties(DeployProperties props) {
		// Ĭ��ʵ��
	}

	/**
	 * ��Դ��ע��
	 */
	public void resourceInject(ResourceHandlerFactory factory) {
		Field[] fields = this.getClass().getDeclaredFields();
		// ��������field
		for (Field field : fields) {
			// ���Ҵ���@ResourceAwareע���field
			ResourceAware aware =
					field.getAnnotation(ResourceAware.class);
			if (aware != null) {
				String name = field.getName();
				// ͨ�������������һ������Ϊname�ľ��
				// �þ������name��Ϊkey�����߳���
				ResourceHandler<?> handlerOfResource = factory.generateHandler(
								name, aware.resourceFactory());
				// �����ע���������
				try {
					boolean access = field.isAccessible();
					field.setAccessible(true);
					field.set(this, handlerOfResource);
					field.setAccessible(access);
				} catch (Throwable ignored) {
				}
			}
		}
	}
}
