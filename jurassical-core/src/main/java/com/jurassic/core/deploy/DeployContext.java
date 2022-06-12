package com.jurassic.core.deploy;

import com.jurassic.core.annotation.Bean;
import com.jurassic.core.annotation.Component;
import com.jurassic.core.annotation.Configuration;
import com.jurassic.core.annotation.Param;
import com.jurassic.core.bus.Constant;
import com.jurassic.core.bus.EventBus;
import com.jurassic.core.classloader.ComponentClassLoader;
import com.jurassic.core.classloader.JurassicRootClassLoader;
import com.jurassic.core.compiler.element.ProgressElement;
import com.jurassic.core.dataflow.filter.Filter;
import com.jurassic.core.dataflow.handler.BaseEventHandler;
import com.jurassic.core.dataflow.handler.BatchEventHandler;
import com.jurassic.core.dataflow.handler.EventHandler;
import com.jurassic.core.dataflow.worker.BatchEventWorker;
import com.jurassic.core.dataflow.worker.SingleEventWorker;
import com.jurassic.core.event.Event;
import com.jurassic.core.event.EventMonitor;
import com.jurassic.core.global.GlobalInstRegisterTable;
import com.jurassic.core.handler.AbstractHandler;
import com.jurassic.core.handler.DeployProperties;
import com.jurassic.core.handler.HandlerConfig;
import com.jurassic.core.processor.EventProcessor;
import com.jurassic.core.processor.WorkHandler;
import com.jurassic.core.processor.impl.BatchEventProcessor;
import com.jurassic.core.processor.impl.SingleEventProcessor;
import com.jurassic.core.progress.Progress;
import com.jurassic.core.progress.ProgressMonitor;
import com.jurassic.core.progress.factory.ProgressFactory;
import com.jurassic.core.progress.factory.ProgressTemplate;
import com.jurassic.core.progress.handler.BatchTaskHandler;
import com.jurassic.core.progress.handler.EPU;
import com.jurassic.core.progress.handler.TaskHandler;
import com.jurassic.core.progress.handler.pin.Pin;
import com.jurassic.core.progress.worker.BatchTaskWorker;
import com.jurassic.core.progress.worker.EPUWorker;
import com.jurassic.core.progress.worker.SingleTaskWorker;
import com.jurassic.core.resource.AttachedResourceTbl;
import com.jurassic.core.resource.ResourceFactoryTbl;
import com.jurassic.core.resource.ResourceHandlerFactory;
import com.jurassic.core.thread.ComponentThreadPool;
import com.jurassic.core.util.TypedUtil;
import com.jurassic.core.compiler.Compiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

/**
 * 组件包发布上下文
 * 上下文主要维护了其中所有的处理器的生命周期
 * 完成流程的创建和事件的触发
 * 它的功能作为总线的一个缩影，由总线统一调度
 * 完成总线上事件的发布和处理
 *
 * 基本配置规则：
 * ProcessTemplate配置：
 * @Configuration
 * class ...{
 *     @Bean
 *     ProcessTemplate ...(...);
 * }
 * Handler配置
 * @Component
 * class ... extends AbstractHandler{
 *     ...
 * }
 * @Configuration
 * class ...{
 *     @Bean
 *     Map<String, HandlerConfig> ...();
 * }
 * Filter配置
 * @Component
 * class ... implements Filter{
 *     ...
 * }
 * 线程池配置
 * @Configuration
 * class ...{
 *     @Bean
 *     ThreadFactory ...();
 * }
 * DeployAware和UnDeployAware的配置
 * @Configuration
 * class ...{
 *     @Bean
 *     DeployAware(UnDeployAware) ...();
 * }
 * ProgressMonitor和EventMonitor的配置
 * @Configuration
 * class ...{
 *     @Bean
 *     Map<String, ProgressMonitor(EventMonitor)> ...();
 * }
 * 
 * @author yzhu
 * 
 */
public class DeployContext {
	private static final Logger logger = LoggerFactory.getLogger(DeployContext.class);

