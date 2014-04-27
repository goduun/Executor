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
 * 抽象转换者，实现了绝大部分转换者接口中的方法，并提供了一系列工具方法。
 * <p>
 * 提供的默认值：
 * <ol>
 * <li>默认消费者每次执行可处理的最大任务数为1。</li>
 * <li>默认不唤醒任何祖先任务。</li>
 * <li>默认awake方法为空方法。</li>
 * </ol>
 * 
 * @author Hu Ruomin
 */
public abstract class AbstractTaskConverter<C extends Task, P extends Task>
		extends AbstractTaskProcessor<C> implements TaskConverter<C, P> {

	/**
	 * 任务传送管道
	 */
	private TaskPipe<P> pipe;
	/**
	 * {@inheritDoc}
	 * 
	 */
	@Override
	public void awake(Task ancestor, Task task) {
		awake(ancestor, task, pipe);
	}

	/**
	 * {@inheritDoc}
	 * 
	 */
	@Override
	public void execute(TaskList<C> tasks) {
		execute(tasks, pipe);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @throws IllegalArgumentException
	 *             pipe为null
	 */
	@Override
	public void setPipe(TaskPipe<P> pipe) {
		if (null == pipe) {
			throw new IllegalArgumentException();
		}
		this.pipe = pipe;
	}

	/**
	 * 唤醒祖先任务。
	 * <p>
	 * 重载封装了{@link #awake(Task, Task, TaskPipe<P>)}，移除了触发唤醒的任务参数
	 * 
	 * @param ancestor
	 *            被唤醒的祖先任务
	 * @param pipe
	 *            任务传送管道
	 */
	protected void awake(Task ancestor, TaskPipe<P> pipe) {
		return;
	}
	
	/**
	 * 唤醒祖先任务。
	 * <p>
	 * 重载封装了{@link #awake(Task, Task)}，增加管道作为参数。
	 * 
	 * @param ancestor
	 *            被唤醒的祖先任务
	 * @param task
	 *            触发唤醒的任务
	 * @param pipe
	 *            任务传送管道
	 */
	protected void awake(Task ancestor, Task task, TaskPipe<P> pipe) {
		awake(ancestor, pipe);
	}

	/**
	 * 执行任务。
	 * <p>
	 * 重载封装了{@link #execute(TaskList)}，增加管道作为参数。
	 * 
	 * @param tasks
	 *            待消费的任务
	 * @param pipe
	 *            任务传送管道
	 * @see #execute(TaskList)
	 */
	protected abstract void execute(TaskList<C> tasks, TaskPipe<P> pipe);
}
