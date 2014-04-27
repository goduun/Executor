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
