package com.jurassic.core.bus.impl;

import com.jurassic.core.bus.Constant;
import com.jurassic.core.bus.EventBus;
import com.jurassic.core.bus.error.ComponentNotFoundErr;
import com.jurassic.core.bus.error.EventBusNotStartedErr;
import com.jurassic.core.bus.error.EventNotFiredErr;
import com.jurassic.core.classloader.ComponentClassLoader;
import com.jurassic.core.classloader.JurassicRootClassLoader;
import com.jurassic.core.dataflow.handler.impl.ReflectionHandler;
import com.jurassic.core.dataflow.handler.impl.StaticsHandler;
import com.jurassic.core.dataflow.worker.BatchEventWorker;
import com.jurassic.core.dataflow.worker.SingleEventWorker;
import com.jurassic.core.event.ReflectionEvent;
import com.jurassic.core.processor.WorkHandler;
import com.jurassic.core.progress.handler.impl.SubProgressInvokeHandler;
import com.jurassic.core.progress.handler.pin.Pin;
import com.jurassic.core.progress.task.EndTask;
import com.jurassic.core.progress.task.StartTask;
import com.jurassic.core.progress.task.SubProgressInvokeTask;
import com.jurassic.core.progress.worker.BatchTaskWorker;
import com.jurassic.core.processor.EventProcessor;
import com.jurassic.core.processor.impl.BatchEventProcessor;
import com.jurassic.core.processor.impl.SingleEventProcessor;
import com.jurassic.core.deploy.DeployContext;
import com.jurassic.core.progress.handler.impl.EndHandler;
import com.jurassic.core.progress.handler.impl.StartHandler;
import com.jurassic.core.event.FireDelayEvent;
import com.jurassic.core.event.Event;
import com.jurassic.core.global.GlobalInstRegisterTable;
import com.jurassic.core.progress.Progress;
import com.jurassic.core.progress.manager.ProgressManager;
import com.jurassic.core.progress.worker.SingleTaskWorker;
import com.jurassic.core.resource.ResourceFactory;
import com.jurassic.core.resource.ResourceFactoryTbl;
import com.jurassic.core.util.CopyOnWriteMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * EventBus的系统默认实现
 * 
 * @author yzhu
 */
public class EBus implements EventBus, ResourceFactoryTbl, ProgressManager {
	private static final Logger logger = LoggerFactory.getLogger(EBus.class);

	// 总线使用的类加载器
	private final JurassicRootClassLoader _rootClassLoader;
	// 启动标志
	private volatile boolean _started = false;
	// 业务处理器注册表
	// 总线上有4个默认的处理器:
	// 1.start处理器,处理所有流程的*_start事件
	// 2.end处理器,处理所有流程的*_end事件
	// 3.statics处理器,处理每一个事件的执行时间信息,并加以统计
	// 4.reflection处理器,处理使用反射操作执行的事件
	// 这4个处理器作为系统默认的内置处理器,在总线启动和关闭的时候进行维护
	public static final String COMPONENT_KEY_START = "start";
	public static final String COMPONENT_KEY_END = "end";
	public static final String COMPONENT_KEY_STATICS = "statics";
	public static final String COMPONENT_KEY_REFLECTOR = "reflection";
	// 在总线上部署的各个包的上下文
	private final CopyOnWriteMap<String, DeployContext> _contexts
			= new CopyOnWriteMap<>();
	// 守护线程,负责向总线上的一些公共组件提供线程支持
	private ExecutorService _daemon;
	// 服务器内置的资源工厂表
	private Map<String, ResourceFactory<?>> _resourceFactoryTbl;
	private Timer _timer;// 时钟

	public EBus(JurassicRootClassLoader clzLoader) {
		this._rootClassLoader = clzLoader;
		// EventBus除了扮演总线的角色之外，还同时扮演流程管理器和资源管理器的角色
		GlobalInstRegisterTable.register(EventBus.GLOBAL_KEY, this);
		GlobalInstRegisterTable.register(ProgressManager.GLOBAL_KEY, this);
		GlobalInstRegisterTable.register(ResourceFactoryTbl.GLOBAL_KEY, this);
	}

	public void schedule(Event event){
		// event.getScheduleSpan()后触发事件
		this._timer.schedule(new TimerTask() {
			public void run() {
				event.reset();
				try {
					fire(event.getPackageKey(), event.getEventKey(), event);
				} catch (Throwable ignored) {}
			}
		}, event.getScheduleSpan());
	}

