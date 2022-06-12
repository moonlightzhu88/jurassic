package com.jurassic.core.resource;

/**
 * 资源句柄工厂
 * 负责生成对应的资源句柄
 *
 * @author yzhu
 */
public class ResourceHandlerFactory {

	private final AttachedResourceTbl _attachedResourceTbl;// 绑定资源映射表
	private final ResourceFactoryTbl _resourceFactoryTbl;// 资源工厂表

	public ResourceHandlerFactory(AttachedResourceTbl mapping,
                                  ResourceFactoryTbl pool) {
		this._attachedResourceTbl = mapping;
		this._resourceFactoryTbl = pool;
	}

	public AttachedResourceTbl getAttachedResourceTbl() {
		return this._attachedResourceTbl;
	}

	/**
	 * 创建指定资源的句柄
	 */
	public ResourceHandler<?> generateHandler(String name,
			String nameOfResourceFactory) {
		ResourceFactory<?> factory = this._resourceFactoryTbl
				.getResourceFactory(nameOfResourceFactory);
		return new ResourceHandler<>(name, this._attachedResourceTbl, factory);
	}
}
