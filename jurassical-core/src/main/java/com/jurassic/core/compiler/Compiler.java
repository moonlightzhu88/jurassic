package com.jurassic.core.compiler;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.*;

import com.jurassic.core.compiler.element.*;
import com.jurassic.core.classloader.ComponentClassLoader;
import com.jurassic.core.compiler.express.DataNode;
import com.jurassic.core.compiler.express.ExpressNode;
import com.jurassic.core.compiler.express.Node;
import com.jurassic.core.compiler.express.Variable;
import com.jurassic.core.notification.ResultNotification;
import com.jurassic.core.progress.Progress;
import com.jurassic.core.progress.factory.ProgressTemplate;
import com.jurassic.core.progress.handler.pin.*;
import com.jurassic.core.progress.handler.pin.express.Express;
import com.jurassic.core.progress.task.JumpEndTask;
import com.jurassic.core.progress.task.JumpStartTask;
import com.jurassic.core.progress.task.SubProgressInvokeTask;
import com.jurassic.core.progress.task.Task;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * 流程配置文件解析
 * 
 * @author yzhu
 * 
 */
public class Compiler {

	/**
	 * 解析<input>和<output>
	 */
	private static ProgressParamElement parseProgressParam(
			Element element, boolean input)
			throws ParserException{
		ProgressParamElement progressParamElement = new ProgressParamElement();
		Attribute name = element.attribute("name");
		Attribute type = element.attribute("type");
		Attribute desc = element.attribute("desc");
		if (type == null)
			throw new ParserException("invalid <input/> or <output/>");
		progressParamElement.setType(type.getText());
		if (desc != null)
			progressParamElement.setDesc(desc.getText());
		if (name != null)
			progressParamElement.setName(name.getText());
		progressParamElement.setInputFlag(input);
		return progressParamElement;
	}

	/**
	 * 解析<data/>
	 */
	private static DataElement parseData(Element element)
			throws ParserException{
		DataElement dataElement = new DataElement();
		Attribute name = element.attribute("name");
		Attribute type = element.attribute("type");
		Attribute value = element.attribute("value");
		Attribute format = element.attribute("format");
		if (type == null || value == null)
			throw new ParserException("invalid <data/>");
		if ("date".equals(type.getText()) && format == null)
			throw new ParserException("invalid <data/>");
		if (name != null)
			dataElement.setName(name.getText());
		dataElement.setType(type.getText());
		dataElement.setValue(value.getText());
		if (format != null)
			dataElement.setFormat(format.getText());
		return dataElement;
	}

	/**
	 * 解析<static-object/>
	 */
	private static StaticObjectElement parseStaticObject(Element element)
			throws ParserException{
		StaticObjectElement staticObjectElement = new StaticObjectElement();
		Attribute name = element.attribute("name");
		Attribute className = element.attribute("class");
		Attribute instance = element.attribute("instance");
		if (className == null || instance == null)
			throw new ParserException("invalid <static-object/>");
		if (name != null)
			staticObjectElement.setName(name.getText());
		staticObjectElement.setClassName(className.getText());
		staticObjectElement.setInstance(instance.getText());
		return staticObjectElement;
	}

	/**
	 * 解析<class/>
	 */
	private static ClassElement parseClass(Element element)
			throws ParserException{
		ClassElement classElement = new ClassElement();
		Attribute name = element.attribute("name");
		Attribute className = element.attribute("class");
		if (className == null)
			throw new ParserException("invalid <class/>");
		if (name != null)
			classElement.setName(name.getText());
		classElement.setClassName(className.getText());
		return classElement;
	}

	/**
	 * 解析<param/>
	 */
	private static ParamElement parseParam(Element element)
			throws ParserException{
		ParamElement paramElement = new ParamElement();
		Attribute name = element.attribute("name");
		Attribute index = element.attribute("index");
		if (index == null)
			throw new ParserException("invalid <param/>");
		if (name != null)
			paramElement.setName(name.getText());
		paramElement.setIndex(index.getText());
		return paramElement;
	}