	public void fire(String packageKey, String handlerKey, Event event) throws EventBusNotStartedErr,
			ComponentNotFoundErr, EventNotFiredErr {
		if (!this._started) {
			// 总线关闭的时候不接受任何事件的触发
			logger.error("event bus not started");
			throw new EventBusNotStartedErr();
		}
		// 对于*start,*end,statics,reflect,sub_progress_invoke特定的事件
		// 找对应的processor进行处理
		if (handlerKey.endsWith(StartTask.START_SUFFIX)) {
			if (!this._starter.doEvent((StartTask)event)) {
				logger.error("event " + event + " not deal");
				throw new EventNotFiredErr("event not fire");
			}
			return;
		} else if (handlerKey.endsWith(EndTask.END_SUFFIX)) {
			if (!this._ender.doEvent((EndTask)event)) {
				logger.error("event " + event + " not deal");
				throw new EventNotFiredErr("event not fire");
			}
			return;
		} else if (handlerKey.equals(EBus.COMPONENT_KEY_STATICS)) {
			if (!this._statics.doEvent(event)) {
				logger.error("event " + event + " not deal");
				throw new EventNotFiredErr("event not fire");
			}
			return;
		} else if (handlerKey.equals(EBus.COMPONENT_KEY_REFLECTOR)) {
			if (!this._reflector.doEvent((ReflectionEvent) event)) {
				logger.error("event " + event + " not deal");
				throw new EventNotFiredErr("event not fire");
			}
			return;
		} else if (handlerKey.equals(SubProgressInvokeTask.KEY)) {
			if (!this._subProgressInvoker.doEvent((SubProgressInvokeTask) event)) {
				logger.error("event " + event + " not deal");
				throw new EventNotFiredErr("event not fire");
			}
		}
		if (packageKey != null && !"".equals(packageKey)) {
			// 对于指定了package的事件，则定向进行点对点的事件触发
			DeployContext context = this._contexts.get(packageKey);
			if (context == null) {
				logger.error("event " + event + " not deal");
				throw new EventNotFiredErr("event not fire");
			}
			if (!context.doEvent(handlerKey, event)) {
				logger.error("event " + event + " not deal");
				throw new EventNotFiredErr("event not fire");
			}
		} else {
			// 未指定package则进行所有包的广播操作
			Map<String, DeployContext> map = this._contexts.toMap();
			for (String key : map.keySet()) {
				DeployContext context = map.get(key);
				context.doEvent(handlerKey, event);
			}
		}
	}

	public synchronized void shutdown() {
		if (!this._started)
			return;

		// started设置成false只对后续还要进行fire操作拦截
		// 对正在进行fire操作的线程没有影响
		this._started = false;

		// 以部署包为单位,逐个undeploy
		String[] keys = this._contexts.toMap()
				.keySet().toArray(new String[0]);
		for (String key : keys) {
			this.undeploy(key);
		}

		// 关闭start，end，statics,reflect四个内部处理器
		logger.info("shutdown start processor");
		this._starter.shutdown();
		logger.info("shutdown end processor");
		this._ender.shutdown();
		logger.info("shutdown statics processor");
		this._statics.shutdown();
		logger.info("shutdown reflect processor");
		this._reflector.shutdown();
		logger.info("shutdown sub progress invoker progress");
		this._subProgressInvoker.shutdown();

		// 关闭总线使用的内部线程
		this._daemon.shutdown();
		this._timer.cancel();

		logger.info("event bus shutdown");
	}

	/**
	 * 构建内部start处理器
	 */
	private EventProcessor<StartTask> _starter;
	private void addStarter() {
		this._starter = new BatchEventProcessor<>(
				new StartHandler(),
				new BatchTaskWorker<>(),
				Constant.DRPT_DATA_SIZE_POWER);
		logger.info("start starter processor");
		this._starter.start(this._daemon);
	}

	/**
	 * 构建内部end处理器
	 */
	private EventProcessor<EndTask> _ender;
	private void addEnder() {
		this._ender = new BatchEventProcessor<>(
				new EndHandler(),
				new BatchTaskWorker<>(),
				Constant.DRPT_DATA_SIZE_POWER);
		logger.info("start ender processor");
		this._ender.start(this._daemon);
	}

	/**
	 * 构建内部statics处理器
	 */
	private EventProcessor<Event> _statics;
	private void addStatics() {
		this._statics = new BatchEventProcessor<>(
				new StaticsHandler(),
				new BatchEventWorker<>(),
				Constant.DRPT_DATA_SIZE_POWER);
		logger.info("start statics processor");
		this._statics.start(this._daemon);
	}