	private final String _packageKey;// 包的key
	private volatile boolean _deploy = false;
	private ResourceHandlerFactory _resourceHandlerFactory;
	private ComponentClassLoader _classLoader;// 类加载器
	private DeployProperties _properties;// 配置信息
	private ComponentThreadPool _threadPool;// 线程池
	// 流程工厂
	private ProgressFactory _progressFactory = new ProgressFactory();
	// 发布的处理器
	private Map<String, EventProcessor<?>> _processors = new HashMap<>();
	// 包发布和卸载时的感知事件
	private List<DeployAware> _deployAwares = new ArrayList<>();
	private List<UnDeployAware> _undeployAwares = new ArrayList<>();
	// 流程监控接口
	private Map<String, ProgressMonitor> _progressMonitors = new HashMap<>();
	// 事件监控接口
	private Map<String, EventMonitor> _eventMonitors = new HashMap<>();

	public DeployContext(String packageKey, String rootFilePath, URL[] urls) {
		this._packageKey = packageKey;
		// 生成类加载器
		this._classLoader = GlobalInstRegisterTable
				.getInst(JurassicRootClassLoader.GLOBAL_KEY,
						JurassicRootClassLoader.class)
				.bindComponentLoader(packageKey, urls);
		// 构建资源句柄工厂
		this._resourceHandlerFactory =
			new ResourceHandlerFactory(new AttachedResourceTbl(),
				GlobalInstRegisterTable.getInst(
						ResourceFactoryTbl.GLOBAL_KEY, ResourceFactoryTbl.class));

		// 加载配置
		String fileName =
				rootFilePath + "/" + DeployProperties.CONF_NAME;
		File file = new File(fileName);
		if (!file.exists()) {
			// 如果配置文件不存在，则部署失败
			logger.warn("there is no deploy.conf in " + rootFilePath);
		}
		this._properties = new DeployProperties(file);
	}

	public ComponentClassLoader getClassLoader() {
		return this._classLoader;
	}

	/**
	 * 获得所有的配置属性
	 */
	public DeployProperties getProperties() {
		return this._properties;
	}

	/**
	 * 注册bean
	 */
	@SuppressWarnings("unchecked")
	private void registerBean(Map<String, Object> tbl, String key, Object bean) {
		if (logger.isDebugEnabled())
			logger.debug("register bean[" + key + "]," + bean.getClass());
		Object orignal = tbl.get(key);
		if (orignal == null) {
			// 加入key-object
			tbl.put(key, bean);
		} else {
			if (orignal instanceof List) {
				// 如果之前已经注册过相应的object，则追加在列表后面
				List<Object> list = (List<Object>) orignal;
				list.add(bean);
			} else {
				// 如果之前只注册过一个object，则将其与instance组成list
				List<Object> list = new ArrayList<>();
				list.add(orignal);
				list.add(bean);
				tbl.put(key, list);
			}
		}
	}

	/**
	 * 查找参数的@Param注解
	 */
	private Param getParamAnnotation(Annotation[] annotations) {
		if (annotations == null)
			return null;
		for (Annotation annotation : annotations) {
			if (annotation.annotationType().equals(Param.class)) {
				return (Param) annotation;
			}
		}
		return null;
	}

