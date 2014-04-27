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

/**
 * 执行器。
 * <p>
 * 执行器是一个生产-消费模式的多线程框架。
 * <p>
 * 执行器实现了生产-消费模式，一个执行器可具有多个生产者，一个任务队列，一个任务消费者，生产者生产任务，放入任务队列，消费者从任务队列中获取任务进行处理。
 * <p>
 * 执行器是一个多线程框架，可方便的指定及运行时调整执行生产和消费的线程数，并可监控执行器的运行效率与线程状态。
 * <p>
 * 执行器按运行时长可分为两类：
 * <ol>
 * <li>常驻执行器：此类执行器不会也不能够被终止，在启动后，它拥有的线程资源不会被销毁，可避免频繁创建/销毁线程的开销。常驻执行器适用于开发常驻进程。</li>
 * <li>非常驻执行器：此类执行器能够被终止，在执行过程中，它拥有的线程如果闲置就可能被销毁。非常驻执行器适用于开发一次性或间歇性进程。</li>
 * </ol>
 * 执行器按功能可分为两类：
 * <ol>
 * <li>处理器：此类执行器只能够完成基本的生产与消费。</li>
 * <li>转换器：此类执行器除了能够完成基本的生产与消费外，还可在消费过程中生产新的任务，并将新任务放入另一个执行器的任务队列。</li>
 * </ol>
 * 通过多个转换器的连接，可将复杂的任务拆分为多个简单的任务，分散在不同转换器中执行，实现“大拆小、繁化简”的程序模式。
 * <p>
 * 执行器能够进行统一格式的日志输出，日志的范围包括：
 * <ol>
 * <li>任务日志：任务生命周期中每个环节的处理情况日志。</li>
 * <li>运行日志：生产者与消费者自身的运行情况日志。</li>
 * </ol>
 * <b>执行器使用步骤：</b>
 * <ol>
 * <li>初始化执行器。</li>
 * <li>为执行器增加任务生产者。</li>
 * <li>将执行器连接至其他转换器（2与3均为可选步骤，但需至少完成一步，否则执行器将没有任务来源）。</li>
 * <li>为执行器设置任务消费者。</li>
 * <li>设置生产线程数上限（可选步骤，默认：运行时可用的CPU个数）。</li>
 * <li>设置消费线程数上限（可选步骤，默认：运行时可用的CPU个数）。</li>
 * <li>设置是否输出任务日志（可选步骤，默认：输出）。</li>
 * <li>设置是否输出运行日志（可选步骤，默认：输出）。</li>
 * <li>启动执行器。</li>
 * <li>终止执行器（非常驻执行器）。</li>
 * </ol>
 * 
 * @author Hu Ruomin
 * @param <T>
 *            执行器生产与消费的任务类型
 */
public interface Executor<T extends Task> {

	/**
	 * 为当前执行器增加生产者。
	 * <p>
	 * 如果当前执行器未启动，生产者被添加后也不会启动；如果当前执行器已启动，生产者被添加后会立刻被提交到线程池。
	 * 不允许对正在终止或已终止的执行器调用该方法。
	 * 
	 * @param producer
	 *            生产者对象
	 * @throws IllegalArgumentException
	 *             producer为空
	 * @throws IllegalStateException
	 *             如果当前执行器已开始执行
	 * @throws IllegalStateException
	 *             当前执行器已终止或正在终止
	 * @throws IllegalStateException
	 *             该生产者对象已被添加过
	 */
	void addProducer(TaskProducer<T> producer);

	/**
	 * 启动执行器，非阻塞方法，调用后立刻返回。
	 * <p>
	 * 执行器在启动之前必须完成消费者的设置，在启动之后终止之前可以添加新的生产者。
	 * <p>
	 * 一个执行器只能使用一次，一旦启动-终止之后就不不允许再次启动，对已启动但未终止的执行器再次启动将无任何效果。
	 * <p>
	 * 
	 * @throws IllegalStateException
	 *             如果当前执行器正在终止或已终止
	 * @throws IllegalStateException
	 *             如果当前执行器没有设置消费者
	 */
	void execute();