	/**
	 * 解析各种类型的PinElement
	 */
	private static PinElement parsePin(Element element, List<String> refPins)
			throws ParserException{
		String elementName = element.getName();
		if ("data".equals(elementName)) {
			return parseData(element);
		} else if ("static-object".equals(elementName)) {
			return parseStaticObject(element);
		} else if ("class".equals(elementName)) {
			return parseClass(element);
		} else if ("param".equals(elementName)) {
			return parseParam(element);
		} else if ("ref".equals(elementName)) {
			RefElement refPin = parseRef(element);
			if (!refPins.contains(refPin.getRefName()))
				refPins.add(refPin.getRefName());
			return parseRef(element);
		} else if ("list".equals(elementName)) {
			return parseList(element, refPins);
		} else if ("express".equals(elementName)) {
			return parseExpress(element, refPins);
		} else if ("empty".equals(elementName)) {
			return new EmptyElement();
		} else if ("composite".equals(elementName)) {
			return parseComposite(element, refPins);
		}
		throw new ParserException("invalid <pin/>");
	}

	/**
	 * 解析<list/>
	 */
	@SuppressWarnings("unchecked")
	private static ListElement parseList(Element element, List<String> refPins)
			throws ParserException{
		ListElement listElement = new ListElement();
		Attribute name = element.attribute("name");
		if (name != null)
			listElement.setName(name.getText());
		Iterator<Element> it = element.elementIterator();
		while (it.hasNext()) {
			listElement.addPin(parsePin(it.next(), refPins));
		}
		return listElement;
	}

	/**
	 * 解析<ref/>
	 */
	private static RefElement parseRef(Element element)
			throws ParserException{
		RefElement refElement = new RefElement();
		Attribute name = element.attribute("name");
		Attribute refName = element.attribute("ref-name");
		if (refName == null)
			throw new ParserException("invalid <ref/>");
		refElement.setRefName(refName.getText());
		if (name != null)
			refElement.setName(name.getText());
		return refElement;
	}

	/**
	 * 解析<express/>
	 */
	@SuppressWarnings("unchecked")
	private static ExpressElement parseExpress(Element element, List<String> refPins)
			throws ParserException{
		ExpressElement expressElement = new ExpressElement();
		Attribute name = element.attribute("name");
		Attribute text = element.attribute("text");
		if (text == null)
			throw new ParserException("invalid <express/>");
		if (name != null)
			expressElement.setName(name.getText());
		expressElement.setText(text.getText());
		try {
			expressElement.parseExpress();
		} catch (Throwable ex) {
			throw new ParserException(ex.getMessage());
		}
		Iterator<Element> it = element.elementIterator();
		while (it.hasNext()) {
			// 解析所有的管脚Pin
			expressElement.addPin(parsePin(it.next(), refPins));
		}
		return expressElement;
	}

	/**
	 * 解析<bind/>
	 */
	private static BindElement parseBind(Element element, List<String> refPins)
			throws ParserException{
		BindElement bindElement = new BindElement();
		Attribute name = element.attribute("name");
		Attribute from = element.attribute("from");
		Attribute to = element.attribute("to");
		if (from == null || to == null)
			throw new ParserException("invalid <bind/>");
		bindElement.setFrom(from.getText());
		bindElement.setTo(to.getText());
		if (name != null)
			bindElement.setName(name.getText());
		// 可选，条件表达式
		Iterator<Element> children = element.elementIterator();
		if (children.hasNext()) {
			ExpressElement condition = parseExpress(children.next(), refPins);
			bindElement.setCondition(condition);
		}
		return bindElement;
	}

	/**
	 * 解析<binds/>
	 */
	@SuppressWarnings("unchecked")
	private static BindsElement parseBinds(Element element, List<String> refPins)
			throws ParserException{
		BindsElement bindingElement = new BindsElement();
		Attribute name = element.attribute("name");
		if (name != null)
			bindingElement.setName(name.getText());
		Iterator<Element> it = element.elementIterator();
		while (it.hasNext()) {
			// 解析每个<bind/>
			bindingElement.addBind(parseBind(it.next(), refPins));
		}
		return bindingElement;
	}

	/**
	 * 解析<notification/>
	 */
	@SuppressWarnings("unchecked")
	private static NotificationElement parseNotification(Element element,
														 List<String> refPins)
			throws ParserException{
		NotificationElement notificationElement = new NotificationElement();
		Attribute name = element.attribute("name");
		Attribute task = element.attribute("task");
		Attribute classOfNotification = element.attribute("class");
		if (task == null)
			throw new ParserException("invalid <notification/>");
		notificationElement.setTask(task.getText());
		if (classOfNotification != null)
			notificationElement.setClassOfNotification(classOfNotification.getText());
		if (name != null)
			notificationElement.setName(name.getText());
		Iterator<Element> it = element.elementIterator();
		if (it.hasNext()) {
			notificationElement.setResult(parsePin(it.next(), refPins));

		}
		return notificationElement;
	}

