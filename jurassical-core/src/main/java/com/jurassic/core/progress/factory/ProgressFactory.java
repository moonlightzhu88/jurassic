package com.jurassic.core.progress.factory;

import com.jurassic.core.progress.Progress;
import com.jurassic.core.progress.handler.pin.ObjectPin;
import com.jurassic.core.progress.handler.pin.Pin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 流程工厂
 * 
 * @author yzhu
 * 
 */
public class ProgressFactory {

	private static final Logger logger = LoggerFactory.getLogger(ProgressFactory.class);

	// 所有流程生成器的注册表
	private Map<String, ProgressTemplate> _templateTbl = new HashMap<>();
	// 每类流程的运行计数
	private Map<String, AtomicInteger> _runningProgressNums = new HashMap<>();

	/**
	 * 创建流程
	 */
	public Progress createProgress(String packageKey, String progressKey,
								   Pin[] params, Progress parent) {
		// 根据流程的key找到对应的构建器
		ProgressTemplate generator = this._templateTbl.get(progressKey);
		if (generator == null)
			return null;
		else {
			// 创建流程
			try {
				Progress progress = new Progress(packageKey, progressKey,
						this._runningProgressNums.get(progressKey));
				generator.initProgress(progress, params);
				if (parent != null && progress != null)
					progress.setParent(parent);
				return progress;
			} catch (Throwable ex) {
				logger.warn(ex.getMessage(), ex);
				return null;
			}
		}
	}

	/**
	 * 添加流程生成器
	 */
	public void addTemplate(ProgressTemplate template) {
		this._templateTbl.put(template.getProgressKey(), template);
		this._runningProgressNums.put(
				template.getProgressKey(), new AtomicInteger(0));
	}

	/**
	 * 判断流程工厂是否配置了流程模板
	 */
	public boolean hasProgressTemplate() {
		return !this._templateTbl.isEmpty();
	}

	/**
	 * 判断是否还有运行的流程
	 */
	public boolean hasRunningProgress() {
		for (Map.Entry<String, AtomicInteger> entry : this._runningProgressNums.entrySet()) {
			AtomicInteger num = entry.getValue();
			if (num.intValue() > 0) {
				logger.warn("progress " + entry.getKey() + " has running");
				return true;
			}
		}
		return false;
	}

	/**
	 * 销毁资源
	 */
	public void destroyResource() {
		this._templateTbl.clear();
		this._templateTbl = null;
		this._runningProgressNums.clear();
		this._runningProgressNums = null;
	}
}
