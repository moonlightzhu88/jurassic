package com.jurassic.core.notification;

/**
 * 流程处理结束后的回调接口,用于通知执行结果
 *
 * @author yzhu
 *
 */
public interface ResultNotification {

	void notify(Object result);
}
