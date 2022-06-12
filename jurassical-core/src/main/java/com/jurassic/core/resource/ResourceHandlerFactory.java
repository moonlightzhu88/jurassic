package com.jurassic.core.resource;

/**
 * ��Դ�������
 * �������ɶ�Ӧ����Դ���
 *
 * @author yzhu
 */
public class ResourceHandlerFactory {

	private final AttachedResourceTbl _attachedResourceTbl;// ����Դӳ���
	private final ResourceFactoryTbl _resourceFactoryTbl;// ��Դ������

	public ResourceHandlerFactory(AttachedResourceTbl mapping,
                                  ResourceFactoryTbl pool) {
		this._attachedResourceTbl = mapping;
		this._resourceFactoryTbl = pool;
	}

	public AttachedResourceTbl getAttachedResourceTbl() {
		return this._attachedResourceTbl;
	}

	/**
	 * ����ָ����Դ�ľ��
	 */
	public ResourceHandler<?> generateHandler(String name,
			String nameOfResourceFactory) {
		ResourceFactory<?> factory = this._resourceFactoryTbl
				.getResourceFactory(nameOfResourceFactory);
		return new ResourceHandler<>(name, this._attachedResourceTbl, factory);
	}
}
