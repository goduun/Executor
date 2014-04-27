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
 * 执行器的状态监控实施器。
 * <p>
 * 执行器的状态监控器用该实施器来完成对执行器的状态监控与运行时动态调整执行器线程池上限。
 * 
 * @see ExecutingMonitor
 * @author Hu Ruomin
 */
public interface ExecutingStateMonitor {

	/**
	 * 保存当前执行器状态，该方法由执行器的状态监控器每隔一段时间调用。
	 * 
	 * @param state
	 *            当前执行器状态
	 */
	void saveState(ExecutingState state);

	/**
	 * 获取新的生产者线程池最大线程数上限，该方法由执行器的状态监控器每隔一段时间调用。
	 * <p>
	 * 如果该方法的返回值大于0，监控器将指定执行器的生产者线程池最大线程数上限调整为该方法的返回值。
	 * 
	 * @param executorName
	 *            执行器名称
	 * @return 新的生产者线程池最大线程数上限
	 */
	int newProducerThreadMaxSize(String executorName);

	/**
	 * 获取新的消费者线程池最大线程数上限，该方法由执行器的状态监控器每隔一段时间调用。
	 * <p>
	 * 如果该方法的返回值大于0，监控器将指定执行器的消费者线程池最大线程数上限调整为该方法的返回值。
	 * 
	 * @param executorName
	 *            执行器名称
	 * @return 新的消费者线程池最大线程数上限
	 */
	int newConsumerThreadMaxSize(String executorName);
}