	/**
	 * 注册@Configuration的@Bean
	 */
	private boolean registerConfiguration(
			Map<String, Object> tbl, Object configuration,
			Method method, Queue<Object[]> delayBeans) throws Exception{
		Bean beanAnnotation = method.getAnnotation(Bean.class);

		// 该public方法定义了@Bean
		Type[] typeOfParams = method.getGenericParameterTypes();
		Annotation[][] annotationOfParameters = method.getParameterAnnotations();

		if (typeOfParams.length == 0) {
			// 不需要参数的配置方法可以直接调用该方法产生对应的Bean
			Object instance = method.invoke(configuration);
			// 如果@Bean定义了name属性，则使用其为Bean的key
			// 否则使用类名作key
			if (beanAnnotation != null) {
				String key = !"".equals(beanAnnotation.name()) ?
						beanAnnotation.name() : method.getGenericReturnType().getTypeName();
				this.registerBean(tbl, key, instance);
			}
			return true;
		} else {
			// 根据参数类型查找已经注册的bean
			Object[] params = new Object[typeOfParams.length];
			boolean delay = false;// 标记该方法是否需要延后加载
			for (int i = 0; i < params.length; i++) {
				Param paramAnnotation = this.getParamAnnotation(annotationOfParameters[i]);
				// 如果参数定义了@Param，则使用其name，否则使用类型
				Object bean = tbl.get(paramAnnotation != null ?
						paramAnnotation.name() : typeOfParams[i].getTypeName());
				if (bean != null) {
					params[i] = bean;
				} else {
					// 如果参数需要的bean还没有注册，暂时搁置该方法的@Bean生成过程
					delayBeans.add(
							new Object[]{configuration, method});
					if (logger.isDebugEnabled()) {
						logger.debug("delay config method[" +
								configuration.getClass().getTypeName() + "," +
								method.getName() + "] because param[" +
								typeOfParams[i].getTypeName() + "] not available");
					}
					delay = true;
					break;// 扫描下一个方法
				}
			}
			if (!delay) {
				// 参数准备好，可以执行method注册对应的Bean
				Object bean = method.invoke(configuration, params);
				if (beanAnnotation != null) {
					String key = !"".equals(beanAnnotation.name()) ?
							beanAnnotation.name() : method.getGenericReturnType().getTypeName();
					this.registerBean(tbl, key, bean);
				}
				return true;
			}
		}

		return false;
	}

	/**
	 * 部署组件包的实例化
	 */
	private Map<String, Object> registerBeans(List<Class<?>> classes) {
		try {
			Map<String, Object> tbl = new HashMap<>();
			// 注册总线
			this.registerBean(tbl, EventBus.class.getTypeName(),
					GlobalInstRegisterTable.getInst(
							EventBus.GLOBAL_KEY, EventBus.class
					));
			// 注册资源句柄工厂
			this.registerBean(tbl, ResourceHandlerFactory.class.getTypeName(),
					this._resourceHandlerFactory);
			// 注册发布的上下文
			this.registerBean(tbl, this.getClass().getTypeName(), this);
			// @Configuration类的实例
			List<Object> configurationObjects = new ArrayList<>();

			for (Class<?> clz : classes) {
				Component componentAnnotation = clz.getAnnotation(Component.class);
				Configuration configurationAnnotation = clz.getAnnotation(Configuration.class);
				if (componentAnnotation != null) {
					// @Component标记的class
					Object bean = clz.newInstance();
					// 如果@Configuration定义了name属性，则使用其为Bean的key
					// 否则使用类名作key
					String key = !"".equals(componentAnnotation.name()) ?
							componentAnnotation.name() : clz.getTypeName();
					this.registerBean(tbl, key, bean);
				} else if (configurationAnnotation != null) {
					// @Configuration标记的class
					configurationObjects.add(clz.newInstance());
				}
			}

			// 执行@Conguration类的各个标记@Bean的方法，生成配置Bean
			Queue<Object[]> delayBeans = new LinkedList<>();// 用于记录暂时无法实例化的method
			for (Object configuration : configurationObjects) {
				Class<?> clz = configuration.getClass();
				Method[] methods = clz.getDeclaredMethods();
				for (Method method : methods) {
					this.registerConfiguration(tbl, configuration, method, delayBeans);
				}
			}
			// 反复扫描lazyBean直至全部注册完毕
			int size = delayBeans.size();
			int num = 0;
			while (!delayBeans.isEmpty()) {
				Object[] dataOfInvoke = delayBeans.poll();
				if (this.registerConfiguration(tbl, dataOfInvoke[0],
						(Method) dataOfInvoke[1], delayBeans)) {
					// 如果可以注册bean了，对delayBean中的数量减1，同时清空num计数
					size--;
					num = 0;
				} else {
					// num计数加1，表示从上一次有method生成了Bean之后，累计无法加载的Bean数量
					num++;
				}
				if (size == num) {
					// 如果累计的无法加载的Bean数量达到delayBeans的大小
					// 则说明lazyBeans中的所有method都无法注册
					// Bean的注册过程将无法完成
					break;
				}
			}
			if (!delayBeans.isEmpty()){
				// 无法注册完所有的Bean，则加载失败
				return null;
			}
			return tbl;
		} catch (Throwable ex) {
			logger.error(ex.getMessage(), ex);
			return null;
		}
	}