	/**
	 * 解析<composite/>
	 */
	@SuppressWarnings("unchecked")
	private static CompositeElement parseComposite(Element element, List<String> refPins)
			throws ParserException{
		CompositeElement compositeElement = new CompositeElement();
		Attribute name = element.attribute("name");
		Attribute className = element.attribute("class");
		if (className == null)
			throw new ParserException("invalid <composite/>");
		if (name != null)
			compositeElement.setName(name.getText());
		compositeElement.setClassName(className.getText());
		// 解析每一个子元素-》PinElement
		Iterator<Element> it = element.elementIterator();
		while (it.hasNext()) {
			PinElement subPinElement = parsePin(it.next(), refPins);
			// composite的子元素需要配置name
			compositeElement.addPin(subPinElement);
		}
		return compositeElement;
	}

	/**
	 * 解析<if/>
	 */
	private static TaskElement parseIf(Element element) throws ParserException {
		TaskElement taskElement = new TaskElement();
		Attribute name = element.attribute("name");
		if (name == null)
			throw new ParserException("invalid <if/>");
		taskElement.setName(name.getText());
		taskElement.setClassName(JumpStartTask.class.getName());
		return taskElement;
	}

	/**
	 * 解析<endif/>
	 */
	private static TaskElement parseEndIf(Element element) throws ParserException {
		TaskElement taskElement = new TaskElement();
		Attribute name = element.attribute("name");
		if (name == null)
			throw new ParserException("invalid <endif/>");
		taskElement.setName(name.getText() + "_endif");
		taskElement.setClassName(JumpEndTask.class.getName());
		return taskElement;
	}

	/**
	 * 解析<task/>
	 */
	@SuppressWarnings("unchecked")
	private static TaskElement parseTask(Element element, List<String> refPins)
			throws ParserException{
		TaskElement taskElement = new TaskElement();
		Attribute name = element.attribute("name");
		Attribute className = element.attribute("class");
        Attribute desc = element.attribute("desc");
		if (name == null)
			throw new ParserException("invalid <task/>");
		taskElement.setName(name.getText());
		if (className == null)
			throw new ParserException("invalid <task/>");
        taskElement.setClassName(className.getText());
        if (desc != null)
            taskElement.setDesc(desc.getText());
		Iterator<Element> it = element.elementIterator();
		while (it.hasNext()) {
			// 解析子元素，或者为<property/>或者为<pin/>
			Element child = it.next();
			taskElement.addInput(parsePin(child, refPins));
		}
		return taskElement;
	}

	/**
	 * 解析<sub-progress/>
	 */
	private static SubProgressElement parseSubProgress(
			Element element, List<String> refPins) throws ParserException {
		SubProgressElement subProgressElement = new SubProgressElement();
		Attribute name = element.attribute("name");
		Attribute packageKey = element.attribute("package");
		Attribute progressKey = element.attribute("progress");
		if (packageKey == null)
			throw new ParserException("invalid <sub-progress/>");
		if (progressKey == null)
			throw new ParserException("invalid <sub-progress/>");
		if (name != null)
			subProgressElement.setName(name.getText());
		subProgressElement.setPackageKey(packageKey.getText());
		subProgressElement.setProgressKey(progressKey.getText());
		Iterator<Element> it = element.elementIterator();
		while (it.hasNext()) {
			// 解析子元素，或者为<property/>或者为<pin/>
			Element child = it.next();
			subProgressElement.addInput(parsePin(child, refPins));
		}
		return subProgressElement;
	}

	/**
	 * 解析<entry/>
	 */
	private static EntryElement parseEntry(Element element)
			throws ParserException{
		EntryElement entryElement = new EntryElement();
		Attribute name = element.attribute("name");
		Attribute from = element.attribute("from");
		Attribute to = element.attribute("to");
		Attribute except = element.attribute("except");
		if (from == null || to == null || except == null)
			throw new ParserException("invalid <entry/>");
		entryElement.setFrom(from.getText());
		entryElement.setTo(to.getText());
		entryElement.setExcept(except.getText());
		if (name != null)
			entryElement.setName(name.getText());
		return entryElement;
	}

