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
