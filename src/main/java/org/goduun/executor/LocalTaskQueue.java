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

import java.util.Collection;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 任务队列。<p>
 * 采用ArrayBlockingQueue来实现。
 * 
 * @param <T>
 *            任务类型
 * @author Hu Ruomin
 */
public class LocalTaskQueue<T extends Task> implements TaskQueue<T> {

	/**
	 * 任务队列
	 */
	private final BlockingQueue<T> queue;

	/**
	 * 任务队列最大容量
	 */
	private final int capacity;

	/**
	 * 构造函数
	 * 
	 * @param capacity
	 *            队列的最大容量
	 * @throws IllegalArgumentException
	 *             如果capacity小于1
	 */
	public LocalTaskQueue(int capacity) {
		queue = new ArrayBlockingQueue<T>(capacity);
		this.capacity = capacity;
	}

	/**
	 * 构造函数
	 * 
	 * @param capacity
	 *            队列的最大容量
	 * @param fair
	 *            是否采用公平原则，如果是，多个线程在进行任务队列操作时，将采用FIFO机制， 即等待最久的队列将最先获取队列的操作机会
	 * @throws IllegalArgumentException
	 *             如果capacity小于1
	 */
	public LocalTaskQueue(int capacity, boolean fair) {
		queue = new ArrayBlockingQueue<T>(capacity, fair);
		this.capacity = capacity;
	}

	/**
	 * 构造函数
	 * 
	 * @param capacity
	 *            队列的最大容量
	 * @param fair
	 *            是否采用公平原则，如果是，多个线程在进行任务队列操作时，将采用FIFO机制， 即等待最久的队列将最先获取队列的操作机会
	 * @param c
	 *            初始化结合。初始化队列时，将该集合内的元素塞入队列
	 * @throws IllegalArgumentException
	 *             如果capacity小于1或小于c.size()
	 * @throws NullPointerException
	 *             c为null
	 */
	public LocalTaskQueue(int capacity, boolean fair, Collection<T> c) {
		queue = new ArrayBlockingQueue<T>(capacity, fair, c);
		this.capacity = capacity;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @throws ClassCastException
	 *             如果由于task的类型无法正确被转换
	 * @throws NullPointerException
	 *             task为null
	 * @throws IllegalArgumentException
	 *             如果task对象有某些属性导致其无法被放入任务队列
	 */
	@Override
	public void put(T task) throws InterruptedException {
		queue.put(task);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public T poll() {
		return queue.poll();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public T take() throws InterruptedException {
		return queue.take();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int size() {
		return queue.size();
	}

	/**
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public int capacity() {
		return capacity;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @throws ClassCastException
	 *             如果由于task的类型无法正确被转换
	 * @throws NullPointerException
	 *             task为null
	 * @throws IllegalArgumentException
	 *             如果task对象有某些属性导致其无法被放入任务队列
	 */
	@Override
	public boolean offer(T task) {
		return queue.offer(task);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @throws ClassCastException
	 *             如果由于task的类型无法正确被转换
	 * @throws NullPointerException
	 *             task为null
	 * @throws IllegalArgumentException
	 *             如果task对象有某些属性导致其无法被放入任务队列
	 */
	@Override
	public boolean offer(T task, long timeout, TimeUnit unit)
			throws InterruptedException {
		return queue.offer(task, timeout, unit);
	}

	/**
	 * {@inheritDoc}
	 * 
	 */
	@Override
	public T poll(long timeout, TimeUnit unit) throws InterruptedException {
		return queue.poll(timeout, unit);
	}
}
