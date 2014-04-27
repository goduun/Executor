/*
 * Copyright (c) 2012 eshore.com. 
 *
 * All Rights Reserved.
 *
 * This program is the confidential and proprietary information of 
 * eshore. ("Confidential Information").  You shall not disclose such
 * Confidential Information and shall use it only in accordance with
 * the terms of the license agreement you entered into with eshore.com.
 *
 */
package org.goduun.executor;

import java.util.concurrent.BlockingQueue;

/**
 * 生产错误传输管道。
 * <p>
 * 执行器生产者在执行过程中，如果有错误发生，可通过该管道向执行器传输错误信息，执行器会将错误信息作为日志进行输出。
 * 
 * @author Hu Ruomin
 */
public class ProducingErrorPipe {

	/**
	 * 错误管道
	 */
	private final BlockingQueue<ProducingError> pipe;

	/**
	 * 构造函数。
	 * 
	 * @param pipe
	 *            错误管道
	 * @throws IllegalArgumentException
	 *             pipe为null
	 */
	public ProducingErrorPipe(BlockingQueue<ProducingError> pipe) {
		if (null == pipe) {
			throw new IllegalArgumentException();
		}
		this.pipe = pipe;
	}

	/**
	 * 新建一个错误，并放入管道。
	 * @param producerClass
	 *            执行器的生产者类型
	 * @param message
	 *            错误信息
	 * 
	 * @throws IllegalArgumentException
	 *             producerClass为null
	 */
	public void createError(Class<?> producerClass, String message) {
		put(new ProducingError(producerClass, message));
	}

	/**
	 * 新建一个错误，并放入管道。
	 * @param producerClass
	 *            执行器的生产者类型
	 * @param message
	 *            错误信息
	 * @param cause
	 *            错误原因
	 * 
	 * @throws IllegalArgumentException
	 *             producerClass为null
	 */
	public void createError(Class<?> producerClass, String message,
			Throwable cause) {
		put(new ProducingError(producerClass, message, cause));
	}

	/**
	 * 新建一个错误，并放入管道。
	 * @param producerClass
	 *            执行器的生产者类型
	 * @param cause
	 *            错误原因
	 * 
	 * @throws IllegalArgumentException
	 *             producerClass为null
	 */
	public void createError(Class<?> producerClass, Throwable cause) {
		put(new ProducingError(producerClass, cause));
	}

	/**
	 * 将错误放入管道。
	 * 
	 * @param error
	 *            错误
	 * @throws NullPointerException
	 *             task为null
	 * @throws IllegalArgumentException
	 *             如果task对象有某些属性导致其无法被放入任务队列
	 */
	public void put(ProducingError error) {
		try {
			pipe.put(error);
		} catch (InterruptedException e) {
			return;
		}
	}
}