	/**
	 * 配置ProgressTemplate
	 */
	@SuppressWarnings({"rawtypes","unchecked"})
	private void registerProcessTemplate(Map<String, Object> registeredBeans) {
		Object bean = registeredBeans.get(ProgressTemplate.class.getTypeName());
		if (bean != null) {
			// 根据ProgressTemplate的类名作key进行搜索
			if (bean instanceof List) {
				// 如果配置了多个ProgressTemplate
				List<ProgressTemplate> templates = (List<ProgressTemplate>) bean;
				for (ProgressTemplate template : templates) {
					this._progressFactory.addTemplate(template);
				}
			} else if (bean instanceof ProgressTemplate) {
				// 类名配置了一个ProgressTemplate
				ProgressTemplate template = (ProgressTemplate) bean;
				this._progressFactory.addTemplate(template);
			}
		}
	}

	/**
	 * 配置Handler
	 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	private void registerHandler(Map<String, Object> registeredBeans) {
		// 找到Handler的配置信息
		// 配置信息类型为Map<String, HandlerConfig>
		// 其中key为Handler的name，value为配置数据
		String typeName =
				new TypedUtil<Map<String, HandlerConfig>>() {
					@Override
					public String getActualTypeName() {
						Type typeOfSuperClz = this.getClass().getGenericSuperclass();
						ParameterizedType pt = (ParameterizedType)typeOfSuperClz;
						return pt.getActualTypeArguments()[0].getTypeName();
					}
				}.getActualTypeName();
		Object configOfHandlers = registeredBeans.get(typeName);
		Map<String, HandlerConfig> combindedConfigOfHandlers = new HashMap<>();
		// 将所有的配置合并起来
		if (configOfHandlers != null) {
			if (configOfHandlers instanceof List) {
				// @Configuration配置了多组Handler
				for (Map<String, HandlerConfig> configs : (List<Map<String, HandlerConfig>>) configOfHandlers) {
					combindedConfigOfHandlers.putAll(configs);
				}
			} else {
				// @Configuration配置了1组Handler
				Map<String, HandlerConfig> configs = (Map<String, HandlerConfig>) configOfHandlers;
				combindedConfigOfHandlers.putAll(configs);
			}
		}
		// 扫描所有注册的Handler
		Map<String, AbstractHandler> handlers = new HashMap<>();
		for (Map.Entry<String, Object> entry : registeredBeans.entrySet()) {
			Object bean = entry.getValue();
			if (bean instanceof AbstractHandler) {
				// bean为Handler定义
				AbstractHandler handler = (AbstractHandler) bean;
				// 配置Handler的key
				HandlerConfig config = combindedConfigOfHandlers.get(handler.getHandlerKey());
				if (config != null) {
					// 如果定义了配置，否则采用默认配置
					handler.config(config.getNumOfThread(), config.getPowerOfBuffer());
				}
				// 加载配置属性
				handler.loadProperties(this._properties);
				// 注入资源句柄
				handler.resourceInject(this._resourceHandlerFactory);
				handlers.put(handler.getHandlerKey(), handler);
			} else if (bean instanceof List) {
				for (Object object : (List) bean) {
					if (object instanceof AbstractHandler) {
						// bean为Handler定义
						AbstractHandler handler = (AbstractHandler) object;
						// 配置Handler的key
						HandlerConfig config = combindedConfigOfHandlers.get(handler.getHandlerKey());
						if (config != null) {
							// 如果定义了配置，否则采用默认配置
							handler.config(config.getNumOfThread(), config.getPowerOfBuffer());
						}
						// 加载配置属性
						handler.loadProperties(this._properties);
						// 注入资源句柄
						handler.resourceInject(this._resourceHandlerFactory);
						handlers.put(handler.getHandlerKey(), handler);
					}
				}
			}
		}
		// 扫描所有注册的Filter
		for (Map.Entry<String, Object> entry : registeredBeans.entrySet()) {
			Object bean = entry.getValue();
			if (bean instanceof Filter) {
				Filter filter = (Filter) bean;
				AbstractHandler handler = handlers.get(filter.getHandlerKey());
				if (handler instanceof BaseEventHandler) {
					// 找到对应的handler
					if (filter.isBefore()) {
						((BaseEventHandler) handler).addBeforeFilter(filter);
					} else {
						((BaseEventHandler) handler).addAfterFilter(filter);
					}
				}
			} else if (bean instanceof List) {
				for (Object object : (List) bean) {
					if (object instanceof Filter) {
						Filter filter = (Filter) object;
						AbstractHandler handler = handlers.get(filter.getHandlerKey());
						if (handler instanceof BaseEventHandler) {
							// 找到对应的handler
							if (filter.isBefore()) {
								((BaseEventHandler) handler).addBeforeFilter(filter);
							} else {
								((BaseEventHandler) handler).addAfterFilter(filter);
							}
						}
					}
				}
			}
		}
		// 将handlers对应实例化为Processors
		for (Map.Entry<String, AbstractHandler> entry : handlers.entrySet()) {
			String handlerKey = entry.getKey();
			AbstractHandler handler = entry.getValue();
			EventProcessor<?> processor = null;
			if (handler instanceof BatchTaskHandler) {
				// 批量任务处理器
				processor = new BatchEventProcessor(
						handler, new BatchTaskWorker(), handler.getPowerOfBuffer());
			} else if (handler instanceof TaskHandler) {
				// 单一任务处理器
				WorkHandler[] workers = new WorkHandler[handler.getNumOfThread()];
				for (int i = 0; i < workers.length; i++)
					workers[i] = new SingleTaskWorker();
				processor = new SingleEventProcessor(
						handler, workers, handler.getPowerOfBuffer());
			} else if (handler instanceof BatchEventHandler) {
				// 批量事件处理器
				processor = new BatchEventProcessor(
						handler, new BatchEventWorker(), handler.getPowerOfBuffer());
			} else if (handler instanceof EventHandler) {
				// 单一事件处理器
				WorkHandler[] workers = new WorkHandler[handler.getNumOfThread()];
				for (int i = 0; i < workers.length; i++)
					workers[i] = new SingleEventWorker();
				processor = new SingleEventProcessor(
						handler, workers, handler.getPowerOfBuffer());
			}
			if (processor != null) {
				this._processors.put(handlerKey, processor);
			}
		}
	}

	/**
	 * 配置ThreadPool
	 */
	private void initThreadPool(Map<String, Object> registeredBeans) {
		// 查询是否有配置ThreadFactory
		Object bean = registeredBeans.get(ThreadFactory.class.getTypeName());
		ThreadFactory threadFactory = bean != null ?
				(ThreadFactory) bean : new DefaultThreadFactory();
		// 构建专用的线程池,线程池的大小由各个handler配置决定
		int threadNum = 0;
		for (Map.Entry<String, EventProcessor<?>> entry : this._processors.entrySet()) {
			// 线程池的大小由所有的处理器的线程配置决定
			EventProcessor<?> processor = entry.getValue();
			threadNum += processor.getHandler().getNumOfThread();
		}
		logger.info("init threadpool, thread size:" + threadNum);
		this._threadPool = new ComponentThreadPool(threadNum > 0 ? threadNum : 1,
				threadFactory, this._classLoader,
				this._resourceHandlerFactory.getAttachedResourceTbl());
	}

