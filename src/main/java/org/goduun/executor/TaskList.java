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

import java.util.LinkedList;
import java.util.List;


/**
 * 任务传送列表。
 * <p>
 * 执行器通过任务传送列表向执行器的消费者传送任务。为防止消费者误操作任务队列，不允许消费者直接操作任务队列，只能操作任务传送列表。
 * <p>
 * 列表与队列的区别在于列表只能获取任务，无法放入任务，并且在任务被获取之前与之后，负责完成计数、日志输出等处理。
 * <p>
 * 任务传送列表提供了两种获取任务的方式：
 * <ol>
 * <li>迭代获取：{@link #get()}</li>
 * <li>一次性获取：{@link #toList()}</li>
 * </ol>
 * 两种方式是互斥的，如果使用了迭代方式，就不能使用一次性方式，反之亦然。
 * <p>
 * 当使用迭代获取方式时，执行器将迭代的过程视为任务的开始和结束过程（即：当调用迭代方法取一个任务时，执行器认为该任务开始执行，且上一个任务结束执行），
 * 执行器会自动记录任务的开始时间和结束时间，并且逐条输出任务的结束日志。
 * <p>
 * 当使用一次性获取方式时，执行器不会记录任务的开始时间和结束时间，需要调用者自行记录。并且在整批任务都处理完成后，执行器才会输出任务结束日志。
 * 
 * @author Hu Ruomin
 * @param <T>
 *            任务类型
 */
public class TaskList<T extends Task> {

	/**
	 * 任务列表
	 */
	private final List<T> tasks;

	/**
	 * 所属执行器
	 */
	private final Executor<? extends Task> executor;

	/**
	 * 迭代索引
	 */
	private int index = 0;

	/**
	 * 标识当前的任务列表对象是否调用过{@link #toList()}方法
	 */
	private boolean isToListInvoked = false;

	/**
	 * 标识当前的任务列表对象是否调用过{@link #get()}方法
	 */
	private boolean isGetInvoked = false;

	/**
	 * 构造函数。
	 * 
	 * @param tasks
	 *            任务列表
	 * @param executor
	 *            所属执行器
	 * @throws IllegalArgumentException
	 *             tasks为null
	 * @throws IllegalArgumentException
	 *             tasks.size()为0
	 * @throws IllegalArgumentException
	 *             executor为null
	 */
	public TaskList(List<T> tasks, Executor<? extends Task> executor) {
		if (null == tasks || null == executor) {
			throw new IllegalArgumentException();
		}
		if (0 == tasks.size()) {
			throw new IllegalArgumentException();
		}
		this.tasks = tasks;
		this.executor = executor;
	}

	/**
	 * 迭代获取任务，第一次调用将返回列表中的第一个任务，第二次返回第二个，以此类推，当获取完所有任务后，再调用时将永远返回null。
	 * <p>
	 * 示例：
	 * 
	 * <pre>
	 * while (null != (task = taskList.get())) {
	 * 	// handle task
	 * }
	 * </pre>
	 * 
	 * 用此方式获取任务时，执行器将自动记录任务的开始和结束时间，并逐条输出日志。
	 * 
	 * @return 获取到的任务，如果整个迭代过程已完成，将永远返回null
	 * @throws IllegalStateException
	 *             如果已使用过{@link #toList()}方法获取任务
	 */
	public T get() {
		if (isToListInvoked) {
			throw new IllegalStateException();
		}
		isGetInvoked = true;
		return getTask();
	}

	/**
	 * 判断当前任务列表是否执行过{@link #get()}操作。
	 * 
	 * @return 已执行过返回true；否则false
	 */
	public boolean getInvoked() {
		return isGetInvoked;
	}

	/**
	 * 获取列表的任务总数。
	 * 
	 * @return 列表的任务总数
	 */
	public int size() {
		return tasks.size();
	}

	/**
	 * 一次性获取任务，该方法只能调用一次，第一次调用时返回所有的任务列表（不会为null且size()>0），之后再次调用时永远返回null。
	 * <p>
	 * 用此方式获取任务时，执行器不会自动记录任务开始和结束时间，需要手工记录。在整批任务全部处理完之后，才会进行日志输出。
	 * 
	 * @return 任务列表，如果列表已被获取过，返回null
	 * @throws IllegalStateException
	 *             如果已使用过{@link #get()}方法获取任务
	 */
	public List<T> toList() {
		if (isGetInvoked) {
			throw new IllegalStateException();
		}
		if (isToListInvoked) {
			return null;
		}
		isToListInvoked = true;
		List<T> list = new LinkedList<T>();
		T task;
		while (null != (task = getTask())) {
			try {
				list.add(task);
			} catch (Exception e) {
				continue;
			}
		}
		return list;
	}

	/**
	 * 判断当前任务列表是否执行过{@link #toList()}操作。
	 * 
	 * @return 已执行过返回true；否则false
	 */
	public boolean toListInvoked() {
		return isToListInvoked;
	}

	/**
	 * 迭代获取获取，并根据情况记录任务开始时间和任务结束时间、日志输出。
	 * 
	 * @return 获取到的任务
	 */
	private T getTask() {
		T task = null;
		if (index <= tasks.size()) {
			// 如果是通过调用get()方法来获取任务，那么自动记录所有任务的开始和结束时间，并输出结束日志
			if (0 < index && isGetInvoked) {
				T preTask = tasks.get(index - 1);
				preTask.stopExecuting();
				if (executor instanceof AbstractExecutor) {
					((AbstractExecutor<?>) executor).logTaskCompletion(preTask);
				}
			}
			// 获取任务并返回
			if (index < tasks.size()) {
				task = tasks.get(index++);
				if (!toListInvoked()) {
					task.startExecuting();
				}
			}
		}
		return task;
	}
}