	/**
	 * 解析<except-table/>
	 */
	@SuppressWarnings("unchecked")
	private static ExceptionTableElement parseExceptTable(Element element)
			throws ParserException{
		ExceptionTableElement exceptionTableElement = new ExceptionTableElement();
		Attribute name = element.attribute("name");
		if (name != null)
			exceptionTableElement.setName(name.getText());
		Iterator<Element> it = element.elementIterator();
		while (it.hasNext()) {
			// 直接解析所有子元素<entry/>
			exceptionTableElement.addEntry(parseEntry(it.next()));
		}
		return exceptionTableElement;
	}

	/**
	 * 解析<progress/>
	 */
	@SuppressWarnings("unchecked")
	public static ProgressElement parseProgress(URL url) throws Exception {
		ProgressElement progressElement = new ProgressElement();
		// 解析xml文件
		SAXReader reader = new SAXReader();
		InputStream stream = url.openStream();
		Document document = reader.read(stream);
		try {
			stream.close();
		} catch (Throwable ignored){}

		Element root = document.getRootElement();
		// 解析<progress name="..."
		Attribute progressKey = root.attribute("name");
		if (progressKey == null) {
			throw new ParserException("invalid <progress/>");
		}
		progressElement.setName(progressKey.getText());

		List<String> refPins = new ArrayList<>();
		Iterator<Element> elements = root.elementIterator();
		while (elements.hasNext()) {
			// 解析子元素
			Element child = elements.next();
			String elementName = child.getName();
			if ("input".equals(elementName)) {
				// 解析<input/>
				progressElement.addInput(parseProgressParam(child, true));
			} else if ("output".equals(elementName)) {
				// 解析<output/>
				progressElement.addOutput(parseProgressParam(child, false));
			} else if ("data".equals(elementName)) {
				// 解析<data/>
				DataElement data = parseData(child);
				if (data.getName() == null)
					throw new ParserException("invalid <data/>");
				progressElement.addPin(data);
			} else if ("static-object".equals(elementName)) {
				// 解析<static-object/>
				StaticObjectElement object = parseStaticObject(child);
				if (object.getName() == null)
					throw new ParserException("invalid <static-object/>");
				progressElement.addPin(object);
			} else if ("class".equals(elementName)) {
				// 解析<class/>
				ClassElement clz = parseClass(child);
				if (clz.getName() == null)
					throw new ParserException("invalid <class/>");
				progressElement.addPin(clz);
			} else if ("param".equals(elementName)) {
				// 解析<param/>
				ParamElement param = parseParam(child);
				if (param.getName() == null)
					throw new ParserException("invalid <param/>");
				progressElement.addPin(param);
			} else if ("list".equals(elementName)) {
				// 解析<list/>
				ListElement list = parseList(child, refPins);
				if (list.getName() == null)
					throw new ParserException("invalid <list/>");
				progressElement.addPin(list);
			} else if ("express".equals(elementName)) {
				// 解析<express/>
				ExpressElement express = parseExpress(child, refPins);
				if (express.getName() == null)
					throw new ParserException("invalid <express/>");
				progressElement.addPin(express);
			} else if ("composite".equals(elementName)) {
				// 解析<composite/>
				CompositeElement composite = parseComposite(child, refPins);
				if (composite.getName() == null)
					throw new ParserException("invalid <composite/>");
				progressElement.addPin(composite);
			} else if ("task".equals(elementName)) {
				// 解析<task/>
				progressElement.addTask(parseTask(child, refPins));
			} else if ("if".equals(elementName)) {
				// 解析<if/>
				progressElement.addTask(parseIf(child));
			} else if ("endif".equals(elementName)) {
				// 解析<endif/>
				progressElement.addTask(parseEndIf(child));
			} else if ("binds".equals(elementName)) {
				// 解析<binds/>
				progressElement.setBinding(parseBinds(child, refPins));
			} else if ("notification".equals(elementName)) {
				// 解析<notification/>
				progressElement.setNotification(parseNotification(child, refPins));
			} else if ("except-table".equals(elementName)) {
				// 解析<except-table/>
				progressElement.setExceptTable(parseExceptTable(child));
			} else if ("sub-progress".equals(elementName)) {
				// 解析<sub-progress/>
				progressElement.addSubProgress(parseSubProgress(child, refPins));
			} else {
				// 其他的element都无效
				throw new ParserException("invalid <progress/>");
			}
		}
		Map<String, PinElement> pins = progressElement.getPins();
		Map<String, TaskElement> tasks = progressElement.getTasks();
		Map<String, SubProgressElement> subProgresses =
				progressElement.getSubProgress();
		// 检查所有引用的数据管脚是否正确
		for (String refName : refPins) {
			int index1 = refName.indexOf("{");
			if (index1 == -1) {
				// 引用的是独立管脚名称
				if (!pins.containsKey(refName))
					throw new ParserException("invalid <ref/>");
			} else {
				String taskName = refName.substring(0, index1);
				if (!tasks.containsKey(taskName)
						&& !subProgresses.containsKey(taskName))
					throw new ParserException("invalid <ref/>");
			}
		}
		// 检查数据管脚的循环引用问题

		// 检查task
		if (tasks.isEmpty())
			throw new ParserException("invalid <task/>");
		// 检查bind配置中task的有效性
		if (progressElement.getBinds() == null
				|| progressElement.getBinds().getBinds().isEmpty())
			throw new ParserException("invalid <binds/>");
		for (BindElement bind : progressElement.getBinds().getBinds()) {
			if (taskNotExists(bind.getFrom(), tasks, subProgresses))
				throw new ParserException("invalid <bind/>");
			if (taskNotExists(bind.getTo(), tasks, subProgresses))
				throw new ParserException("invalid <bind/>");
		}
		if (progressElement.getNotification() != null) {
			// 检查notification的task有效性
			NotificationElement notificationElement = progressElement.getNotification();
			if (taskNotExists(notificationElement.getTask(), tasks, subProgresses))
				throw new ParserException("invalid <notification/>");
		}
		if (progressElement.getExceptTable() != null) {
			// 检查异常表中的task有效性
			ExceptionTableElement errorElement = progressElement.getExceptTable();
			for (EntryElement entry : errorElement.getEntrys()) {
				if (taskNotExists(entry.getFrom(), tasks, subProgresses))
					throw new ParserException("invalid <entry/>");
				if (taskNotExists(entry.getTo(), tasks, subProgresses))
					throw new ParserException("invalid <entry/>");
				if (taskNotExists(entry.getExcept(), tasks, subProgresses))
					throw new ParserException("invalid <entry/>");
			}
		}

		return progressElement;
	}

