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
 * 执行器工厂类，提供一系列的静态方法，创建不同类型的执行器，简化执行器的初始化操作。
 * 
 * @author Hu Ruomin
 */
public final class ExecutorFactory {

	/**
	 * 初始化一个非常驻执行的转换执行器。
	 * 
	 * @param name
	 *            执行器名称
	 * @param consoleClass
	 *            执行器宿主的类型，用于构造执行器的日志对象，输出执行日志
	 * @return 执行器对象
	 */
	public static <C extends Task, P extends Task> ConvertingExecutor<C, P> newConverter(
			String name, Class<?> consoleClass) {
		return new ConvertingExecutor<C, P>(name, consoleClass, false, 0, 0,
				null);
	}

	/**
	 * 初始化一个非常驻执行的转换执行器。
	 * 
	 * @param name
	 *            执行器名称
	 * @param consoleClass
	 *            执行器宿主的类型，用于构造执行器的日志对象，输出执行日志
	 * @param maxConsumerPoolSize
	 *            执行器的消费者线程池上限
	 * @return 执行器对象
	 */
	public static <C extends Task, P extends Task> ConvertingExecutor<C, P> newConverter(
			String name, Class<?> consoleClass, int maxConsumerPoolSize) {
		return new ConvertingExecutor<C, P>(name, consoleClass, false, 0,
				maxConsumerPoolSize, null);
	}

	/**
	 * 初始化一个非常驻执行的转换执行器。
	 * 
	 * @param name
	 *            执行器名称
	 * @param consoleClass
	 *            执行器宿主的类型，用于构造执行器的日志对象，输出执行日志
	 * @param maxProducerPoolSize
	 *            执行器的生产者线程池上限
	 * @param maxConsumerPoolSize
	 *            执行器的消费者线程池上限
	 * @return 执行器对象
	 */
	public static <C extends Task, P extends Task> ConvertingExecutor<C, P> newConverter(
			String name, Class<?> consoleClass, int maxProducerPoolSize,
			int maxConsumerPoolSize) {
		return new ConvertingExecutor<C, P>(name, consoleClass, false,
				maxProducerPoolSize, maxConsumerPoolSize, null);
	}

	/**
	 * 初始化一个非常驻执行的转换执行器。
	 * 
	 * @param name
	 *            执行器名称
	 * 
	 * @param consoleClass
	 *            执行器宿主的类型，用于构造执行器的日志对象，输出执行日志
	 * @param maxProducerPoolSize
	 *            执行器的生产者线程池上限
	 * @param maxConsumerPoolSize
	 *            执行器的消费者线程池上限
	 * @param taskQueue
	 *            任务队列
	 * @return 执行器对象
	 */
	public static <C extends Task, P extends Task> ConvertingExecutor<C, P> newConverter(
			String name, Class<?> consoleClass, int maxProducerPoolSize,
			int maxConsumerPoolSize, TaskQueue<C> taskQueue) {
		return new ConvertingExecutor<C, P>(name, consoleClass, false,
				maxProducerPoolSize, maxConsumerPoolSize, taskQueue);
	}

	/**
	 * 初始化一个非常驻执行的转换执行器。
	 * 
	 * @param name
	 *            执行器名称
	 * @param consoleClass
	 *            执行器宿主的类型，用于构造执行器的日志对象，输出执行日志
	 * @param maxConsumerPoolSize
	 *            执行器的消费者线程池上限
	 * @param taskQueue
	 *            任务队列
	 * @return 执行器对象
	 */
	public static <C extends Task, P extends Task> ConvertingExecutor<C, P> newConverter(
			String name, Class<?> consoleClass, int maxConsumerPoolSize,
			TaskQueue<C> taskQueue) {
		return new ConvertingExecutor<C, P>(name, consoleClass, false, 0,
				maxConsumerPoolSize, taskQueue);
	}

	/**
	 * 初始化一个非常驻执行的转换执行器。
	 * 
	 * @param name
	 *            执行器名称
	 * @param consoleClass
	 *            执行器宿主的类型，用于构造执行器的日志对象，输出执行日志
	 * @param taskQueue
	 *            任务队列
	 * @return 执行器对象
	 */
	public static <C extends Task, P extends Task> ConvertingExecutor<C, P> newConverter(
			String name, Class<?> consoleClass, TaskQueue<C> taskQueue) {
		return new ConvertingExecutor<C, P>(name, consoleClass, false, 0, 0,
				taskQueue);
	}

