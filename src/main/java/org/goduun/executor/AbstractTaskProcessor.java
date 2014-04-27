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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 抽象任务处理者，实现了绝大部分处理者接口中的方法，并提供了一系列工具方法。
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
public abstract class AbstractTaskProcessor<C extends Task> implements
		TaskProcessor<C> {

	/**
	 * 需要唤醒的祖先任务类型
	 */
	private Set<Class<? extends Task>> awakableClasses = new HashSet<Class<? extends Task>>();

	/**
	 * 处理者每次执行可处理的最大任务数
	 */
	private volatile int capacity = 1;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void awake(Task ancestor, Task task) {
		awake(ancestor);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public abstract void execute(TaskList<C> tasks);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<Class<? extends Task>> getAwakableClasses() {
		return awakableClasses;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getCapacity() {
		return capacity;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean shouldAwake() {
		return 0 < awakableClasses.size();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean shouldAwake(Class<? extends Task> ancestorClass) {
		return awakableClasses.contains(ancestorClass);
	}

	/**
	 * 增加可唤醒的祖先任务类型。
	 * 
	 * @param ancestorClass
	 *            祖先任务类型
	 * @throws IllegalArgumentException
	 *             ancestorClass为null
	 */
	protected void addAwakableClass(Class<? extends Task> ancestorClass) {
		if (null == ancestorClass) {
			throw new IllegalArgumentException();
		}
		awakableClasses.add(ancestorClass);
	}

	/**
	 * 批量增加可唤醒的祖先任务类型。
	 * 
	 * @param ancestorClasses
	 *            祖先任务类型
	 * @throws IllegalArgumentException
	 *             ancestorClasses为null
	 * @throws IllegalArgumentException
	 *             ancestorClasses中有null元素
	 */
	protected void addAwakableClass(Class<? extends Task>[] ancestorClasses) {
		if (null == ancestorClasses) {
			throw new IllegalArgumentException();
		}
		for (Class<? extends Task> ancestorClass : ancestorClasses) {
			awakableClasses.add(ancestorClass);
		}
	}

	/**
	 * 唤醒祖先任务。
	 * <p>
	 * 重载封装了{@link #awake(Task, Task)}，移除了触发唤醒的任务参数。
	 * 
	 * @param ancestor
	 *            被唤醒的祖先任务
	 */
	protected void awake(Task ancestor) {
		return;
	}

	/**
	 * 批量记录任务开始时间。
	 * <p>
	 * 该方法只适合通过{@link TaskList#toList()}的方法来获取任务后使用，简化批量设置的操作。
	 * 
	 * @param startedAt
	 *            任务开始时间
	 * @param taskList
	 *            任务列表
	 * @throws IllegalArgumentException
	 *             taskList为null
	 * @throws IllegalArgumentException
	 *             task不是从AbstractTask继承而来
	 */
	protected void setTasksStartedAt(long startedAt, List<C> taskList) {
		if (null == taskList) {
			throw new IllegalArgumentException();
		}
		for (C task : taskList) {
			if (task instanceof AbstractTask) {
				((AbstractTask) task).setStartedAt(startedAt);
			} else {
				throw new IllegalArgumentException();
			}
		}
	}

	/**
	 * 批量记录任务结束时间。
	 * <p>
	 * 该方法只适合通过{@link TaskList#toList()}的方法来获取任务后使用，简化批量设置的操作。
	 * 
	 * @param stoppedAt
	 *            任务结束时间
	 * @param taskList
	 *            任务列表
	 * @throws IllegalArgumentException
	 *             taskList为null
	 * @throws IllegalArgumentException
	 *             task不是从AbstractTask继承而来
	 */
	protected void setTasksStoppedAt(long stoppedAt, List<C> taskList) {
		if (null == taskList) {
			throw new IllegalArgumentException();
		}
		for (C task : taskList) {
			if (task instanceof AbstractTask) {
				((AbstractTask) task).setStoppedAt(stoppedAt);
			} else {
				throw new IllegalArgumentException();
			}
		}
	}
}