	private static boolean taskNotExists(
			String name, Map<String, TaskElement> tasks,
			Map<String, SubProgressElement> subProgresses) {
		TaskElement task = tasks.get(name);
		SubProgressElement subProgress = subProgresses.get(name);
		return (task == null && subProgress == null)
				&& !"start".equals(name) && !"end".equals(name);
	}

    /**
     * 根据ProgressTemplate的定义生成对应的ProgressTemplate接口
     */
    public static ProgressTemplate generateProgressTemplate(
            ProgressElement progressElement, ComponentClassLoader classLoader) {
        return new ProgressTemplate() {

            public void initProgress(Progress progress, Pin[] paramPins) throws Exception{
				// 生成非匿名的数据管脚
				Map<String, PinHandler> lazyPins = new HashMap<>();
				Map<String, Task> tasks = new HashMap<>();
				// 默认放入start和end任务
				tasks.put("start", progress.getStartTask());
				tasks.put("end", progress.getEndTask());
				Map<String, Pin> pins = new HashMap<>();
				for (Map.Entry<String, PinElement> entry : progressElement.getPins().entrySet()) {
					Pin pin = generatePin(entry.getValue(), paramPins, classLoader,
							pins, tasks, lazyPins);
					pins.put(entry.getKey(), pin);
				}
				// 生成Task
				for (Map.Entry<String, TaskElement> entry : progressElement.getTasks().entrySet()) {
					Task task = generateTask(
							progress, entry.getValue(), paramPins, classLoader,
							pins, tasks, lazyPins);
					tasks.put(entry.getKey(), task);
				}
				// 关联if-endif任务
				for (Map.Entry<String, Task> entry : tasks.entrySet()) {
					if (entry.getValue() instanceof  JumpEndTask) {
						JumpEndTask endif = (JumpEndTask) entry.getValue();
						String name = entry.getKey();
						String nameOfIf = name.substring(0, name.length() - 6);
						JumpStartTask jmpStart = (JumpStartTask) tasks.get(nameOfIf);
						if (jmpStart == null) {
							throw new Exception("invalid if-endif");
						}
						endif.setJumpStartTask(jmpStart);
					}
				}
				// 生成子流程调用task
				for (Map.Entry<String, SubProgressElement> entry : progressElement.getSubProgress().entrySet()) {
					SubProgressInvokeTask task = generateSubProgress(
							progress, entry.getValue(), paramPins, classLoader,
							pins, tasks, lazyPins);
					tasks.put(entry.getKey(), task);
				}
				// lazy加载所有的ref管脚
				// ref管脚只能Progress中定义的非匿名Pin或者task的output
				if (!lazyPins.isEmpty()) {
					for (Map.Entry<String, PinHandler> entry : lazyPins.entrySet()) {
						String key = entry.getKey();
						PinHandler pinHandler = entry.getValue();
						pinHandler.setPin(getPin(pins, tasks, key));
					}
				}
				// 连接各个task
				List<BindElement> binds = progressElement.getBinds().getBinds();
				for (BindElement bind : binds) {
					Task from = tasks.get(bind.getFrom());
					Task to = tasks.get(bind.getTo());
					if (bind.getCondition() == null) {
						from.bind(to);
					} else {
						Express condition = (Express) generateExpressPin(
								bind.getCondition(), paramPins, classLoader,
								pins, tasks, lazyPins
						);
						from.bindWithCondition(to, condition);
					}
				}
				// 设置流程的输出
				NotificationElement notificationElement =
						progressElement.getNotification();
				if (notificationElement != null) {
					// 设置endpoint的task和输出的数据管脚
					Task endTask = tasks.get(notificationElement.getTask());
					Pin resultPin = null;
					if (notificationElement.getResult() != null) {
						resultPin = generatePin(notificationElement.getResult(),
								paramPins, classLoader, pins, tasks, null);
					}
					progress.setEndpoint(endTask, resultPin);
					if (notificationElement.getClassOfNotification() != null) {
						// 设置通知接口
						ResultNotification notification =
								(ResultNotification) classLoader.loadClass(
										notificationElement.getClassOfNotification()).newInstance();
						progress.setNotification(notification);
					}
				}
				// 添加异常配置entry
				if (progressElement.getExceptTable() != null) {
					for (EntryElement errorEntryElement : progressElement.getExceptTable().getEntrys()) {
						String from = errorEntryElement.getFrom();
						String to = errorEntryElement.getTo();
						Task start = "start".equals(from) ? progress.getStartTask() : tasks.get(from);
						Task end = "end".equals(to) ? progress.getEndTask() : tasks.get(to);
						Task entry = tasks.get(errorEntryElement.getExcept());
						progress.addCatchTable(start, end, entry);
					}
				}
            }

            public String getProgressKey() {
                return progressElement.getName();
            }
        };
    }