	/**
	 * 启动processor
	 */
	private boolean startProcessors() {
		for (Map.Entry<String, EventProcessor<?>> entry : this._processors.entrySet()) {
			EventProcessor<?> processor = entry.getValue();
			// 启动Processor
			if (!processor.start(this._threadPool)) {
				logger.warn("start processor[" + entry.getKey() + "] fail");
				return false;
			}
		}
		return true;
	}

	/**
	 * 关闭Processor
	 */
	private void shutdownProcessors() {
		for (Map.Entry<String, EventProcessor<?>> entry : this._processors.entrySet()) {
			EventProcessor<?> processor = entry.getValue();
			// 关闭Processor
			if (processor.isStarted())
				processor.shutdown();
		}
	}

	/**
	 * 注册DeployAware接口
	 */
	@SuppressWarnings("unchecked")
	private void registerDeployAwares(Map<String, Object> registeredBeans) {
		Object bean = registeredBeans.get(DeployAware.class.getTypeName());
		if (bean != null) {
			if (bean instanceof List) {
				List<DeployAware> awares = (List<DeployAware>)bean;
				this._deployAwares.addAll(awares);
			} else {
				this._deployAwares.add((DeployAware) bean);
			}
		}
	}

	/**
	 * 注册UnDeployAware接口
	 */
	@SuppressWarnings("unchecked")
	private void registerUnDeployAwares(Map<String, Object> registeredBeans) {
		Object bean = registeredBeans.get(UnDeployAware.class.getTypeName());
		if (bean != null) {
			if (bean instanceof List) {
				List<UnDeployAware> awares = (List<UnDeployAware>)bean;
				this._undeployAwares.addAll(awares);
			} else {
				this._undeployAwares.add((UnDeployAware) bean);
			}
		}
	}

