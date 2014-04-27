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

import java.util.concurrent.TimeUnit;

/**
 * 执行器的任务队列。
 * <p>
 * 任务队列负责中转及缓冲任务，执行器的生产者将任务生产出之后，将任务放入任务队列，执行器的消费者再从任务队列中取出任务进行处理。
 * <p>
 * 
 * @param <T>
 *            任务类型
 * @author Hu Ruomin
 */
public interface TaskQueue<T extends Task> {

	/**
	 * 获取任务队列的最大容量。
	 * 
	 * @return 任务队列的最大容量
	 */
	int capacity();

	/**
	 * 尝试将任务放入任务队列，非阻塞方法，调用后会立刻返回，如果队列满导致任务无法被放入，将返回false，放入成功返回true。
	 * 
	 * @param task
	 *            待放入队列的任务
	 * @return 任务是否已放入任务队列
	 */
	boolean offer(T task);

	/**
	 * 尝试将任务放入任务队列，阻塞方法，如果当前任务队列满，线程将阻塞，等待队列有空位出现，当任务成功放入队列或线程阻塞超过指定时长后，方法将返回。
	 * 
	 * @param task
	 *            待放入队列的任务
	 * @param timeout
	 *            最大阻塞等待时长
	 * @param unit
	 *            最大阻塞等待时长单位
	 * @return 任务是否已放入任务队列
	 * @throws InterruptedException
	 *             阻塞过程中线程被中断
	 */
	boolean offer(T task, long timeout, TimeUnit unit)
			throws InterruptedException;

	/**
	 * 从任务队列中获取任务，非阻塞方法，调用后立刻返回，如果任务队列不为空，该方法将返回获取到的任务，如果任务队列为空，将返回null。
	 * 
	 * @return 获取到的任务，如果当前队列为空，返回null
	 */
	T poll();

	/**
	 * 从任务队列中获取任务，阻塞方法，如果当前任务队列为空，线程将阻塞，等待队列中有任务出现，当成功获取到任务或线程阻塞超过指定时长后，方法将返回。
	 * 
	 * @param timeout
	 *            最大阻塞等待时长
	 * @param unit
	 *            最大阻塞等待时长单位
	 * @return 获取到的任务，如果在阻塞超时前未获取到任务，返回null
	 * @throws InterruptedException
	 *             阻塞过程中线程被中断
	 */
	T poll(long timeout, TimeUnit unit) throws InterruptedException;

	/**
	 * 将任务放入任务队列，阻塞方法，当任务队列满时，方法不会返回，当前线程将阻塞，直到队列中有任务被其他线程取走，当前任务被放入队列后，方法才会返回。
	 * 
	 * @param task
	 *            待放入队列的任务
	 * @throws InterruptedException
	 *             阻塞过程中线程被中断
	 */
	void put(T task) throws InterruptedException;

	/**
	 * 获取当前任务队列中的任务数量。
	 * 
	 * @return 当前任务队列中的任务数量
	 */
	int size();

	/**
	 * 从任务队列中获取任务，阻塞方法，当任务队列为空时，方法不会返回，当前线程将阻塞，等待其他线程往队列中放入任务，在获取到任务后，方法才会返回。
	 * 
	 * @return 获取到的任务
	 * @throws InterruptedException
	 *             阻塞过程中线程被中断
	 */
	T take() throws InterruptedException;

}