	/**
	 * 构建内部的reflector处理器
	 */
	private EventProcessor<ReflectionEvent> _reflector;
	@SuppressWarnings("unchecked")
	private void addReflector() {
		this._reflector = new SingleEventProcessor<ReflectionEvent>(
				new ReflectionHandler(),
				new WorkHandler[]{new SingleEventWorker<>()},
				Constant.DRPT_DATA_SIZE_POWER);
		logger.info("start reflector processor");
		this._reflector.start(this._daemon);
	}

	/**
	 * 构建内部的SubProgressInvoke处理器
	 */
	private EventProcessor<SubProgressInvokeTask> _subProgressInvoker;
	@SuppressWarnings({"rawtypes", "unchecked"})
	private void addSubProgressInvoker() {
		this._subProgressInvoker = new SingleEventProcessor<SubProgressInvokeTask>(
				new SubProgressInvokeHandler(),
				new WorkHandler[]{new SingleTaskWorker()},
				Constant.DRPT_DATA_SIZE_POWER);
		logger.info("start sub-progress invoker");
		this._subProgressInvoker.start(this._daemon);
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public synchronized boolean start() {
		if (this._started)
			return true;
		this._daemon = Executors.newCachedThreadPool();
		logger.info("start event bus");
		// 初始化资源配置表
		this._resourceFactoryTbl = new HashMap<>();
		// 构建内部的start，end，statics,reflect,sub-progress-invoke处理器
		this.addStarter();
		this.addEnder();
		this.addStatics();
		this.addReflector();
		this.addSubProgressInvoker();
		this._started = true;
		// 启动timer
		this._timer = new Timer("timer");
		// 1ms间隔的timer事件，发送给所有的processor，
		this._timer.schedule(this._delayTask, 1000, 1);

		return true;
	}
	private TimerTask _delayTask = new TimerTask() {
		public void run() {
			try {
				// 向所有的部署包发送DelayFireEvent
				// 让各个包上的processor处理各个组件上的延迟事件
				Map<String, DeployContext> map = _contexts.toMap();
				for (String key : map.keySet()) {
					DeployContext context = map.get(key);
					context.doEvent(null, FireDelayEvent.instance);
				}
				// 向内部的组件发送FireDelayEvent,触发Processor处理各自的delay事件
				((EventProcessor) _starter).doEvent(FireDelayEvent.instance);
				((EventProcessor) _ender).doEvent(FireDelayEvent.instance);
				_statics.doEvent(FireDelayEvent.instance);
				((EventProcessor) _reflector).doEvent(FireDelayEvent.instance);
				((EventProcessor) _subProgressInvoker).doEvent(FireDelayEvent.instance);
			} catch (Throwable ex) {
				logger.warn(ex.getMessage());
			}
		}
	};

	public synchronized boolean deploy(DeployContext context) {
		if (context.deploy()) {
			// 部署组件包
			this._contexts.set(context.getPackageKey(), context);
			this._contexts.flush();
			context.start();
			return true;
		} else {
			return false;
		}
	}

	public synchronized void undeploy(String packageKey) {
		DeployContext context = this._contexts.remove(packageKey);
		if (context != null && context.isDeployed()) {
			this._contexts.flush();
			// 撤销组件包
			context.undeploy();
			// 撤销根类加载器下的组件包类加载器
			this._rootClassLoader.unbindComponentLoader(packageKey);
		}
	}

	public void loadResourceFactory(String classOfFactory, String id,
									Map<String, String> params) throws Throwable {
		// 获得资源工厂class
		ComponentClassLoader commonLibLoader = this._rootClassLoader.getCommonLibLoader();
		Class<?> _class = commonLibLoader.loadClass(classOfFactory);
		ResourceFactory<?> factory = (ResourceFactory<?>) _class.newInstance();
		// 初始化资源工厂
		factory.init(params);
		this._resourceFactoryTbl.put(id, factory);
	}

	public ResourceFactory<?> getResourceFactory(String id) {
		return this._resourceFactoryTbl.get(id);
	}

	public Progress createProgress(String packageKey,
								   String progressKey,
								   Pin[] params,
								   Progress parent){
		DeployContext context = this._contexts.get(packageKey);
		if (context == null) {
			return null;
		}
		// 使用发布的组件包上下文创建流程
		return context.createProgress(progressKey, params, parent);
	}

	public Event createEvent(String packageKey, String eventKey, Object[] params) {
		DeployContext context = this._contexts.get(packageKey);
		if (context == null) {
			return null;
		}
		return context.createEvent(eventKey, params);
	}
}
