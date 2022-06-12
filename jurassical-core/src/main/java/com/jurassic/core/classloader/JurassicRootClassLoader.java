package com.jurassic.core.classloader;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.jurassic.core.global.GlobalInstRegisterTable;

/**
 * 堆类加载器,一种可以支持横向搜索策略的类加载
 * 堆类加载会关联一批原子类加载器,堆类加载器会在某些特定的场景下帮助这些原子类加载器之间搜索class
 * 而不是一味的采用双亲委派的机制.但是这种搜索策略只会适用于一些特定的场景,
 * 例如采用类名路径的某些前缀作为key进行水平搜索,而其他的一些情况仍然保持双亲委派的机制
 * 
 * @author yzhu
 * 
 */
public class JurassicRootClassLoader extends ClassLoader {

	public static final String GLOBAL_KEY = "jurassicLoader";

	// true为服务器模式,false为client模式
	// 服务器模式下，会采用Jurassic固有的类加载模式
	// 客户端模式下，会采用Jdk原有的双亲委派的加载模式
	private final boolean _mode;
	// 每一个发布包对应的类加载器
	private final Map<String, ComponentClassLoader> _componentLoaderTbl;
	// 公共lib库的类加载器
	private ComponentClassLoader _commonLibLoader;

	public JurassicRootClassLoader(boolean mode, URL[] commonLibs,
								   ClassLoader parent) {
		super(parent);
		GlobalInstRegisterTable.register(JurassicRootClassLoader.GLOBAL_KEY, this);
		this._mode = mode;
		this._componentLoaderTbl = new HashMap<>();
		// 初始化的时候加载common lib目录下的类
		this._commonLibLoader
				= new ComponentClassLoader(mode, commonLibs, this);
	}

	/**
	 * 获得公共lib包所在的类加载器
	 */
	public ComponentClassLoader getCommonLibLoader() {
		return this._commonLibLoader;
	}

	/**
	 * 绑定组件包类加载器
	 * 在组件包发布的时候，先生成该组件包使用的唯一类加载器
	 * 并根据包的key，绑定到Jurassic的根加载器上
	 */
	public synchronized ComponentClassLoader bindComponentLoader(String key,
			URL[] urls) {
		ComponentClassLoader componentClassLoader = new ComponentClassLoader(
				this._mode, urls, this);
		this._componentLoaderTbl.put(key, componentClassLoader);
		return componentClassLoader;
	}

	/**
	 * 去除组件包类加载器的绑定关系
	 */
	public synchronized void unbindComponentLoader(String packageKey) {
		this._componentLoaderTbl.remove(packageKey);
	}

	/**
	 * Jurassic根加载器的加载规则
	 * 1.现在系统加载器中查找
	 */
	public Class<?> loadClass(String name, ClassLoader exclude)
			throws ClassNotFoundException {
		if (this._mode) {
			Class<?> result = null;
			// 在系统类加载器找
			try {
				result = this.getParent().loadClass(name);
			} catch (Throwable ex) {
			}

			// 在共享库中查找
			if (result == null) {
				if (this._commonLibLoader != exclude) {
					// 这里共享库不能为exclude，以免发生类找不到出现的循环查找问题
					try {
						result = this._commonLibLoader.loadClass(name);
					} catch (Throwable ex) {
					}
				}
			}
			return result;
		} else {
			// Client模式下直接采用原有的双亲委派机制
			return super.loadClass(name);
		}
	}
}
