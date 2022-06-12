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
 * ���������ļ�����
 * 
 * @author yzhu
 * 
 */
public class Compiler {

	/**
	 * ����<input>��<output>
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
	 * ����<data/>
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
	 * ����<static-object/>
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
	 * ����<class/>
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
	 * ����<param/>
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
	 * �����������͵�PinElement
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
	 * ����<list/>
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
	 * ����<ref/>
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
	 * ����<express/>
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
			// �������еĹܽ�Pin
			expressElement.addPin(parsePin(it.next(), refPins));
		}
		return expressElement;
	}

	/**
	 * ����<bind/>
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
		// ��ѡ���������ʽ
		Iterator<Element> children = element.elementIterator();
		if (children.hasNext()) {
			ExpressElement condition = parseExpress(children.next(), refPins);
			bindElement.setCondition(condition);
		}
		return bindElement;
	}

	/**
	 * ����<binds/>
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
			// ����ÿ��<bind/>
			bindingElement.addBind(parseBind(it.next(), refPins));
		}
		return bindingElement;
	}

	/**
	 * ����<notification/>
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
	 * ����<composite/>
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
		// ����ÿһ����Ԫ��-��PinElement
		Iterator<Element> it = element.elementIterator();
		while (it.hasNext()) {
			PinElement subPinElement = parsePin(it.next(), refPins);
			// composite����Ԫ����Ҫ����name
			compositeElement.addPin(subPinElement);
		}
		return compositeElement;
	}

	/**
	 * ����<if/>
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
	 * ����<endif/>
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
	 * ����<task/>
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
			// ������Ԫ�أ�����Ϊ<property/>����Ϊ<pin/>
			Element child = it.next();
			taskElement.addInput(parsePin(child, refPins));
		}
		return taskElement;
	}

	/**
	 * ����<sub-progress/>
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
			// ������Ԫ�أ�����Ϊ<property/>����Ϊ<pin/>
			Element child = it.next();
			subProgressElement.addInput(parsePin(child, refPins));
		}
		return subProgressElement;
	}

	/**
	 * ����<entry/>
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
	 * ����<except-table/>
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
			// ֱ�ӽ���������Ԫ��<entry/>
			exceptionTableElement.addEntry(parseEntry(it.next()));
		}
		return exceptionTableElement;
	}

	/**
	 * ����<progress/>
	 */
	@SuppressWarnings("unchecked")
	public static ProgressElement parseProgress(URL url) throws Exception {
		ProgressElement progressElement = new ProgressElement();
		// ����xml�ļ�
		SAXReader reader = new SAXReader();
		InputStream stream = url.openStream();
		Document document = reader.read(stream);
		try {
			stream.close();
		} catch (Throwable ignored){}

		Element root = document.getRootElement();
		// ����<progress name="..."
		Attribute progressKey = root.attribute("name");
		if (progressKey == null) {
			throw new ParserException("invalid <progress/>");
		}
		progressElement.setName(progressKey.getText());

		List<String> refPins = new ArrayList<>();
		Iterator<Element> elements = root.elementIterator();
		while (elements.hasNext()) {
			// ������Ԫ��
			Element child = elements.next();
			String elementName = child.getName();
			if ("input".equals(elementName)) {
				// ����<input/>
				progressElement.addInput(parseProgressParam(child, true));
			} else if ("output".equals(elementName)) {
				// ����<output/>
				progressElement.addOutput(parseProgressParam(child, false));
			} else if ("data".equals(elementName)) {
				// ����<data/>
				DataElement data = parseData(child);
				if (data.getName() == null)
					throw new ParserException("invalid <data/>");
				progressElement.addPin(data);
			} else if ("static-object".equals(elementName)) {
				// ����<static-object/>
				StaticObjectElement object = parseStaticObject(child);
				if (object.getName() == null)
					throw new ParserException("invalid <static-object/>");
				progressElement.addPin(object);
			} else if ("class".equals(elementName)) {
				// ����<class/>
				ClassElement clz = parseClass(child);
				if (clz.getName() == null)
					throw new ParserException("invalid <class/>");
				progressElement.addPin(clz);
			} else if ("param".equals(elementName)) {
				// ����<param/>
				ParamElement param = parseParam(child);
				if (param.getName() == null)
					throw new ParserException("invalid <param/>");
				progressElement.addPin(param);
			} else if ("list".equals(elementName)) {
				// ����<list/>
				ListElement list = parseList(child, refPins);
				if (list.getName() == null)
					throw new ParserException("invalid <list/>");
				progressElement.addPin(list);
			} else if ("express".equals(elementName)) {
				// ����<express/>
				ExpressElement express = parseExpress(child, refPins);
				if (express.getName() == null)
					throw new ParserException("invalid <express/>");
				progressElement.addPin(express);
			} else if ("composite".equals(elementName)) {
				// ����<composite/>
				CompositeElement composite = parseComposite(child, refPins);
				if (composite.getName() == null)
					throw new ParserException("invalid <composite/>");
				progressElement.addPin(composite);
			} else if ("task".equals(elementName)) {
				// ����<task/>
				progressElement.addTask(parseTask(child, refPins));
			} else if ("if".equals(elementName)) {
				// ����<if/>
				progressElement.addTask(parseIf(child));
			} else if ("endif".equals(elementName)) {
				// ����<endif/>
				progressElement.addTask(parseEndIf(child));
			} else if ("binds".equals(elementName)) {
				// ����<binds/>
				progressElement.setBinding(parseBinds(child, refPins));
			} else if ("notification".equals(elementName)) {
				// ����<notification/>
				progressElement.setNotification(parseNotification(child, refPins));
			} else if ("except-table".equals(elementName)) {
				// ����<except-table/>
				progressElement.setExceptTable(parseExceptTable(child));
			} else if ("sub-progress".equals(elementName)) {
				// ����<sub-progress/>
				progressElement.addSubProgress(parseSubProgress(child, refPins));
			} else {
				// ������element����Ч
				throw new ParserException("invalid <progress/>");
			}
		}
		Map<String, PinElement> pins = progressElement.getPins();
		Map<String, TaskElement> tasks = progressElement.getTasks();
		Map<String, SubProgressElement> subProgresses =
				progressElement.getSubProgress();
		// ����������õ����ݹܽ��Ƿ���ȷ
		for (String refName : refPins) {
			int index1 = refName.indexOf("{");
			if (index1 == -1) {
				// ���õ��Ƕ����ܽ�����
				if (!pins.containsKey(refName))
					throw new ParserException("invalid <ref/>");
			} else {
				String taskName = refName.substring(0, index1);
				if (!tasks.containsKey(taskName)
						&& !subProgresses.containsKey(taskName))
					throw new ParserException("invalid <ref/>");
			}
		}
		// ������ݹܽŵ�ѭ����������

		// ���task
		if (tasks.isEmpty())
			throw new ParserException("invalid <task/>");
		// ���bind������task����Ч��
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
			// ���notification��task��Ч��
			NotificationElement notificationElement = progressElement.getNotification();
			if (taskNotExists(notificationElement.getTask(), tasks, subProgresses))
				throw new ParserException("invalid <notification/>");
		}
		if (progressElement.getExceptTable() != null) {
			// ����쳣���е�task��Ч��
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
     * ����ProgressTemplate�Ķ������ɶ�Ӧ��ProgressTemplate�ӿ�
     */
    public static ProgressTemplate generateProgressTemplate(
            ProgressElement progressElement, ComponentClassLoader classLoader) {
        return new ProgressTemplate() {

            public void initProgress(Progress progress, Pin[] paramPins) throws Exception{
				// ���ɷ����������ݹܽ�
				Map<String, PinHandler> lazyPins = new HashMap<>();
				Map<String, Task> tasks = new HashMap<>();
				// Ĭ�Ϸ���start��end����
				tasks.put("start", progress.getStartTask());
				tasks.put("end", progress.getEndTask());
				Map<String, Pin> pins = new HashMap<>();
				for (Map.Entry<String, PinElement> entry : progressElement.getPins().entrySet()) {
					Pin pin = generatePin(entry.getValue(), paramPins, classLoader,
							pins, tasks, lazyPins);
					pins.put(entry.getKey(), pin);
				}
				// ����Task
				for (Map.Entry<String, TaskElement> entry : progressElement.getTasks().entrySet()) {
					Task task = generateTask(
							progress, entry.getValue(), paramPins, classLoader,
							pins, tasks, lazyPins);
					tasks.put(entry.getKey(), task);
				}
				// ����if-endif����
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
				// ���������̵���task
				for (Map.Entry<String, SubProgressElement> entry : progressElement.getSubProgress().entrySet()) {
					SubProgressInvokeTask task = generateSubProgress(
							progress, entry.getValue(), paramPins, classLoader,
							pins, tasks, lazyPins);
					tasks.put(entry.getKey(), task);
				}
				// lazy�������е�ref�ܽ�
				// ref�ܽ�ֻ��Progress�ж���ķ�����Pin����task��output
				if (!lazyPins.isEmpty()) {
					for (Map.Entry<String, PinHandler> entry : lazyPins.entrySet()) {
						String key = entry.getKey();
						PinHandler pinHandler = entry.getValue();
						pinHandler.setPin(getPin(pins, tasks, key));
					}
				}
				// ���Ӹ���task
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
				// �������̵����
				NotificationElement notificationElement =
						progressElement.getNotification();
				if (notificationElement != null) {
					// ����endpoint��task����������ݹܽ�
					Task endTask = tasks.get(notificationElement.getTask());
					Pin resultPin = null;
					if (notificationElement.getResult() != null) {
						resultPin = generatePin(notificationElement.getResult(),
								paramPins, classLoader, pins, tasks, null);
					}
					progress.setEndpoint(endTask, resultPin);
					if (notificationElement.getClassOfNotification() != null) {
						// ����֪ͨ�ӿ�
						ResultNotification notification =
								(ResultNotification) classLoader.loadClass(
										notificationElement.getClassOfNotification()).newInstance();
						progress.setNotification(notification);
					}
				}
				// ����쳣����entry
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
	 * ����pin�����ƻ�����ݹܽ�
	 */
	private static Pin getPin(Map<String, Pin> pins, Map<String, Task> tasks, String name) {
		int index1 = name.indexOf("{");
		int index2 = name.indexOf("}");
		if (index1 == -1) {
			// �����˷�������Pin
			return pins.get(name);
		} else {
			// ������task��output�ܽ�
			String taskName = name.substring(0, index1);
			int pinIndex = Integer.parseInt(name.substring(index1 + 1, index2));
			Task task = tasks.get(taskName);
			if (task == null)
				return null;
			return tasks.get(taskName).output(pinIndex);
		}
	}

	/**
	 * ���������̵���
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
				// ����ÿ�������ܽ�
				inputs[i++] = generatePin(pinElement, paramPins,
						classLoader, pins, tasks, lazyPins);
			}
			task.input(inputs);
		}
		return task;
	}

	/**
	 * ����task
	 */
	private static Task generateTask(
			Progress progress, TaskElement taskElement, Pin[] paramPins,
			ComponentClassLoader classLoader,
			Map<String, Pin> pins, Map<String, Task> tasks,
			Map<String, PinHandler> lazyPins) throws Exception{
		String className = taskElement.getClassName();
		String desc = taskElement.getDesc();
		// ����taskʵ��
		Class<?> classOfTask = classLoader.loadClass(className);
		Constructor<?> constructor = classOfTask.getConstructor(Progress.class, String.class);
		Task task = (Task)constructor.newInstance(progress, desc);
		// ע��input�ܽ�
		if (!taskElement.getInputs().isEmpty()) {
			Pin[] inputs = new Pin[taskElement.getInputs().size()];
			int i = 0;
			for (PinElement pinElement : taskElement.getInputs()) {
				// ��ÿ���ܽŶ������ɶ�ӦPin
				inputs[i++] = generatePin(pinElement, paramPins,
						classLoader, pins, tasks, lazyPins);
			}
			task.input(inputs);
		}
		return task;
	}

	/**
	 * ����data���͵����ݹܽ�
	 */
	private static Pin generateDataPin(
			DataElement dataElement){
		// ���ھ�̬����boolean/int/long/decimal/string/date
		// ʹ��ObjectPin
		return new ObjectPin(dataElement.getData());
	}

	/**
	 * ����param���͵����ݹܽ�
	 */
 	private static Pin generateParamPin(
			ParamElement paramElement, Pin[] paramPins) {
		return paramPins[paramElement.getIndex()];
	}

	/**
	 * ����static-object���͵����ݹܽ�
	 */
	private static Pin generateStaticObjectPin(
			StaticObjectElement staticObjectElement,
			ComponentClassLoader classLoader) throws Exception{
		// ��̬ʵ����ʹ��ObjectPin
		String className = staticObjectElement.getClassName();
		String instance = staticObjectElement.getInstance();
		Class<?> clz = classLoader.loadClass(className);
		Field field = clz.getDeclaredField(instance);
		return new ObjectPin(field.get(null));
	}

	/**
	 * ����class���͵����ݹܽ�
	 */
	private static Pin generateClassPin(
			ClassElement classElement,
			ComponentClassLoader classLoader)
			throws Exception {
		// ʹ��ObjectPin
		String className = classElement.getClassName();
		Class<?> clz = classLoader.loadClass(className);
		return new ObjectPin(clz);
	}

	/**
	 * ����list���͵����ݹܽ�
	 */
	private static Pin generateListPin(
			ListElement listElement, Pin[] paramPins,
			ComponentClassLoader classLoader,
			Map<String, Pin> pins, Map<String, Task> tasks,
			Map<String, PinHandler> lazyPins) throws Exception {
		// ʹ��ListPin����ÿ����Ԫ�����ɶ�Ӧ��Pin
		ListPin pin = new ListPin();
		for (PinElement subPinElement : listElement.getPins()) {
			pin.add(generatePin(subPinElement, paramPins,
					classLoader, pins, tasks, lazyPins));
		}
		return pin;
	}

	/**
	 * ����composite���͵����ݹܽ�
	 */
	private static Pin generateCompositePin(
			CompositeElement compositeElement,
			Pin[] paramPins, ComponentClassLoader classLoader,
			Map<String, Pin> pins, Map<String, Task> tasks,
			Map<String, PinHandler> lazyPins)
			throws Exception{
		// ʹ��CompositePin
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
	 * ����ref���͵����ݹܽ�
	 */
	private static Pin generateRefPin(
			RefElement refElement, Map<String, Pin> pins,
			Map<String, Task> tasks, Map<String, PinHandler> lazyPins) {
		// �ȴ��Ѿ�������pin��task�������
		Pin refPin = getPin(pins, tasks, refElement.getRefName());
		if (refPin != null)
			return refPin;
		// �������û�д���pin������lazyģʽʱ�����
		PinHandler pin = new PinHandler();
		lazyPins.put(refElement.getRefName(), pin);
		return pin;
	}

	/**
	 * ���ݱ��ʽ�ڵ����ɶ�Ӧ�����ݹܽ�
	 */
	private static Pin generatePinFromNode(
			Node node, Map<String, Pin> pins, Map<String, Task> tasks,
			Map<String, PinHandler> lazyPins) throws Exception {
		if (node instanceof DataNode) {
			// ���ݽڵ�
			DataNode dataNode = (DataNode) node;
			Object data = dataNode.getData();
			if (data instanceof Variable) {
				// ����pinΪ���ùܽ�
				Variable variable = (Variable) data;
				String refName = variable.getRefName();
				int index = variable.getIndex();
				if (index == -1) {
					// ������ͨ�����ݹܽţ���pins�в��ң������ʱ�����ڣ�����lazyPins�ӳټ���
					Pin pin = pins.get(refName);
					if (pin == null) {
						PinHandler pinHandler = new PinHandler();
						lazyPins.put(refName, pinHandler);
						return pinHandler;
					} else {
						return pin;
					}
				} else {
					// ������task������ܽ�
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
				// ����pinΪ������string��boolean��number
				return new ObjectPin(data);
			}
		} else if (node instanceof ExpressNode) {
			// ���ʽ�ڵ�
			ExpressNode expressNode = (ExpressNode) node;
			Class<?> clz = Express.expressTbl.get(expressNode.getOperator());
			Express express = (Express) clz.newInstance();
			// ���ɶ�Ӧ�������ڵ��Pin
			List<Pin> paramPins = new ArrayList<>();
			Node child = expressNode.getFirstChild();
			while (child != null) {
				paramPins.add(generatePinFromNode(child, pins, tasks, lazyPins));
				child = child.getBrother();
			}
			// ���ʽ����Pin
			express.setPins(paramPins.toArray(new Pin[0]));
			return express;
		}
		throw new ParserException("invalid <express/>");
	}

    /**
     * ����express���͵����ݹܽ�
     */
    private static Pin generateExpressPin(
            ExpressElement expressElement, Pin[] paramPins,
			ComponentClassLoader classLoader,
			Map<String, Pin> pins, Map<String, Task> tasks,
			Map<String, PinHandler> lazyPins)  throws Exception{
		// ������ʽ������pin���������ɱ��ص����ݹܽ�
		List<PinElement> localPinElements = expressElement.getPins();
		List<Pin> localPins = new ArrayList<>();
		if (!localPinElements.isEmpty()) {
			for (PinElement pinElement : localPinElements) {
				localPins.add(generatePin(pinElement, paramPins, classLoader,
						pins, tasks, lazyPins));
			}
		}
		// ���ݽ����ı��ʽ�������ɶ�Ӧ��Express
		// �Ȱѱ��ʽ�ڲ������ݹܽţ����뵽pins��
		Map<String, Pin> allPins = new HashMap<>(pins);
		if (!localPins.isEmpty()) {
			// ���عܽŵ�name������Ҫ������
			for (int i = 0; i < localPins.size(); i++) {
				allPins.put("" + i, localPins.get(i));
			}
		}
		Node root = expressElement.getRoot();
		return generatePinFromNode(root, allPins, tasks, lazyPins);
	}

	/**
	 * �������ݹܽ�
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
