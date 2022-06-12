package com.jurassic.core.resource.xml;

import java.util.HashMap;
import java.util.Map;

/**
 * 资源工厂配置对象
 * 
 * @author yzhu
 *
 */
public class ResourceFactoryConfig {

	private final String _id;// 工厂名称
	private final String _clz;// 实现类
	private final Map<String, String> _params;// 初始化需要的参数映射表
	
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
