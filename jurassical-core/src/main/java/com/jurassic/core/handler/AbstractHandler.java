package com.jurassic.core.handler;

import com.jurassic.core.bus.Constant;
import com.jurassic.core.resource.ResourceAware;
import com.jurassic.core.resource.ResourceHandler;
import com.jurassic.core.resource.ResourceHandlerFactory;

import java.lang.reflect.Field;

/**
 * 处理器的基类定义
 *
 * @author yzhu
 * 
 */
public abstract class AbstractHandler {

	protected int _numOfThread;// 线程数量
	protected int _powerOfBuffer;// 数据缓冲区大小，使用2的幂次方，这里记录的是指数

	public AbstractHandler() {
	}

	/**
	 * 配置处理器
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
	 * 加载配置信息
	 */
	public void loadProperties(DeployProperties props) {
		// 默认实现
	}

	/**
	 * 资源的注入
	 */
	public void resourceInject(ResourceHandlerFactory factory) {
		Field[] fields = this.getClass().getDeclaredFields();
		// 搜索所有field
		for (Field field : fields) {
			// 查找带有@ResourceAware注解的field
			ResourceAware aware =
					field.getAnnotation(ResourceAware.class);
			if (aware != null) {
				String name = field.getName();
				// 通过句柄工厂创建一个名字为name的句柄
				// 该句柄会以name作为key绑定在线程上
				ResourceHandler<?> handlerOfResource = factory.generateHandler(
								name, aware.resourceFactory());
				// 将句柄注入该属性中
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