	/**
	 * 注册流程和事件的监听器
	 */
	@SuppressWarnings("unchecked")
	private void registerMonitor(Map<String, Object> registeredBeans) {
		String typeName =
				new TypedUtil<Map<String, ProgressMonitor>>() {
					@Override
					public String getActualTypeName() {
						Type typeOfSuperClz = this.getClass().getGenericSuperclass();
						ParameterizedType pt = (ParameterizedType)typeOfSuperClz;
						return pt.getActualTypeArguments()[0].getTypeName();
					}
				}.getActualTypeName();
		Object bean = registeredBeans.get(typeName);
		// 将所有的配置合并起来
		if (bean != null) {
			if (bean instanceof List) {
				// @Configuration配置了多组ProgressMonitor
				for (Map<String, ProgressMonitor> monitors : (List<Map<String, ProgressMonitor>>) bean) {
					this._progressMonitors.putAll(monitors);
				}
			} else {
				// @Configuration配置了1组ProgressMonitor
				Map<String, ProgressMonitor> monitors = (Map<String, ProgressMonitor>) bean;
				this._progressMonitors.putAll(monitors);
			}
		}
		typeName =
				new TypedUtil<Map<String, EventMonitor>>() {
					@Override
					public String getActualTypeName() {
						Type typeOfSuperClz = this.getClass().getGenericSuperclass();
						ParameterizedType pt = (ParameterizedType)typeOfSuperClz;
						return pt.getActualTypeArguments()[0].getTypeName();
					}
				}.getActualTypeName();
		bean = registeredBeans.get(typeName);
		// 将所有的配置合并起来
		if (bean != null) {
			if (bean instanceof List) {
				// @Configuration配置了多组ProgressMonitor
				for (Map<String, EventMonitor> monitors : (List<Map<String, EventMonitor>>) bean) {
					this._eventMonitors.putAll(monitors);
				}
			} else {
				// @Configuration配置了1组ProgressMonitor
				Map<String, EventMonitor> monitors = (Map<String, EventMonitor>) bean;
				this._eventMonitors.putAll(monitors);
			}
		}
	}

	/**
	 * 从指定路径加载流程模板
	 */
	private void loadProgressTemplateFromPath(String path) {
		List<URL> templateUrls = new ArrayList<>();
		if (path.startsWith("classpath:")) {
			// 从classpath上加载
			List<URL> urls = this._classLoader.getResourceFromPath(
					path.substring(10), (dir, name) -> name.endsWith(".xml"));
			if (urls != null && !urls.isEmpty()) {
				templateUrls.addAll(urls);
			}
		} else if (path.startsWith("file:")) {
			// 从文件目录加载
			File dir = new File(path.substring(5));
			if (dir.exists()) {
				// 获得progress-template目录下的所有文件对应url
				File[] files = dir.listFiles((dir1, name) -> name.endsWith(".xml"));
				if (files != null && files.length > 0) {
					for (File file : files) {
						try {
							templateUrls.add(new URL(
									"file", null, file.getAbsolutePath()));
						} catch (Throwable ex) {
							logger.warn("progress template file:"
									+ file.getAbsolutePath() + " load fail");
						}
					}
				}
			}
		}
		if (!templateUrls.isEmpty()) {
			for (URL url : templateUrls) {
				try{
					ProgressElement element = Compiler.parseProgress(url);
					ProgressTemplate template =
							Compiler.generateProgressTemplate(
									element, this._classLoader);
					if (logger.isDebugEnabled())
						logger.debug("load progress template:" + template.getProgressKey());
					this._progressFactory.addTemplate(template);
				} catch (Throwable ex) {
					logger.warn(ex.getMessage(), ex);
					logger.warn("progress template:" + url.getFile() + " load fail");
				}
			}
		}
	}