	/**
	 * 根据pin的名称获得数据管脚
	 */
	private static Pin getPin(Map<String, Pin> pins, Map<String, Task> tasks, String name) {
		int index1 = name.indexOf("{");
		int index2 = name.indexOf("}");
		if (index1 == -1) {
			// 引用了非匿名的Pin
			return pins.get(name);
		} else {
			// 引用了task的output管脚
			String taskName = name.substring(0, index1);
			int pinIndex = Integer.parseInt(name.substring(index1 + 1, index2));
			Task task = tasks.get(taskName);
			if (task == null)
				return null;
			return tasks.get(taskName).output(pinIndex);
		}
	}

	/**
	 * 生成子流程调用
	 */
	private static SubProgressInvokeTask generateSubProgress(
			Progress progress, SubProgressElement subProgressElement,
			Pin[] paramPins, ComponentClassLoader classLoader,
			Map<String, Pin> pins, Map<String, Task> tasks,
			Map<String, PinHandler> lazyPins) throws Exception{
		SubProgressInvokeTask task = new SubProgressInvokeTask(
				progress, subProgressElement.getPackageKey(),
				subProgressElement.getProgressKey());
		if (!subProgressElement.getInputs().isEmpty()) {
			Pin[] inputs = new Pin[subProgressElement.getInputs().size()];
			int i = 0;
			for (PinElement pinElement : subProgressElement.getInputs()) {
				// 生成每个参数管脚
				inputs[i++] = generatePin(pinElement, paramPins,
						classLoader, pins, tasks, lazyPins);
			}
			task.input(inputs);
		}
		return task;
	}

	/**
	 * 生成task
	 */
	private static Task generateTask(
			Progress progress, TaskElement taskElement, Pin[] paramPins,
			ComponentClassLoader classLoader,
			Map<String, Pin> pins, Map<String, Task> tasks,
			Map<String, PinHandler> lazyPins) throws Exception{
		String className = taskElement.getClassName();
		String desc = taskElement.getDesc();
		// 构造task实例
		Class<?> classOfTask = classLoader.loadClass(className);
		Constructor<?> constructor = classOfTask.getConstructor(Progress.class, String.class);
		Task task = (Task)constructor.newInstance(progress, desc);
		// 注入input管脚
		if (!taskElement.getInputs().isEmpty()) {
			Pin[] inputs = new Pin[taskElement.getInputs().size()];
			int i = 0;
			for (PinElement pinElement : taskElement.getInputs()) {
				// 将每个管脚定义生成对应Pin
				inputs[i++] = generatePin(pinElement, paramPins,
						classLoader, pins, tasks, lazyPins);
			}
			task.input(inputs);
		}
		return task;
	}

