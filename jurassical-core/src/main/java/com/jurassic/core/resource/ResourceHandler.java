package com.jurassic.core.resource;

/**
 * 资源句柄
 *
 * @author yzhu
 */
public class ResourceHandler<T> {

	private final String _name;// 句柄名称
	private final AttachedResourceTbl _mapping;// 线程的资源映射表
	private final ResourceFactory<T> _resFactory;// 资源工厂

	public ResourceHandler(String name, AttachedResourceTbl mapping,
			ResourceFactory<T> factory) {
		this._name = name;
		this._mapping = mapping;
		this._resFactory = factory;
	}

	public T getResource() {
		// 先从映射表里面查找指定名字对应的资源
		T resource = this._mapping.getResource(this._name);
		if (resource == null) {
			// 创建资源
			Resource<T> newResource = this._resFactory.newResource();
			if (newResource != null) {
				this._mapping.attachResource(this._name, newResource);
				return newResource.getResource();
			} else {
				return null;
			}
		} else {
			return resource;
		}
	}
}
