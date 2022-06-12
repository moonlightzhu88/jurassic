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
 * 组件包对应的类加载器
 * 负责管理，加载组件包中的所有Class
 * 
 * @author yzhu
 * 
 */
public class ComponentClassLoader extends URLClassLoader {
	private static final Logger logger = LoggerFactory.getLogger(ComponentClassLoader.class);

	// true为服务器模式,false为client模式
	// 服务器模式下，会采用Jurassic固有的类加载模式
	// 客户端模式下，会采用Jdk原有的双亲委派的加载模式
	private final boolean _mode;
	// 加载的jar，加载的jar包都是静态加载的
	private final URL[] _loadedJars;

	public ComponentClassLoader(boolean mode, URL[] urls,
			JurassicRootClassLoader parent) {
		super(urls, parent);
		this._mode = mode;
		this._loadedJars = urls;
	}

	/**
	 * 组件类加载器的加载策略：
	 * 1.在自身加载器中寻找对应的Class
	 * 2.委派给上层加载器JurassicRootClassLoader负责加载Class
	 * 在Client模式下仍然沿用原有的加载策略
	 */
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		if (this._mode) {
			Class<?> result;
			synchronized (getClassLoadingLock(name)) {
				result = findLoadedClass(name);
				if (result == null) {
                    // 先在自己的类加载器里面查找
                    try {
                        result= this.findClass(name);
                    } catch (Throwable ignored) {}
                    if (result == null) {
                        try {
							JurassicRootClassLoader parent =
									(JurassicRootClassLoader) this.getParent();
                            // 在父类加载器中查找
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
			// client模式下采用原有的双亲委派机制
			return super.loadClass(name);
		}
	}

	/**
	 * 直接使用Class的字节码加载Class
	 * 这个方法主要使用在序列化的过程中动态生成指定Bean的序列化类
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
	 * 查找带有annotation的interface接口
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
					// 搜索所有加载jar下的Class类，去除内部类
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
	 * 查找带有相应注解的class
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
							// 搜索加载类是否有指定的注解
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
	 * 从path加载所有满足条件的资源
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
				// 找到path下的元素
				String name = "/" + entry.getName();
				if (name.contains(path)) {
					if (filter.accept(null, name)) {
						// 过滤文件名
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