	/**
	 * 生成data类型的数据管脚
	 */
	private static Pin generateDataPin(
			DataElement dataElement){
		// 对于静态数据boolean/int/long/decimal/string/date
		// 使用ObjectPin
		return new ObjectPin(dataElement.getData());
	}

	/**
	 * 生成param类型的数据管脚
	 */
 	private static Pin generateParamPin(
			ParamElement paramElement, Pin[] paramPins) {
		return paramPins[paramElement.getIndex()];
	}

	/**
	 * 生成static-object类型的数据管脚
	 */
	private static Pin generateStaticObjectPin(
			StaticObjectElement staticObjectElement,
			ComponentClassLoader classLoader) throws Exception{
		// 静态实例，使用ObjectPin
		String className = staticObjectElement.getClassName();
		String instance = staticObjectElement.getInstance();
		Class<?> clz = classLoader.loadClass(className);
		Field field = clz.getDeclaredField(instance);
		return new ObjectPin(field.get(null));
	}

	/**
	 * 生成class类型的数据管脚
	 */
	private static Pin generateClassPin(
			ClassElement classElement,
			ComponentClassLoader classLoader)
			throws Exception {
		// 使用ObjectPin
		String className = classElement.getClassName();
		Class<?> clz = classLoader.loadClass(className);
		return new ObjectPin(clz);
	}

	/**
	 * 生成list类型的数据管脚
	 */
	private static Pin generateListPin(
			ListElement listElement, Pin[] paramPins,
			ComponentClassLoader classLoader,
			Map<String, Pin> pins, Map<String, Task> tasks,
			Map<String, PinHandler> lazyPins) throws Exception {
		// 使用ListPin，对每个子元素生成对应的Pin
		ListPin pin = new ListPin();
		for (PinElement subPinElement : listElement.getPins()) {
			pin.add(generatePin(subPinElement, paramPins,
					classLoader, pins, tasks, lazyPins));
		}
		return pin;
	}

	/**
	 * 生成composite类型的数据管脚
	 */
	private static Pin generateCompositePin(
			CompositeElement compositeElement,
			Pin[] paramPins, ComponentClassLoader classLoader,
			Map<String, Pin> pins, Map<String, Task> tasks,
			Map<String, PinHandler> lazyPins)
			throws Exception{
		// 使用CompositePin
		String className = compositeElement.getClassName();
		Class<?> clz = classLoader.loadClass(className);
		CompositePin compositePin = (CompositePin) clz.newInstance();
		for (PinElement pinElement : compositeElement.getPins()) {
			Pin subPin = generatePin(pinElement, paramPins,
					classLoader, pins, tasks, lazyPins);
			compositePin.addPin(subPin);
		}
		return compositePin;
	}

	/**
	 * 生成ref类型的数据管脚
	 */
	private static Pin generateRefPin(
			RefElement refElement, Map<String, Pin> pins,
			Map<String, Task> tasks, Map<String, PinHandler> lazyPins) {
		// 先从已经创建的pin和task里面查找
		Pin refPin = getPin(pins, tasks, refElement.getRefName());
		if (refPin != null)
			return refPin;
		// 如果还有没有创建pin，采用lazy模式时候加载
		PinHandler pin = new PinHandler();
		lazyPins.put(refElement.getRefName(), pin);
		return pin;
	}