	/**
	 * 部署
	 */
	public boolean deploy() {
		if (this._deploy)
			return true;
		// 确保配置文件成功加载
		if (!this._properties.isLoaded()) {
			logger.warn("load package properties fail");
			return false;
		}
		// 加载@Component和@Configuration
		List<Class<?>> classes = this._classLoader
				.findClassByAnnotation(Component.class, Configuration.class);
		// 注册所有的@Component和@Bean
		Map<String, Object> registeredBeans = this.registerBeans(classes);
		if (registeredBeans == null) {
			logger.warn("initialize bean fail");
			return false;
		}
		// 配置ProgressTemplate
		this.registerProcessTemplate(registeredBeans);
		// 通过配置文件加载ProgressTemplate
		String path = this._properties.getProperty("template.path");
		if (path != null) {
			this.loadProgressTemplateFromPath(path);
		}
		if (this._progressFactory.hasProgressTemplate()) {
			// 如果模块加载了流程模板，则默认生成一个EPU
			EventProcessor processor = new BatchEventProcessor(
					new EPU(), new EPUWorker(), Constant.DEFAULT_EPU_POWER);
			this._processors.put(processor.getHandler().getHandlerKey(),
					processor);
		}
		// 配置Handler
		this.registerHandler(registeredBeans);
		// 配置流程和事件的monitor
		this.registerMonitor(registeredBeans);
		// 配置ThreadPool
		this.initThreadPool(registeredBeans);
		// 配置DeployAware和UnDeployAware
		this.registerDeployAwares(registeredBeans);
		this.registerUnDeployAwares(registeredBeans);

		// 启动Processor
		if (!this.startProcessors()) {
			// 启动失败需要关闭所有已经启动的Processor
			this.shutdownProcessors();
			this._threadPool.shutdown();
			this.destroyResource();
			this._deploy = false;
			return false;
		}
		this._deploy = true;

		return true;
	}

	public void start() {
		// 执行DeployAware的startup方法
		for (DeployAware aware : this._deployAwares) {
			aware.startup(this);
		}
	}

	/**
	 * 卸载
	 */
	public void undeploy() {
		if (!this._deploy)
			return;
		// 直接标记发布状态，组织后续的操作
		this._deploy = false;
		logger.info("undeploy package " + this._packageKey);
		// 停止所有业务流程的总入口,这样不会有新的业务流程启动
		// 同时等待现有已经运行中的业务流程执行完毕
		logger.info("waiting for all proc finish");
		while (this._progressFactory.hasRunningProgress()) {
			LockSupport.parkNanos(1000000L);
		}
		// 执行UnDeployAware的shutdown方法
		for (UnDeployAware aware : this._undeployAwares) {
			aware.shutdown(this);
		}
		// 关闭所有的Processor
		this.shutdownProcessors();
		this._threadPool.shutdown();
		this.destroyResource();
		logger.info("shutdown all processors");
	}

	/**
	 * 销毁资源
	 */
	private void destroyResource() {
		this._resourceHandlerFactory = null;
		this._classLoader = null;
		this._properties.destroyResource();
		this._properties = null;
		this._threadPool = null;
		this._progressFactory.destroyResource();
		this._progressFactory = null;
		this._processors.clear();
		this._processors = null;
		this._deployAwares.clear();
		this._deployAwares = null;
		this._undeployAwares.clear();
		this._undeployAwares = null;
		this._progressMonitors.clear();
		this._progressMonitors = null;
		this._eventMonitors.clear();
		this._eventMonitors = null;
	}

	public boolean isDeployed() {
		return this._deploy;
	}

	public String getPackageKey() {
		return this._packageKey;
	}

	/**
	 * 获得依赖包
	 * 包的deploy过程中会先发布所依赖的包
	 * 再deploy本包
	 */
	public String[] getDependPackages() {
		String depends = this._properties.getProperty("depend");
		if (depends == null || "".equals(depends))
			return null;

		return depends.split(",");
	}

