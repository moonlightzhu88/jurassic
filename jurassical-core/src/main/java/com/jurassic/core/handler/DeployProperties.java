package com.jurassic.core.handler;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

/**
 * 处理器的配置定义
 * 每个处理器加载配置定义在组件包的根目录下,在包加载的时候,加载器会扫描根目录是否存在配置属性文件
 * 例如app应用包的根目录为com.jurassic.component.app.
 * 属性文件为com/jurassic/component/app/deploy.conf
 * 配置文件采用property的方式
 * 
 * @author yzhu
 * 
 */
public class DeployProperties {

	public static final String CONF_NAME = "deploy.conf";// 处理器的配置文件名

	// 加载的配置属性
	private Properties _properties;

	/**
	 * 从类路径加载
	 */
	public DeployProperties(String classpath, ClassLoader loader) {
		// 加载属性文件
		this._properties = new Properties();
		try {
			this._properties.load(loader.getResourceAsStream(classpath));
		} catch (Throwable ex) {
			this._properties = null;
		}
	}

	public void destroyResource() {
		if (this._properties != null) {
			this._properties.clear();
			this._properties = null;
		}
	}

	/**
	 * 从文件路径加载
	 */
	public DeployProperties(File file) {
		this._properties = new Properties();
		try {
			this._properties.load(new FileInputStream(file));
		} catch (Throwable ex) {
			this._properties = null;
		}
	}

	/**
	 * 判断配置文件是否加载完成
	 */
	public boolean isLoaded() {
		return this._properties != null;
	}

	/**
	 * 获得属性
	 */
	public String getProperty(String key) {
		return this._properties.getProperty(key);
	}

	/**
	 * 获得所有的key值
	 */
	public List<String> getKeys() {
		Enumeration<?> iterator = this._properties.keys();
		if (iterator != null) {
			List<String> keys = new ArrayList<>();
			while (iterator.hasMoreElements()) {
				keys.add((String) iterator.nextElement());
			}
			return keys;
		} else {
			return null;
		}
	}

}