	/**
	 * 初始化一个非常驻执行的基础执行器。
	 * 
	 * @param name
	 *            执行器名称
	 * @param consoleClass
	 *            执行器宿主的类型，用于构造执行器的日志对象，输出执行日志
	 * @return 执行器对象
	 */
	public static <C extends Task> ProcessingExecutor<C> newExecutor(String name,
			Class<?> consoleClass) {
		return new ProcessingExecutor<C>(name, consoleClass, false, 0, 0, null);
	}

	/**
	 * 初始化一个非常驻执行的基础执行器。
	 * 
	 * @param name
	 *            执行器名称
	 * @param consoleClass
	 *            执行器宿主的类型，用于构造执行器的日志对象，输出执行日志
	 * @param maxConsumerPoolSize
	 *            执行器的消费者线程池上限
	 * @return 执行器对象
	 */
	public static <C extends Task> ProcessingExecutor<C> newExecutor(String name,
			Class<?> consoleClass, int maxConsumerPoolSize) {
		return new ProcessingExecutor<C>(name, consoleClass, false, 0,
				maxConsumerPoolSize, null);
	}

	/**
	 * 初始化一个非常驻执行的基础执行器。
	 * 
	 * @param name
	 *            执行器名称
	 * @param consoleClass
	 *            执行器宿主的类型，用于构造执行器的日志对象，输出执行日志
	 * @param maxProducerPoolSize
	 *            执行器的生产者线程池上限
	 * @param maxConsumerPoolSize
	 *            执行器的消费者线程池上限
	 * @return 执行器对象
	 */
	public static <C extends Task> ProcessingExecutor<C> newExecutor(String name,
			Class<?> consoleClass, int maxProducerPoolSize,
			int maxConsumerPoolSize) {
		return new ProcessingExecutor<C>(name, consoleClass, false,
				maxProducerPoolSize, maxConsumerPoolSize, null);
	}

	/**
	 * 初始化一个非常驻执行的基础执行器。
	 * 
	 * @param name
	 *            执行器名称
	 * @param consoleClass
	 *            执行器宿主的类型，用于构造执行器的日志对象，输出执行日志
	 * @param maxProducerPoolSize
	 *            执行器的生产者线程池上限
	 * @param maxConsumerPoolSize
	 *            执行器的消费者线程池上限
	 * @param taskQueue
	 *            任务队列
	 * @return 执行器对象
	 */
	public static <C extends Task> ProcessingExecutor<C> newExecutor(String name,
			Class<?> consoleClass, int maxProducerPoolSize,
			int maxConsumerPoolSize, TaskQueue<C> taskQueue) {
		return new ProcessingExecutor<C>(name, consoleClass, false,
				maxProducerPoolSize, maxConsumerPoolSize, taskQueue);
	}

	/**
	 * 初始化一个非常驻执行的基础执行器。
	 * 
	 * @param name
	 *            执行器名称
	 * @param consoleClass
	 *            执行器宿主的类型，用于构造执行器的日志对象，输出执行日志
	 * @param maxConsumerPoolSize
	 *            执行器的消费者线程池上限
	 * @param taskQueue
	 *            任务队列
	 * @return 执行器对象
	 */
	public static <C extends Task> ProcessingExecutor<C> newExecutor(String name,
			Class<?> consoleClass, int maxConsumerPoolSize,
			TaskQueue<C> taskQueue) {
		return new ProcessingExecutor<C>(name, consoleClass, false, 0,
				maxConsumerPoolSize, taskQueue);
	}

	/**
	 * 初始化一个非常驻执行的基础执行器。
	 * 
	 * @param name
	 *            执行器名称
	 * @param consoleClass
	 *            执行器宿主的类型，用于构造执行器的日志对象，输出执行日志
	 * @param taskQueue
	 *            任务队列
	 * @return 执行器对象
	 */
	public static <C extends Task> ProcessingExecutor<C> newExecutor(String name,
			Class<?> consoleClass, TaskQueue<C> taskQueue) {
		return new ProcessingExecutor<C>(name, consoleClass, false, 0, 0, taskQueue);
	}

