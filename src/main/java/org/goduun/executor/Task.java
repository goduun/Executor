/*
 * Copyright (c) 2012 eshore.com. 
 *
 * All Rights Reserved.
 *
 * This program is the confidential and proprietary information of 
 * eshore. ("Confidential Information").  You shall not disclose such
 * Confidential Information and shall use it only in accord with
 * the terms of the license agreement you entered into with eshore.com.
 *
 */
package org.goduun.executor;

import java.io.Serializable;

import org.goduun.executor.datasource.DataSource;

/**
 * 执行器生产消费的任务。
 * 
 * <p>
 * 一个任务，代表着一件需完成的事情，由执行器的生产者负责创建，放入任务队列，随后，执行器的消费者将其从任务队列中取出，进行处理。
 * 任务与任务之间可设置父子关系
 * ，一个任务能够派生出多个子任务，子任务又能派生子任务，从而形成一颗任务树。<b>任务关系会在任务日志中体现，因此，强烈建议设置任务关系
 * ，便于通过日志进行任务处理情况分析。</b>
 * 
 * <p>
 * 执行器在消费任务时，能够通过唤醒机制对任务树进行同步控制，详情请查阅：{@link TaskProcessor}。
 * 
 * <p>
 * 执行器在生产消费的过程中，可输出每个任务的处理过程日志，主要包括：
 * <ol>
 * <li>任务创建日志</li>
 * <li>任务执行日志</li>
 * <li>任务唤醒日志</li>
 * </ol>
 * 任务日志的内容主要包括：
 * <ol>
 * <li>任务的唯一标识</li>
 * <li>任务的扩展信息</li>
 * <li>执行耗时</li>
 * <li>执行是否异常</li>
 * <li>执行异常原因</li>
 * <li>唤醒耗时</li>
 * <li>唤醒是否异常</li>
 * <li>唤醒异常原因</li>
 * </ol>
 * 
 * @see TaskProducer
 * @see TaskProcessor
 * @see TaskConverter
 * 
 * @author Hu Ruomin
 */
public interface Task extends Serializable {

	/**
	 * 获取处理该任务时所默认使用的数据源。
	 * <p>
	 * 如果方法返回一个有效的数据源，那么在执行器处理该任务前，会自动将当前线程的数据源切换至该数据源。
	 * 如果方法返回null或一个未定义的数据源或发生异常，执行器将使用系统默认数据源。
	 * 
	 * @return 任务默认数据源
	 */
	DataSource getDefaultDataSource();

	/**
	 * 获取执行器处理当前任务所耗费的时间，时间单位：毫秒。
	 * 
	 * @return 当前任务的执行耗时
	 * 
	 * @see #startExecuting()
	 * @see #stopExecuting()
	 */
	long getExecutingMillis();

	/**
	 * 获取当前任务执行失败的原因。
	 * 
	 * @return 当前任务执行失败的原因
	 */
	Throwable getFailedCause();

	/**
	 * 获取当前任务唤醒失败的原因。
	 * 
	 * @return 当前任务唤醒失败的原因
	 */
	Throwable getFailedCauseOfAwaking();

	/**
	 * 获取当前任务区别于同类其他任务的唯一标识。
	 * 
	 * @return 当前任务区别于同类其他任务的唯一标识
	 */
	String getId();

	/**
	 * 获取当前任务的扩展信息。
	 * 
	 * @return 当前任务的扩展信息
	 */
	String getInfo();

	/**
	 * 获取当前任务的执行结果描述。
	 * 
	 * @return 当前任务的执行结果描述
	 */
	String getMessage();

	/**
	 * 获取当前任务的唤醒结果描述。
	 * 
	 * @return 当前任务的唤醒结果描述
	 */
	String getMessageOfAwaking();

	/**
	 * 获取当前任务的父任务。
	 * 
	 * @return 当前任务的父任务，可为null
	 */
	Task getParent();

	/**
	 * 获取当前任务的状态，任务状态被执行器用于同步控制，使用者请勿直接操作。
	 * 
	 * @return 当前任务的状态
	 */
	TaskState getTaskState();

	/**
	 * 判断当前任务是否执行失败。
	 * 
	 * @return 失败返回true，否则false
	 */
	boolean isFailed();

	/**
	 * 判断当前任务是否唤醒失败。
	 * 
	 * @return 失败返回true，否则false
	 */
	boolean isFailedToBeAwaked();

	/**
	 * 设置处理该任务时所默认使用的数据源。
	 * 
	 * @param defaultDataSource
	 *            处理该任务时所默认使用的数据源
	 */
	void setDefaultDataSource(DataSource defaultDataSource);

	/**
	 * 设置当前任务的执行结果描述。
	 * 
	 * @param message
	 *            当前任务的执行结果描述
	 */
	void setMessage(String message);

	/**
	 * 设置当前任务的唤醒结果描述。
	 * 
	 * @param message
	 *            唤醒结果描述
	 */
	void setMessageOfAwaking(String message);

	/**
	 * 设置当前任务的状态，任务状态被执行器用于同步控制，使用者请勿直接操作。
	 * 
	 * @return 当前任务的状态
	 */
	
	/**
	 * 设置当前任务的状态，任务状态被执行器用于同步控制，使用者请勿直接操作。
	 * 
	 * @param taskState 当前任务的状态
	 */
	void setTaskState(TaskState taskState);

	/**
	 * 标识当前任务被执行器开始处理，记录开始时间。
	 */
	void startExecuting();

	/**
	 * 标识当前任务已被执行器处理完成，记录完成时间。
	 */
	void stopExecuting();

}
