package com.jurassic.core.resource.xml;

import java.util.HashMap;
import java.util.Map;

/**
 * ��Դ�������ö���
 * 
 * @author yzhu
 *
 */
public class ResourceFactoryConfig {

	private final String _id;// ��������
	private final String _clz;// ʵ����
	private final Map<String, String> _params;// ��ʼ����Ҫ�Ĳ���ӳ���
	
	public ResourceFactoryConfig(String id, String clz){
		this._id = id;
		this._clz = clz;
		this._params = new HashMap<>();
	}
	
	public void setParam(String name, String value){
		this._params.put(name, value);
	}
	
	public String getID(){
		return this._id;
	}
	
	public String getFactoryClz(){
		return this._clz;
	}
	
	public Map<String, String> getInitParams(){
		return this._params;
	}
}