	/**
	 * 根据表达式节点生成对应的数据管脚
	 */
	private static Pin generatePinFromNode(
			Node node, Map<String, Pin> pins, Map<String, Task> tasks,
			Map<String, PinHandler> lazyPins) throws Exception {
		if (node instanceof DataNode) {
			// 数据节点
			DataNode dataNode = (DataNode) node;
			Object data = dataNode.getData();
			if (data instanceof Variable) {
				// 数据pin为引用管脚
				Variable variable = (Variable) data;
				String refName = variable.getRefName();
				int index = variable.getIndex();
				if (index == -1) {
					// 引用普通的数据管脚，到pins中查找，如果暂时不存在，采用lazyPins延迟加载
					Pin pin = pins.get(refName);
					if (pin == null) {
						PinHandler pinHandler = new PinHandler();
						lazyPins.put(refName, pinHandler);
						return pinHandler;
					} else {
						return pin;
					}
				} else {
					// 引用了task的输出管脚
					Task task = tasks.get(refName);
					if (task == null) {
						PinHandler pinHandler = new PinHandler();
						lazyPins.put(refName + "{" + index + "}", pinHandler);
						return pinHandler;
					} else {
						return task.output(index);
					}
				}
			} else {
				// 数据pin为常量，string，boolean，number
				return new ObjectPin(data);
			}
		} else if (node instanceof ExpressNode) {
			// 表达式节点
			ExpressNode expressNode = (ExpressNode) node;
			Class<?> clz = Express.expressTbl.get(expressNode.getOperator());
			Express express = (Express) clz.newInstance();
			// 生成对应操作数节点的Pin
			List<Pin> paramPins = new ArrayList<>();
			Node child = expressNode.getFirstChild();
			while (child != null) {
				paramPins.add(generatePinFromNode(child, pins, tasks, lazyPins));
				child = child.getBrother();
			}
			// 表达式设置Pin
			express.setPins(paramPins.toArray(new Pin[0]));
			return express;
		}
		throw new ParserException("invalid <express/>");
	}

    /**
     * 生成express类型的数据管脚
     */
    private static Pin generateExpressPin(
            ExpressElement expressElement, Pin[] paramPins,
			ComponentClassLoader classLoader,
			Map<String, Pin> pins, Map<String, Task> tasks,
			Map<String, PinHandler> lazyPins)  throws Exception{
		// 如果表达式定义了pin，则先生成本地的数据管脚
		List<PinElement> localPinElements = expressElement.getPins();
		List<Pin> localPins = new ArrayList<>();
		if (!localPinElements.isEmpty()) {
			for (PinElement pinElement : localPinElements) {
				localPins.add(generatePin(pinElement, paramPins, classLoader,
						pins, tasks, lazyPins));
			}
		}
		// 根据解析的表达式树，生成对应的Express
		// 先把表达式内部的数据管脚，加入到pins中
		Map<String, Pin> allPins = new HashMap<>(pins);
		if (!localPins.isEmpty()) {
			// 本地管脚的name按照需要来定义
			for (int i = 0; i < localPins.size(); i++) {
				allPins.put("" + i, localPins.get(i));
			}
		}
		Node root = expressElement.getRoot();
		return generatePinFromNode(root, allPins, tasks, lazyPins);
	}

	/**
	 * 生成数据管脚
	 */
	private static Pin generatePin(
			PinElement pinElement, Pin[] paramPins,
			ComponentClassLoader classLoader,
			Map<String, Pin> pins, Map<String, Task> tasks,
			Map<String, PinHandler> lazyPins)
			throws Exception{
		int pinType = pinElement.getPinType();
		switch (pinType) {
			case PinElement.T_DATA:
				return generateDataPin((DataElement) pinElement);
			case PinElement.T_STATIC:
				return generateStaticObjectPin(
						(StaticObjectElement) pinElement, classLoader);
			case PinElement.T_CLASS:
				return generateClassPin(
						(ClassElement) pinElement, classLoader);
			case PinElement.T_PARAM:
				return generateParamPin((ParamElement) pinElement, paramPins);
			case PinElement.T_REF:
				return generateRefPin((RefElement) pinElement, pins, tasks, lazyPins);
			case PinElement.T_LIST:
				return generateListPin(
						(ListElement) pinElement, paramPins,
						classLoader, pins, tasks, lazyPins);
			case PinElement.T_EXPRESS:
				return generateExpressPin((ExpressElement) pinElement, paramPins,
						classLoader, pins, tasks, lazyPins);
			case PinElement.T_COMPOSITE:
				return generateCompositePin(
						(CompositeElement) pinElement, paramPins,
						classLoader, pins, tasks, lazyPins);
			case PinElement.T_EMPTY:
				return EmptyPin.inst;
		}
		throw new ParserException("invalid <pin/>");
	}

	public static void main(String[] args){
		ExpressElement expressElement = new ExpressElement();
		expressElement.setText("'abc'>='cde' || $a==true && $a[0]+1>$b");
		try {
			expressElement.parseExpress();
			System.out.println(expressElement.getRoot().toText());
		} catch (Throwable ex){
			ex.printStackTrace();
		}
	}
}