	/**
	 * 启动执行器，在启动之后，当前线程将永远休眠，该方法适合于启动常驻执行器。
	 * <p>
	 * 执行器在启动之前必须完成消费者的设置，在启动之后终止之前可以添加新的生产者。
	 * <p>
	 * 一个执行器只能使用一次，一旦启动-终止之后就不不允许再次启动，对已启动但未终止的执行器再次启动将无任何效果。
	 * <p>
	 * 不允许对非常驻执行器调用该方法。
	 * 
	 * @throws IllegalStateException
	 *             如果当前执行器不是常驻执行器
	 * @throws IllegalStateException
	 *             如果当前执行器没有设置消费者
	 * @throws InterruptedException
	 *             休眠过程中当前线程被中断
	 */
	void executeAndSleep() throws InterruptedException;

	/**
	 * 将当前执行器连接至指定的转换器，使当前执行器接受指定转换器在其消费过程中所生产出的任务。
	 * <p>
	 * 不允许将非常驻执行器连接至常驻转换器。
	 * 
	 * @param converter
	 *            指定的转换器
	 * @throws IllegalArgumentException
	 *             converter为null
	 * @throws IllegalArgumentException
	 *             当前执行器为非常驻，目标转换器为常驻
	 * @throws IllegalStateException
	 *             当前执行器已终止或正在终止
	 * @throws IllegalStateException
	 *             目标执行器已连接至其他转换器
	 */
	void follow(Converter<? extends Task, T> converter);

	/**
	 * 获取从执行器启动开始到当前为止，已消费完成的任务总数。
	 * 
	 * @return 已消费完成的任务总数
	 */
	long getConsumedTaskCount();

	/**
	 * 获取当前活跃的消费者线程数，即正在执行消费的线程数。
	 * 
	 * @return 当前活跃的消费者线程数
	 */
	int getConsumerThreadActiveCount();

	/**
	 * 获取从执行器启动开始到当前为止，已执行完的消费者线程数。
	 * 
	 * @return 已执行完的消费者线程数
	 */
	long getConsumerThreadCompletedCount();

	/**
	 * 获取当前消费者线程池最大线程数上限。
	 * 
	 * @return 当前消费者线程池最大线程数上限
	 */
	int getConsumerThreadMaxSize();

	/**
	 * 获取当前活跃的生产者线程数，即正在执行生产的线程数。
	 * 
	 * @return 当前活跃的生产者线程数
	 */
	int getProducerThreadActiveCount();

	/**
	 * 获取从执行器启动开始到当前为止，已执行完的生产者线程数。
	 * 
	 * @return 已执行完的生产者线程数
	 */
	long getProducerThreadCompletedCount();

	/**
	 * 获取当前生产者线程池最大线程数上限。
	 * 
	 * @return 当前生产者线程池最大线程数上限
	 */
	int getProducerThreadMaxSize();

	/**
	 * 获取从执行器启动开始到当前为止，进入任务队列的任务总数。
	 * 
	 * @return 进入任务队列的任务总数
	 */
	long getQueuedTaskCount();

	/**
	 * 获取当前任务队列的最大容量。
	 * 
	 * @return 当前任务队列的最大容量
	 */
	int getTaskQueueCapacity();

	/**
	 * 获取当前任务队列中的任务总数。
	 * 
	 * @return 当前任务队列中的任务总数
	 */
	int getTaskQueueSize();

	/**
	 * 判断当前执行器是否已启动过，一旦启动执行器后，该方法将永远返回true，即使执行器正在终止或已终止。
	 * 
	 * @return 已启动过返回true，否则false
	 */
	boolean isExecuted();

	/**
	 * 判断执行器是否输出运行日志。
	 * 
	 * @return 输出返回true，否则false
	 */
	boolean isLoggingExecution();