	/**
	 * 初始化一个常驻执行的转换执行器。
	 * 
	 * @param name
	 *            执行器名称
	 * @param consoleClass
	 *            执行器宿主的类型，用于构造执行器的日志对象，输出执行日志
	 * @return 执行器对象
	 */
	public static <C extends Task, P extends Task> ConvertingExecutor<C, P> newResidentConverter(
			String name, Class<?> consoleClass) {
		return new ConvertingExecutor<C, P>(name, consoleClass, true, 0, 0,
				null);
	}

	/**
	 * 初始化一个常驻执行的转换执行器。
	 * 
	 * @param name
	 *            执行器名称
	 * @param consoleClass
	 *            执行器宿主的类型，用于构造执行器的日志对象，输出执行日志
	 * @param maxConsumerPoolSize
	 *            执行器的消费者线程池上限
	 * @return 执行器对象
	 */
	public static <C extends Task, P extends Task> ConvertingExecutor<C, P> newResidentConverter(
			String name, Class<?> consoleClass, int maxConsumerPoolSize) {
		return new ConvertingExecutor<C, P>(name, consoleClass, true, 0,
				maxConsumerPoolSize, null);
	}

	/**
	 * 初始化一个常驻执行的转换执行器。
	 * 
	 * @param name
	 *            执行器名称
	 * @param consoleClass
	 *            执行器宿主的类型，用于构造执行器的日志对象，输出执行日志
	 * @param maxProducerPoolSize
	 *            执行器的生产者线程池上限
	 * @param maxConsumerPoolSize
	 *            执行器的消费者线程池上限
	 * @return 执行器对象
	 */
	public static <C extends Task, P extends Task> ConvertingExecutor<C, P> newResidentConverter(
			String name, Class<?> consoleClass, int maxProducerPoolSize,
			int maxConsumerPoolSize) {
		return new ConvertingExecutor<C, P>(name, consoleClass, true,
				maxProducerPoolSize, maxConsumerPoolSize, null);
	}

	/**
	 * 初始化一个常驻执行的转换执行器。
	 * 
	 * @param name
	 *            执行器名称
	 * @param consoleClass
	 *            执行器宿主的类型，用于构造执行器的日志对象，输出执行日志
	 * @param maxProducerPoolSize
	 *            执行器的生产者线程池上限
	 * @param maxConsumerPoolSize
	 *            执行器的消费者线程池上限
	 * @param taskQueue
	 *            任务队列
	 * @return 执行器对象
	 */
	public static <C extends Task, P extends Task> ConvertingExecutor<C, P> newResidentConverter(
			String name, Class<?> consoleClass, int maxProducerPoolSize,
			int maxConsumerPoolSize, TaskQueue<C> taskQueue) {
		return new ConvertingExecutor<C, P>(name, consoleClass, true,
				maxProducerPoolSize, maxConsumerPoolSize, taskQueue);
	}

	/**
	 * 初始化一个常驻执行的转换执行器。
	 * 
	 * @param name
	 *            执行器名称
	 * @param consoleClass
	 *            执行器宿主的类型，用于构造执行器的日志对象，输出执行日志
	 * @param maxConsumerPoolSize
	 *            执行器的消费者线程池上限
	 * @param taskQueue
	 *            任务队列
	 * @return 执行器对象
	 */
	public static <C extends Task, P extends Task> ConvertingExecutor<C, P> newResidentConverter(
			String name, Class<?> consoleClass, int maxConsumerPoolSize,
			TaskQueue<C> taskQueue) {
		return new ConvertingExecutor<C, P>(name, consoleClass, true, 0,
				maxConsumerPoolSize, taskQueue);
	}

	/**
	 * 初始化一个常驻执行的转换执行器。
	 * 
	 * @param name
	 *            执行器名称
	 * @param consoleClass
	 *            执行器宿主的类型，用于构造执行器的日志对象，输出执行日志
	 * @param taskQueue
	 *            任务队列
	 * @return 执行器对象
	 */
	public static <C extends Task, P extends Task> ConvertingExecutor<C, P> newResidentConverter(
			String name, Class<?> consoleClass, TaskQueue<C> taskQueue) {
		return new ConvertingExecutor<C, P>(name, consoleClass, true, 0, 0,
				taskQueue);
	}

