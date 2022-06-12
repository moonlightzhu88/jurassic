package com.jurassic.core.bus;

/**
 * Jurassic运行框架的常量定义
 * 这里面定义了线程池,内存模块,运行参数等常量
 * 
 * @author yzhu
 */
public class Constant {

	// 处理线程的默认线程数
	public static final int DRPT_WORKER_SIZE = 4;
	// 处理线程的最大线程数
	public static final int DRPT_MAX_WORKER_SIZE = 16;
	// 每个处理器接收请求的缓冲区默认大小，缓冲区大小=2的power次方
	public static final int DRPT_DATA_SIZE_POWER = 8;
	// 每个处理器接受请求的缓冲区最大大小
	public static final int DRPT_MAX_DATA_SIZE_POWER = 10;
	// 默认的EPU处理数据容量
	public static final int DEFAULT_EPU_POWER = 8;
}
