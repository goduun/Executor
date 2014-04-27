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
 * 处理器。
 * 
 * @author Hu Ruomin
 */
public class ProcessingExecutor<C extends Task> extends AbstractExecutor<C>
		implements Processor<C> {

	/**
	 * 任务消费者
	 */
	private TaskProcessor<C> consumer;

	/**
	 * 请查看
	 * {@link AbstractExecutor#AbstractExecutor(String, Class, boolean, int, int, TaskQueue)}
	 */
	public ProcessingExecutor(String name, Class<?> consoleClass,
			boolean isResident, int maxProducerPoolSize,
			int maxConsumerPoolSize, TaskQueue<C> taskQueue) {
		super(name, consoleClass, isResident, maxProducerPoolSize,
				maxConsumerPoolSize, taskQueue);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @throws IllegalStateException
	 *             如果当前处理器已开始执行
	 * @throws IllegalStateException
	 *             如果当前处理器已设置过消费者
	 * @throws IllegalArgumentException
	 *             consumer为null
	 */
	@Override
	public void setConsumer(TaskProcessor<C> consumer) {
		if (isExecuted()) {
			throw new IllegalStateException(
					"executor do not accept consumer after execution");
		}
		if (null == consumer) {
			throw new IllegalArgumentException("arguments is null.");
		}

		getLock().lock();
		try {
			if (null != getConsumer()) {
				throw new IllegalStateException(
						"consumer of this executor already exists and can not be changed");
			}
			this.consumer = consumer;
		} finally {
			getLock().unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TaskProcessor<C> getConsumer() {
		return consumer;
	}

}
