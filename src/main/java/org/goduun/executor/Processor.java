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

/**
 * 处理器。<p>
 * 处理器是执行器的一种，能够完成基本的任务生产与消费功能。
 * 
 * @author Hu Ruomin
 */
public interface Processor<C extends Task> extends Executor<C> {

	/**
	 * 设置当前处理器的任务消费者。<p>
	 * 在处理器启动之前，必须完成任务消费者的设置。
	 * 
	 * @param consumer
	 *            消费者对象
	 * 
	 * @throws IllegalStateException
	 *             如果当前处理器已开始执行
	 * @throws IllegalStateException
	 *             如果当前处理器已设置过消费者
	 * @throws IllegalArgumentException
	 *             consumer为null
	 */
	public void setConsumer(TaskProcessor<C> consumer);
}
