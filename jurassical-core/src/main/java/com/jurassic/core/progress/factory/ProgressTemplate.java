package com.jurassic.core.progress.factory;

import com.jurassic.core.progress.Progress;
import com.jurassic.core.progress.handler.pin.Pin;

/**
 * 流程构建接口
 * 
 * @author yzhu
 * 
 */
public interface ProgressTemplate {

	/**
	 * 构建一个完整的流程
	 * 每一类流程对应一个Generator
	 */
	void initProgress(Progress progress, Pin[] params) throws Exception;

	/**
	 * 返回流程的key
	 */
	String getProgressKey();

}