	/**
	 * 创建流程
	 * 由流程模板负责创建
	 */
	public Progress createProgress(
			String progressKey, Pin[] params, Progress parent) {
		if (!this._deploy) {
			// 包没有deploy无法创建流程
			logger.warn("package " + this._packageKey + " has undeployed");
			return null;
		}
		// 使用流程工厂创建流程
		Progress proc = this._progressFactory.createProgress(
				this._packageKey, progressKey, params, parent);
		// 如果需要监控流程，为这个新建的流程注入monitor
		if (proc != null) {
			ProgressMonitor monitor = this._progressMonitors.get(progressKey);
			if (monitor != null) {
				proc.setMonitor(monitor);
			}
		} else {
			logger.warn("create progress[" + this._packageKey + ","
					+ progressKey + "] fail");
		}
		return proc;
	}

	/**
	 * 创建事件
	 * 该方法用在一些暴露的service直接对应单一的事件
	 */
	public Event createEvent(String eventKey, Object[] params) {
		if (!this._deploy) {
			// 包没有deploy无法创建流程
			logger.warn("package " + this._packageKey + " has undeployed");
			return null;
		}
		// 查找处理器
		EventProcessor<?> processor = this._processors.get(eventKey);
		if (processor == null) {
			logger.warn("processor[" + eventKey + "] not exist");
			return null;
		}
		AbstractHandler handler = processor.getHandler();
		if (handler instanceof BaseEventHandler) {
			// 该操作只对普通事件有效，对于流程相关的task还是采用createProc方法创建
			BaseEventHandler<?> baseEventHandler = (BaseEventHandler<?>) handler;
			Event event = baseEventHandler.createEvent(params);
			if (event != null) {
				EventMonitor monitor = this._eventMonitors.get(eventKey);
				if (monitor != null)
					event.setMonitor(monitor);
			}
			return event;
		}
		return null;
	}


	/**
	 * 处理事件
	 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	public boolean doEvent(String handler, Event event) {
		if (!this._deploy) {
			// 包没有deploy处理事件
			logger.warn("package " + this._packageKey + " has undeployed");
			return false;
		}
		if (handler == null) {
			// 如果handler为空，表示广播该包下的所有处理器
			for (Map.Entry<String, EventProcessor<?>> entry : this._processors.entrySet()) {
				EventProcessor processor = entry.getValue();
				try {
					processor.doEvent(event);
				} catch (Throwable ex) {
					logger.error(ex.getMessage(), ex);
				}
			}
			return true;
		}
		// 查找特定的处理器
		EventProcessor processor = this._processors.get(handler);
		if (processor == null || !processor.isStarted()) {
			logger.warn("context[" + this._packageKey + "],process[" + handler + "] not exist");
			return false;
		}
		try {
			return processor.doEvent(event);
		} catch (Throwable ex) {
			logger.error(ex.getMessage(), ex);
			return false;
		}
	}

	/**
	 * 组件安装包使用的线程工厂
	 * 负责为线程池生成指定的线程
	 * 工厂类为每个生成的线程指定特殊的名字前缀
	 */
	private class DefaultThreadFactory implements ThreadFactory {
		private final ThreadGroup _threadGroup;// 线程组
		// 计数器，负责给每一个线程起名字
		private final AtomicInteger threadNumber = new AtomicInteger(1);
		// 线程名字的前缀，采用包key+"-thread-"
		private final String namePrefix;

		public DefaultThreadFactory() {
			SecurityManager s = System.getSecurityManager();
			this._threadGroup = (s != null) ? s.getThreadGroup() : Thread
					.currentThread().getThreadGroup();
			this.namePrefix = _packageKey + "-thread-";
		}

		/**
		 * 创建线程
		 */
		public Thread newThread(Runnable runnable) {
			Thread thread = new Thread(this._threadGroup, runnable, this.namePrefix
					+ this.threadNumber.getAndIncrement(), 0);
			if (thread.isDaemon())
				thread.setDaemon(false);
			if (thread.getPriority() != Thread.NORM_PRIORITY)
				thread.setPriority(Thread.NORM_PRIORITY);
			return thread;
		}
	}

}
