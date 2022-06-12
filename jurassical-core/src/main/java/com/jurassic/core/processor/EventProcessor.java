package com.jurassic.core.processor;

import com.jurassic.core.event.Event;
import com.jurassic.core.handler.AbstractHandler;

import java.util.concurrent.ExecutorService;

/**
 * 事件的处理器
 * 承载独立的处理器,执行相应的业务逻辑
 * 
 * @author yzhu
 * @param <T>
 */
public interface EventProcessor<T extends Event>{

	/**
	 * 组件启动
	 */
	boolean start(ExecutorService es);

	/**
	 * 组件关闭,并确保所有该组件上的事件安全处理完毕
	 */
	void shutdown();

	/**
	 * 判断组件是否正常启动
	 * 正常运行的组件可以接受事件并交由处理器处理
	 */
	boolean isStarted();

	/**
	 * 判断组件是否有正在处理的工作
	 */
	boolean hasDealingEvent();

	/**
	 * 业务处理
	 */
	boolean doEvent(T event);

	/**
	 * 获得处理器
	 */
	AbstractHandler getHandler();

}
