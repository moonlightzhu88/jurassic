package com.jurassic.core.classloader;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.jurassic.core.global.GlobalInstRegisterTable;

/**
 * ���������,һ�ֿ���֧�ֺ����������Ե������
 * ������ػ����һ��ԭ���������,�������������ĳЩ�ض��ĳ����°�����Щԭ���������֮������class
 * ������һζ�Ĳ���˫��ί�ɵĻ���.����������������ֻ��������һЩ�ض��ĳ���,
 * �����������·����ĳЩǰ׺��Ϊkey����ˮƽ����,��������һЩ�����Ȼ����˫��ί�ɵĻ���
 * 
 * @author yzhu
 * 
 */
public class JurassicRootClassLoader extends ClassLoader {

	public static final String GLOBAL_KEY = "jurassicLoader";

	// trueΪ������ģʽ,falseΪclientģʽ
	// ������ģʽ�£������Jurassic���е������ģʽ
	// �ͻ���ģʽ�£������Jdkԭ�е�˫��ί�ɵļ���ģʽ
	private final boolean _mode;
	// ÿһ����������Ӧ���������
	private final Map<String, ComponentClassLoader> _componentLoaderTbl;
	// ����lib����������
	private ComponentClassLoader _commonLibLoader;

	public JurassicRootClassLoader(boolean mode, URL[] commonLibs,
								   ClassLoader parent) {
		super(parent);
		GlobalInstRegisterTable.register(JurassicRootClassLoader.GLOBAL_KEY, this);
		this._mode = mode;
		this._componentLoaderTbl = new HashMap<>();
		// ��ʼ����ʱ�����common libĿ¼�µ���
		this._commonLibLoader
				= new ComponentClassLoader(mode, commonLibs, this);
	}

	/**
	 * ��ù���lib�����ڵ��������
	 */
	public ComponentClassLoader getCommonLibLoader() {
		return this._commonLibLoader;
	}

	/**
	 * ��������������
	 * �������������ʱ�������ɸ������ʹ�õ�Ψһ�������
	 * �����ݰ���key���󶨵�Jurassic�ĸ���������
	 */
	public synchronized ComponentClassLoader bindComponentLoader(String key,
			URL[] urls) {
		ComponentClassLoader componentClassLoader = new ComponentClassLoader(
				this._mode, urls, this);
		this._componentLoaderTbl.put(key, componentClassLoader);
		return componentClassLoader;
	}

	/**
	 * ȥ���������������İ󶨹�ϵ
	 */
	public synchronized void unbindComponentLoader(String packageKey) {
		this._componentLoaderTbl.remove(packageKey);
	}

	/**
	 * Jurassic���������ļ��ع���
	 * 1.����ϵͳ�������в���
	 */
	public Class<?> loadClass(String name, ClassLoader exclude)
			throws ClassNotFoundException {
		if (this._mode) {
			Class<?> result = null;
			// ��ϵͳ���������
			try {
				result = this.getParent().loadClass(name);
			} catch (Throwable ex) {
			}

			// �ڹ�����в���
			if (result == null) {
				if (this._commonLibLoader != exclude) {
					// ���ﹲ��ⲻ��Ϊexclude�����ⷢ�����Ҳ������ֵ�ѭ����������
					try {
						result = this._commonLibLoader.loadClass(name);
					} catch (Throwable ex) {
					}
				}
			}
			return result;
		} else {
			// Clientģʽ��ֱ�Ӳ���ԭ�е�˫��ί�ɻ���
			return super.loadClass(name);
		}
	}
}