	/**
	 * 初始化一个常驻执行的基础执行器。
	 * 
	 * @param name
	 *            执行器名称
	 * @param consoleClass
	 *            执行器宿主的类型，用于构造执行器的日志对象，输出执行日志
	 * @return 执行器对象
	 */
	public static <C extends Task> ProcessingExecutor<C> newResidentExecutor(
			String name, Class<?> consoleClass) {
		return new ProcessingExecutor<C>(name, consoleClass, true, 0, 0, null);
	}

	/**
	 * 初始化一个常驻执行的基础执行器。
	 * 
	 * @param name
	 *            执行器名称
	 * @param consoleClass
	 *            执行器宿主的类型，用于构造执行器的日志对象，输出执行日志
	 * @param maxConsumerPoolSize
	 *            执行器的消费者线程池上限
	 * @return 执行器对象
	 */
	public static <C extends Task> ProcessingExecutor<C> newResidentExecutor(
			String name, Class<?> consoleClass, int maxConsumerPoolSize) {
		return new ProcessingExecutor<C>(name, consoleClass, true, 0,
				maxConsumerPoolSize, null);
	}

	/**
	 * 初始化一个常驻执行的基础执行器。
	 * 
	 * @param name
	 *            执行器名称
	 * @param consoleClass
	 *            执行器宿主的类型，用于构造执行器的日志对象，输出执行日志
	 * @param maxProducerPoolSize
	 *            执行器的生产者线程池上限
	 * @param maxConsumerPoolSize
	 *            执行器的消费者线程池上限
	 * @return 执行器对象
	 */
	public static <C extends Task> ProcessingExecutor<C> newResidentExecutor(
			String name, Class<?> consoleClass, int maxProducerPoolSize,
			int maxConsumerPoolSize) {
		return new ProcessingExecutor<C>(name, consoleClass, true,
				maxProducerPoolSize, maxConsumerPoolSize, null);
	}

	/**
	 * 初始化一个常驻执行的基础执行器。
	 * 
	 * @param name
	 *            执行器名称
	 * @param consoleClass
	 *            执行器宿主的类型，用于构造执行器的日志对象，输出执行日志
	 * @param maxProducerPoolSize
	 *            执行器的生产者线程池上限
	 * @param maxConsumerPoolSize
	 *            执行器的消费者线程池上限
	 * @param taskQueue
	 *            任务队列
	 * @return 执行器对象
	 */
	public static <C extends Task> ProcessingExecutor<C> newResidentExecutor(
			String name, Class<?> consoleClass, int maxProducerPoolSize,
			int maxConsumerPoolSize, TaskQueue<C> taskQueue) {
		return new ProcessingExecutor<C>(name, consoleClass, true,
				maxProducerPoolSize, maxConsumerPoolSize, taskQueue);
	}

	/**
	 * 初始化一个常驻执行的基础执行器。
	 * 
	 * @param name
	 *            执行器名称
	 * @param consoleClass
	 *            执行器宿主的类型，用于构造执行器的日志对象，输出执行日志
	 * @param maxConsumerPoolSize
	 *            执行器的消费者线程池上限
	 * @param taskQueue
	 *            任务队列
	 * @return 执行器对象
	 */
	public static <C extends Task> ProcessingExecutor<C> newResidentExecutor(
			String name, Class<?> consoleClass, int maxConsumerPoolSize,
			TaskQueue<C> taskQueue) {
		return new ProcessingExecutor<C>(name, consoleClass, true, 0,
				maxConsumerPoolSize, taskQueue);
	}

	/**
	 * 初始化一个常驻执行的基础执行器。
	 * 
	 * @param name
	 *            执行器名称
	 * @param consoleClass
	 *            执行器宿主的类型，用于构造执行器的日志对象，输出执行日志
	 * @param taskQueue
	 *            任务队列
	 * @return 执行器对象
	 */
	public static <C extends Task> ProcessingExecutor<C> newResidentExecutor(
			String name, Class<?> consoleClass, TaskQueue<C> taskQueue) {
		return new ProcessingExecutor<C>(name, consoleClass, true, 0, 0, taskQueue);
	}

	/**
	 * 不允许实例化
	 */
	private ExecutorFactory() {
	}

}
