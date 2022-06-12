package com.jurassic.core.handler;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

/**
 * �����������ö���
 * ÿ���������������ö�����������ĸ�Ŀ¼��,�ڰ����ص�ʱ��,��������ɨ���Ŀ¼�Ƿ�������������ļ�
 * ����appӦ�ð��ĸ�Ŀ¼Ϊcom.jurassic.component.app.
 * �����ļ�Ϊcom/jurassic/component/app/deploy.conf
 * �����ļ�����property�ķ�ʽ
 * 
 * @author yzhu
 * 
 */
public class DeployProperties {

	public static final String CONF_NAME = "deploy.conf";// �������������ļ���

	// ���ص���������
	private Properties _properties;

	/**
	 * ����·������
	 */
	public DeployProperties(String classpath, ClassLoader loader) {
		// ���������ļ�
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
	 * ���ļ�·������
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
	 * �ж������ļ��Ƿ�������
	 */
	public boolean isLoaded() {
		return this._properties != null;
	}

	/**
	 * �������
	 */
	public String getProperty(String key) {
		return this._properties.getProperty(key);
	}

	/**
	 * ������е�keyֵ
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
