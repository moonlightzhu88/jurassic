package com.jurassic.core.bus;

import com.jurassic.core.bus.error.ComponentNotFoundErr;
import com.jurassic.core.bus.error.EventBusNotStartedErr;
import com.jurassic.core.bus.error.EventNotFiredErr;
import com.jurassic.core.deploy.DeployContext;
import com.jurassic.core.event.Event;

/**
 * 事件总线,Jurassic系统的核心部件
 * 负责调度运转整个运行框架
 * 负责管理所有的处理器,在处理器之间传递数据信息
 * 负责调度任务的执行
 * 
 * @author yzhu
 */
public interface EventBus {

	String GLOBAL_KEY = "event_bus";//总线的id

	/**
	 * 将事件传送给对应的处理器进行处理
	 */
	void fire(String packageKey, String handler, Event event) throws EventBusNotStartedErr,
			ComponentNotFoundErr, EventNotFiredErr;

	/**
	 * 延迟调度事件
	 * 按照event指定的调度配置触发事件的执行
	 */
	void schedule(Event event);

	/**
	 * 总线启动
	 */
	boolean start();

	/**
	 * 关闭总线
	 */
	void shutdown();

	/**
	 * 发布应用包
	 */
	boolean deploy(DeployContext ctx);

	/**
	 * 卸载应用包
	 */
	void undeploy(String packageKey);

}
