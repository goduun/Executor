/*
 * Copyright (C) 2014 The Goduun Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.goduun.executor;


/**
 * 转换器。 
 * 
 * @author Hu Ruomin
 */
public class ConvertingExecutor<C extends Task, P extends Task> extends
		AbstractExecutor<C> implements Converter<C, P> {

	/**
	 * 标识一个转换器是否已完成了转换设置
	 */
	private volatile boolean isConverted = false;

	/**
	 * 任务转换者
	 */
	private TaskConverter<C, P> consumer;

	/**
	 * 请查看
	 * {@link AbstractExecutor#AbstractExecutor(String, Class, boolean, int, int, TaskQueue)}
	 */
	public ConvertingExecutor(String name, Class<?> consoleClass,
			boolean isResident, int maxProducerPoolSize,
			int maxConsumerPoolSize, TaskQueue<C> taskQueue) {
		super(name, consoleClass, isResident, maxProducerPoolSize,
				maxConsumerPoolSize, taskQueue);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @throws IllegalStateException
	 *             如果当前执行器正在终止或已终止
	 * @throws IllegalStateException
	 *             如果当前执行器没有设置消费者对象
	 * @throws IllegalStateException
	 *             如果当前执行器没有被其他执行器串联
	 */
	@Override
	public void execute() {
		if (!isConverted) {
			throw new IllegalStateException(
					"converting executor cannot execute before been followed by other executor");
		}
		super.execute();
	}
	
	/**
	 * 
	 * {@inheritDoc}
	 * 
     * @throws IllegalStateException
	 *             如果当前执行器不是常驻执行器
	 * @throws IllegalStateException
	 *             如果当前执行器没有设置消费者
	 * @throws InterruptedException
	 *             休眠过程中当前线程被中断
	 * @throws IllegalStateException
	 *             如果当前转换器没有被其他执行器连接
	 */
	public void executeAndSleep() throws InterruptedException {
		if (!isConverted) {
			throw new IllegalStateException(
					"converting executor cannot execute before been followed by other executor");
		}
		super.executeAndSleep();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @throws IllegalStateException
	 *             如果当前执行器已开始执行
	 * @throws IllegalStateException
	 *             如果当前执行器已设置过converter
	 * @throws IllegalArgumentException
	 *             converter为null
	 */
	@Override
	public void setConsumer(TaskConverter<C, P> converter) {
		if (isExecuted()) {
			throw new IllegalStateException(
					"executor do not accept consumer after execution");
		}
		if (null == converter) {
			throw new IllegalArgumentException("arguments is null.");
		}

		getLock().lock();
		try {
			if (null != getConsumer()) {
				throw new IllegalStateException(
						"consumer of this executor already exists and can not be changed");
			}
			this.consumer = converter;
		} finally {
			getLock().unlock();
		}

	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public TaskProcessor<C> getConsumer() {
		getLock().lock();
		try {
			return consumer;
		} finally {
			getLock().unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @throws IllegalArgumentException
	 *             pipe为null
	 * @throws IllegalStateException
	 *             当前转换器尚未设置消费者对象
	 * @throws IllegalStateException
	 *             当前转换器已被其他执行器连接
	 * 
	 */
	@Override
	public void convertTaskTo(TaskPipe<P> pipe) {
		if (isConverted) {
			throw new IllegalStateException();
		}
		if (null == pipe) {
			throw new IllegalArgumentException();
		}
		isConverted = true;
		getLock().lock();
		try {
			if (null == getConsumer()) {
				throw new IllegalStateException();
			}
			consumer.setPipe(pipe);
		} finally {
			getLock().unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isConverted() {
		return isConverted;
	}

	

}
