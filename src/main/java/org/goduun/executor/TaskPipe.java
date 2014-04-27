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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 任务传送管道。
 * <p>
 * 执行器的生产者通过任务传送管道向任务队列传送任务，为防止生产者误操作任务队列，不允许生产者直接操作任务队列，只能操作任务传送管道。
 * <p>
 * 管道与队列的区别在于管道只能放入任务，无法获取任务，并且在任务被放入队列之前与之后，负责完成计数、日志输出等处理。
 * 
 * @author Hu Ruomin
 * @param <T>
 */
public class TaskPipe<T extends Task> {

	/**
	 * 通过任务管道成功放入任务队列的任务总数
	 */
	private final AtomicLong count = new AtomicLong();

	/**
	 * 通过该管道输送任务的目标任务队列
	 */
	private final TaskQueue<T> taskQueue;

	/**
	 * 所属执行器
	 */
	private final Executor<? extends Task> executor;

	/**
	 * 构造函数。
	 * 
	 * @param taskQueue
	 *            通过该管道输送任务的目标任务队列
	 * @param executor
	 *            所属执行器
	 * 
	 * @throws IllegalArgumentException
	 *             taskQueue为null
	 * @throws IllegalArgumentException
	 *             executor为null
	 */
	public TaskPipe(TaskQueue<T> taskQueue, Executor<? extends Task> executor) {
		if (null == taskQueue || null == executor) {
			throw new IllegalArgumentException();
		}
		this.taskQueue = taskQueue;
		this.executor = executor;
	}

	/**
	 * 获取通过任务管道成功放入任务队列的任务总数。
	 * 
	 * @return 通过任务管道成功放入任务队列的任务总数
	 */
	public long count() {
		return count.get();
	}

	/**
	 * 尝试将任务通过管道放入任务队列，非阻塞方法，调用后会立刻返回，如果队列满导致任务无法被放入，将返回false，放入成功返回true。
	 * 
	 * @param task
	 *            待放入队列的任务
	 * @return 任务是否已放入任务队列
	 * 
	 * @throws ClassCastException
	 *             如果由于task的类型无法正确被转换
	 * @throws NullPointerException
	 *             task为null
	 * @throws IllegalArgumentException
	 *             如果task对象有某些属性导致其无法被放入任务队列
	 */
	public boolean offer(T task) {
		doBeforeTaskQueued(task);
		boolean success = taskQueue.offer(task);
		if (success) {
			doAfterTaskQueued(task);
		}
		return success;
	}

	/**
	 * 尝试将任务通过管道放入任务队列，阻塞方法，如果当前任务队列满，线程将阻塞，等待队列有空位出现，当任务成功放入队列或线程阻塞超过指定时长后，
	 * 方法将返回。
	 * 
	 * @param task
	 *            待放入队列的任务
	 * @param timeout
	 *            最大阻塞等待时长
	 * @param unit
	 *            最大阻塞等待时长单位
	 * @return 任务是否已放入任务队列
	 * 
	 * @throws ClassCastException
	 *             如果由于task的类型无法正确被转换
	 * @throws NullPointerException
	 *             task为null
	 * @throws IllegalArgumentException
	 *             如果task对象有某些属性导致其无法被放入任务队列
	 * @throws InterruptedException
	 *             InterruptedException
	 */
	public boolean offer(T task, long timeout, TimeUnit unit)
			throws InterruptedException {
		doBeforeTaskQueued(task);
		boolean success = taskQueue.offer(task, timeout, unit);
		if (success) {
			doAfterTaskQueued(task);
		}
		return success;
	}

	/**
	 * 将任务通过管道放入任务队列，阻塞方法，当任务队列满时，方法不会返回，当前线程将阻塞，直到队列中有任务被其他线程取走，当前任务被放入队列后，
	 * 方法才会返回。
	 * 
	 * @param task
	 *            待放入队列的任务
	 * @throws ClassCastException
	 *             如果由于task的类型无法正确被转换
	 * @throws NullPointerException
	 *             task为null
	 * @throws IllegalArgumentException
	 *             如果task对象有某些属性导致其无法被放入任务队列
	 * @throws InterruptedException
	 *             InterruptedException
	 */
	public void put(T task) throws InterruptedException {
		doBeforeTaskQueued(task);
		taskQueue.put(task);
		doAfterTaskQueued(task);
	}

	/**
	 * 当任务放入任务队列前需处理的逻辑。
	 * 
	 * @param task
	 *            即将放入任务队列的任务
	 */
	private void doBeforeTaskQueued(T task) {
		if (null == task) {
			return;
		}

		TaskState taskState = new TaskState(task);
		Class<? extends Task> parentClass = null == task.getParent() ? null : task.getParent().getClass();
		TaskState.setTaskClassRelation(task.getClass(), parentClass);
		task.setTaskState(taskState);
	}
	
	/**
	 * 当任务成功放入任务队列后需处理的逻辑。
	 * 
	 * @param task
	 *            成功放入任务队列的任务
	 */
	private void doAfterTaskQueued(T task) {
		if (null == task) {
			return;
		}
		
		// 更新任务状态
		Task parent = task.getParent();
		if (null != parent) {
			TaskState parentState = parent.getTaskState();
			if (null != parentState) {
				while (null != parentState) {
					parentState.increaseUnExecutedSubtask(task.getClass());
					parentState.increaseUnAwakedSubtask(task.getClass());
					
					parent = parent.getParent();
					try {
						parentState = parent.getTaskState();
					} catch (Exception e) {
						parentState = null;
					}
				}
			}
		}

		// 通过任务管道成功放入任务队列的任务总数加1
		count.incrementAndGet();

		// 输出任务生成日志
		if (executor instanceof AbstractExecutor) {
			((AbstractExecutor<?>) executor).logTaskGeneration(task);
		}
	}
}
