package com.jurassic.core.classloader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FilenameFilter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * �������Ӧ���������
 * �����������������е�����Class
 * 
 * @author yzhu
 * 
 */
public class ComponentClassLoader extends URLClassLoader {
	private static final Logger logger = LoggerFactory.getLogger(ComponentClassLoader.class);

	// trueΪ������ģʽ,falseΪclientģʽ
	// ������ģʽ�£������Jurassic���е������ģʽ
	// �ͻ���ģʽ�£������Jdkԭ�е�˫��ί�ɵļ���ģʽ
	private final boolean _mode;
	// ���ص�jar�����ص�jar�����Ǿ�̬���ص�
	private final URL[] _loadedJars;

	public ComponentClassLoader(boolean mode, URL[] urls,
			JurassicRootClassLoader parent) {
		super(urls, parent);
		this._mode = mode;
		this._loadedJars = urls;
	}

	/**
	 * �����������ļ��ز��ԣ�
	 * 1.�������������Ѱ�Ҷ�Ӧ��Class
	 * 2.ί�ɸ��ϲ������JurassicRootClassLoader�������Class
	 * ��Clientģʽ����Ȼ����ԭ�еļ��ز���
	 */
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		if (this._mode) {
			Class<?> result;
			synchronized (getClassLoadingLock(name)) {
				result = findLoadedClass(name);
				if (result == null) {
                    // �����Լ�����������������
                    try {
                        result= this.findClass(name);
                    } catch (Throwable ignored) {}
                    if (result == null) {
                        try {
							JurassicRootClassLoader parent =
									(JurassicRootClassLoader) this.getParent();
                            // �ڸ���������в���
                            result = parent.loadClass(name, this);
                        } catch (Throwable ignored) {}
                    }
				}
			}
			if (result == null) {
				throw new ClassNotFoundException(name);
			}
			return result;
		} else {
			// clientģʽ�²���ԭ�е�˫��ί�ɻ���
			return super.loadClass(name);
		}
	}

	/**
	 * ֱ��ʹ��Class���ֽ������Class
	 * ���������Ҫʹ�������л��Ĺ����ж�̬����ָ��Bean�����л���
	 */
	public Class<?> loadClassFromBytes(String name, byte[] codes)
			throws ClassNotFoundException {
		Class<?> result;
		synchronized (getClassLoadingLock(name)) {
			result = findLoadedClass(name);
			if (result == null) {
				result = this.defineClass(name, codes, 0, codes.length);
			}
		}
		if (result == null) {
			throw new ClassNotFoundException(name);
		}
		return result;
	}

	/**
	 * ���Ҵ���annotation��interface�ӿ�
	 */
	@SuppressWarnings({"rawtypes","unchecked"})
	public List<Class<?>> findByAnnotation(Class annotation) {
		if (this._loadedJars == null || this._loadedJars.length == 0)
			return null;

		List<Class<?>> classes = new ArrayList<>();
		JarFile jar;
		for (URL loadedJar : this._loadedJars) {
			try {
				jar = new JarFile(loadedJar.getFile());
			} catch (Throwable ex) {
				continue;
			}
			Enumeration<?> it = jar.entries();
			while (it.hasMoreElements()) {
				JarEntry entry = (JarEntry) it.nextElement();
				String name = entry.getName();
				int idx = name.indexOf(".class");
				if (idx != -1 && !name.contains("$")) {
					// �������м���jar�µ�Class�࣬ȥ���ڲ���
					String clzName = name.substring(0, idx).replace('/', '.');
					try {
						Class<?> clz = this.loadClass(clzName);
						if (clz.getAnnotation(annotation) != null) {
							classes.add(clz);
						}
					} catch (Throwable ignored) {
					}
				}
			}
			try {
				jar.close();
			} catch (Throwable ignored) {
			}
		}
		return classes;
	}

	/**
	 * ���Ҵ�����Ӧע���class
	 */
	@SuppressWarnings({"unchecked","rawtypes"})
	public List<Class<?>> findClassByAnnotation(Class<?>... classes) {
		if (this._loadedJars == null || this._loadedJars.length == 0)
			return null;
		List<Class<?>> foundClasses = new ArrayList<>();
		JarFile jar;
		for (URL loadedJar : this._loadedJars) {
			try {
				jar = new JarFile(loadedJar.getFile());
			} catch (Throwable ex) {
				continue;
			}
			Enumeration<?> it = jar.entries();
			while (it.hasMoreElements()) {
				JarEntry entry = (JarEntry) it.nextElement();
				String name = entry.getName();
				int idx = name.indexOf(".class");
				if (idx != -1) {
					String clzName = name.substring(0, idx).replace('/', '.');
					try {
						Class<?> loadedClass = this.loadClass(clzName);
						for (Class annotation : classes) {
							// �����������Ƿ���ָ����ע��
							if (loadedClass.getAnnotation(annotation) != null) {
								foundClasses.add(loadedClass);
								break;
							}
						}
					} catch (Throwable ignored) {
					}
				}
			}
			try {
				jar.close();
			} catch (Throwable ignored) {
			}
		}
		return foundClasses;
	}

	/**
	 * ��path��������������������Դ
	 */
	public List<URL> getResourceFromPath(String path, FilenameFilter filter) {
		if (this._loadedJars == null || this._loadedJars.length == 0)
			return null;
		List<URL> filterUrls = new ArrayList<>();
		JarFile jar;
		for (URL loadedJar : this._loadedJars) {
			try {
				jar = new JarFile(loadedJar.getFile());
			} catch (Throwable ex) {
				continue;
			}
			Enumeration<?> it = jar.entries();
			while (it.hasMoreElements()) {
				JarEntry entry = (JarEntry) it.nextElement();
				// �ҵ�path�µ�Ԫ��
				String name = "/" + entry.getName();
				if (name.contains(path)) {
					if (filter.accept(null, name)) {
						// �����ļ���
						try {
							URL url = new URL("jar:file:/" + loadedJar.getPath() + "!" + name);
							if (url != null) {
								filterUrls.add(url);
							} else {
								logger.warn("not find resource:" + name);
							}
						} catch (Throwable ignored) {}
					}
				}
			}
			try {
				jar.close();
			} catch (Throwable ignored) {}
		}
		return filterUrls;
	}
}