	/**
	 * 判断执行器是否输出任务日志。
	 * 
	 * @return 输出返回true，否则false
	 */
	boolean isLoggingTask();

	/**
	 * 判断当前执行器是否常驻执行器。
	 * 
	 * @return 是返回true，否则false
	 */
	boolean isResident();

	/**
	 * 判断当前执行器是否已终止。
	 * 
	 * @return 已终止返回true，否则false，常驻执行器永远返回false
	 */
	boolean isTerminated();

	/**
	 * 判断当前执行器是否正在终止或已终止。
	 * 
	 * @return 是返回true，否则false，常驻执行器永远返回false
	 */
	boolean isTerminating();

	/**
	 * 设置消费者线程池最大线程数上限，如果待设置的上限数小于1，该方法将不起任何作用.
	 * <P>
	 * 可在运行时对执行器的消费者线程数上限进行调整。
	 * 
	 * @param size
	 *            待设置的最大线程数上限
	 */
	void setConsumerThreadMaxSize(int size);

	/**
	 * 设置执行器是否输出运行日志。
	 * 
	 * @param isLoggingEnable
	 *            true表示需要输出日志，false表示不输出
	 */
	void setLoggingExecution(boolean isLoggingEnable);

	/**
	 * 设置执行器是否输出任务日志。
	 * 
	 * @param isLoggingEnable
	 *            true表示需要输出日志，false表示不输出
	 */
	void setLoggingTask(boolean isLoggingEnable);

	/**
	 * 设置生产者线程池最大线程数上限，如果待设置的上限数小于1，该方法将不起任何作用.
	 * <P>
	 * 可在运行时对执行器的生产者线程数上限进行调整。
	 * 
	 * @param size
	 *            待设置的最大线程数上限
	 */
	void setProducerThreadMaxSize(int size);

	/**
	 * 向执行器发送终止指令，尝试终止当前执行器。
	 * <p>
	 * 非阻塞方法，调用后立刻返回，执行器接到终止指令后，便停止接受新的任务生产者
	 * ，但不会停止任何已有的生产者，也不会清空任务队列，只有等当前所有生产者完成任务生产、且执行器处理完所有的这些任务之后，才会终止。
	 * <p>
	 * 对于已停止或正在停止的执行器，该方法将不起任何作用。不允许对常驻执行器调用该方法。
	 * <p>
	 * 如果当前执行器存在连接的情况，必须等当前执行器直接或间接连接的所有目标执行器都接收到终止指令后，才能对当前执行器发送终止指令。
	 * 
	 * @throws IllegalStateException
	 *             当前执行器是常驻执行器
	 * @throws IllegalStateException
	 *             至少有一个与当前执行器直接或间接连接的转换器未处于正在终止或以终止状态
	 * 
	 */
	void terminate();

	/**
	 * 向执行器发送终止指令，并等待其终止，阻塞方法，调用后不会立即返回， 直到执行器真正终止后才会返回。
	 * 
	 * @see #terminate()
	 * @throws IllegalStateException
	 *             当前执行器是常驻执行器
	 * @throws IllegalStateException
	 *             至少有一个与当前执行器直接或间接连接的转换器未处于正在终止或以终止状态
	 * @throws InterruptedException
	 *             阻塞过程中当前线程被中断
	 */
	void terminateAndAwait() throws InterruptedException;

	/**
	 * 向执行器发送终止指令，并等待其终止，阻塞方法，调用后不会立即返回， 直到执行器真正终止或等待超时后才会返回。
	 * 
	 * @param timeout
	 *            超时时长
	 * @param unit
	 *            时间单位
	 * @see #terminate()
	 * @throws IllegalStateException
	 *             当前执行器是常驻执行器
	 * @throws IllegalStateException
	 *             至少有一个与当前执行器直接或间接连接的转换器未处于正在终止或以终止状态
	 * @throws InterruptedException
	 *             阻塞过程中当前线程被中断
	 */
	boolean terminateAndAwait(long timeout, TimeUnit unit)
			throws InterruptedException;
}
