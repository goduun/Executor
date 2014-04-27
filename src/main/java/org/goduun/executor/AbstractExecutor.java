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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

import org.goduun.executor.datasource.DataSource;
import org.goduun.executor.datasource.DynamicDataSourceHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;

/**
 * 抽象执行器，实现了执行器的核心功能逻辑。
 * 
 * @author Hu Ruomin
 */
public abstract class AbstractExecutor<T extends Task> implements Executor<T> {

	/**
	 * cpu个数
	 */
	private static final int CPUS = Runtime.getRuntime().availableProcessors();

	/**
	 * 默认的任务队列容量
	 */
	private static final int DEFAULT_TASK_QUEUE_CAPACITY = 5000;

	/**
	 * 消费者线程调度器
	 */
	private Thread bossThread;

	/**
	 * 已消费完成的任务总数
	 */
	private final AtomicLong completedTaskCount = new AtomicLong();

	/**
	 * 执行器控制日志操作对象
	 */
	private final Logger consoleLogger;

	/**
	 * 任务消费者线程池
	 */
	private final ThreadPoolExecutor consumerPool;

	/**
	 * 任务生产者与执行器之间的错误交互管道
	 */
	private final BlockingQueue<ProducingError> errorPipe = new ArrayBlockingQueue<ProducingError>(
			100);

	/**
	 * 当前执行器所接收任务输出的转换器列表
	 */
	private List<Converter<? extends Task, T>> followedConverters;

	/**
	 * 消费者线程是否有任务正在调度，这部分任务不在任务队列中，且尚未提交至消费者线程池
	 */
	private volatile boolean isBossThreadHoldingTasks = false;

	/**
	 * 是否已启动过
	 */
	private volatile boolean isExecuted = false;

	/**
	 * 是否针对每次生产消费的执行过程输出日志
	 */
	private boolean isLoggingExecution = false;

	/**
	 * 是否针对每个任务的生命周期輸出任务日志
	 */
	private boolean isLoggingTask = true;

	/**
	 * 是否常驻执行的执行器，常驻执行器不会被关闭
	 */
	private final boolean isResident;

	/**
	 * 是否正在终止
	 */
	private volatile boolean isTerminating = false;

	/**
	 * 用于实现执行器本身的线程安全
	 */
	private final Lock lock = new ReentrantLock();

	/**
	 * 任务生产者线程池最大线程数
	 */
	private volatile int maxConsumerPoolSize = CPUS;

	/**
	 * 任务生产者线程池最大线程数
	 */
	private volatile int maxProducerPoolSize = CPUS;

	/**
	 * 该执行器的名称
	 */
	private final String name;

	/**
	 * 任务生产者线程池
	 */
	private final ThreadPoolExecutor producerPool;

	/**
	 * 任务生产者列表
	 */
	private List<TaskProducer<T>> producers;

	/**
	 * 输出生产错误日志的线程
	 */
	private Thread producingLoggerThread;

	/**
	 * 任务生产者向当前执行器传输任务的传送管道
	 */
	private final TaskPipe<T> taskPipe;

	/**
	 * 任务队列
	 */
	private final TaskQueue<T> taskQueue;

	/**
	 * 构造函数
	 * 
	 * @param name
	 *            执行器名称
	 * @param consoleClass
	 *            执行器宿主的类型，用于构造执行器的日志对象，输出执行的运行及错误日志
	 * @param isResident
	 *            是否常驻执行器，常驻执行器不允许被终止也永远不会终止
	 * @param maxProducerPoolSize
	 *            生产者线程池的最大线程数，如小于1，将默认为当前可用的cpu个数
	 * @param maxConsumerPoolSize
	 *            消费者线程池的最大线程数，如小于1，将默认为当前可用的cpu个数
	 * @param taskQueue
	 *            任务队列，如果为null，将默认使用 {@link LocalTaskQueue}，默认容量为：5000
	 */
	protected AbstractExecutor(String name, Class<?> consoleClass,
			boolean isResident, int maxProducerPoolSize,
			int maxConsumerPoolSize, TaskQueue<T> taskQueue) {
		this.name = null == name ? "no name" : name;

		if (null != consoleClass) {
			this.consoleLogger = LoggerFactory.getLogger(consoleClass);
		} else {
			this.consoleLogger = null;
		}

		this.isResident = isResident;

		// 初始化生产者与消费者线程池
		this.maxProducerPoolSize = (maxProducerPoolSize < 1) ? this.maxProducerPoolSize
				: maxProducerPoolSize;
		this.maxConsumerPoolSize = (maxConsumerPoolSize < 1) ? this.maxConsumerPoolSize
				: maxConsumerPoolSize;
		producerPool = new ThreadPoolExecutor(this.maxProducerPoolSize,
				this.maxProducerPoolSize, 0L, TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<Runnable>());
		consumerPool = new ThreadPoolExecutor(this.maxConsumerPoolSize,
				this.maxConsumerPoolSize, 0L, TimeUnit.MILLISECONDS,
				new SynchronousQueue<Runnable>());

		// 初始化任务队列
		if (null == taskQueue) {
			this.taskQueue = new LocalTaskQueue<T>(DEFAULT_TASK_QUEUE_CAPACITY);
		} else {
			this.taskQueue = taskQueue;
		}
		this.taskPipe = new TaskPipe<T>(this.taskQueue, this);
	}

	/**
	 * 
	 * {@inheritDoc}
	 * 
	 * @param producer
	 *            生产者对象
	 * @throws IllegalArgumentException
	 *             producer为空
	 * @throws IllegalStateException
	 *             当前执行器已终止或正在终止
	 * @throws IllegalStateException
	 *             该生产者对象已被添加过
	 */
	public void addProducer(TaskProducer<T> producer) {
		if (isTerminating()) {
			throw new IllegalStateException(
					"executor couldnot accept producer after terminate");
		}
		if (null == producer) {
			throw new IllegalArgumentException("arguments is null.");
		}
		if (checkDuplicateProducer(producer)) {
			throw new IllegalStateException(
					"executor do not accept duplicate producer");
		}
		getLock().lock();
		try {
			if (null == producers) {
				producers = new LinkedList<TaskProducer<T>>();
			}
			producers.add(producer);
			if (isExecuted()) {
				submitProducerThread(producer);
			}
		} finally {
			getLock().unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @throws IllegalStateException
	 *             如果当前执行器正在终止或已终止
	 * @throws IllegalStateException
	 *             如果当前执行器没有设置消费者
	 */
	@Override
	public void execute() {
		if (isTerminating()) {
			throw new IllegalStateException(
					"executor cannot re-execute after termination");
		}
		if (isExecuted()) {
			return;
		}
		if (null == getConsumer()) {
			throw new IllegalStateException(
					"at least one cunsumer should be setted before execute");
		}
		lock.lock();
		try {
			isExecuted = true;
			// 初始化生产者日志器线程
			producingLoggerThread = new Thread(new ProducingLoggerRunner());
			producingLoggerThread.setName("producingLogger-" + name);
			producingLoggerThread.setDaemon(true);
			producingLoggerThread.start();

			// 初始化调度器线程
			bossThread = new Thread(new BossRunner(getConsumer()));
			bossThread.setName("boss-" + name);
			bossThread.setDaemon(true);
			bossThread.start();

			// 启动生产者线程
			if (null != producers) {
				for (TaskProducer<T> producer : producers) {
					submitProducerThread(producer);
				}
			}
		} finally {
			lock.unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @throws IllegalStateException
	 *             如果当前执行器不是常驻执行器
	 * @throws IllegalStateException
	 *             如果当前执行器没有设置消费者
	 * @throws InterruptedException
	 *             休眠过程中当前线程被中断
	 */
	public void executeAndSleep() throws InterruptedException {
		if (!isResident()) {
			throw new IllegalStateException();
		}
		execute();
		LockSupport.park();
		if (Thread.interrupted()) {
			throw new InterruptedException();
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @throws IllegalArgumentException
	 *             converter为null
	 * @throws IllegalArgumentException
	 *             当前执行器为非常驻，目标转换器为常驻
	 * @throws IllegalStateException
	 *             当前执行器已终止或正在终止
	 * @throws IllegalStateException
	 *             目标执行器已连接至其他转换器
	 */
	@Override
	public void follow(Converter<? extends Task, T> converter) {
		if (isTerminating()) {
			throw new IllegalStateException(
					"executor do not accept follow operation after termination");
		}
		if (null == converter) {
			throw new IllegalArgumentException("arguments is null");
		}
		if (converter.isConverted()) {
			throw new IllegalStateException(
					"target is already converted to another one");
		}
		if (!isResident() && converter.isResident()) {
			throw new IllegalArgumentException(
					"following non-resident executor to resident is not permitted");
		}
		lock.lock();
		try {
			converter.convertTaskTo(taskPipe);
			if (null == followedConverters) {
				followedConverters = new LinkedList<Converter<? extends Task, T>>();
			}
			followedConverters.add(converter);
		} finally {
			lock.unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getConsumedTaskCount() {
		return completedTaskCount.get();
	}

	/**
	 * 获取当前执行器的消费者。
	 * 
	 * @return 当前执行器的消费者
	 */
	public abstract TaskProcessor<T> getConsumer();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getConsumerThreadActiveCount() {
		return consumerPool.getActiveCount();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getConsumerThreadCompletedCount() {
		return consumerPool.getCompletedTaskCount();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getConsumerThreadMaxSize() {
		return maxConsumerPoolSize;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getProducerThreadActiveCount() {
		return producerPool.getActiveCount();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getProducerThreadCompletedCount() {
		return producerPool.getCompletedTaskCount();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getProducerThreadMaxSize() {
		return maxProducerPoolSize;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getQueuedTaskCount() {
		return taskPipe.count();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getTaskQueueCapacity() {
		return taskQueue.capacity();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getTaskQueueSize() {
		return taskQueue.size();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isExecuted() {
		return isExecuted;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isLoggingExecution() {
		return isLoggingExecution;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isLoggingTask() {
		return isLoggingTask;
	}

	/**
	 * {@inheritDoc}
	 * 
	 */
	@Override
	public boolean isResident() {
		return isResident;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isTerminated() {
		if (!isTerminating()) {
			return false;
		}

		int producerSum = null == producers ? 0 : producers.size();
		if (producerSum > producerPool.getCompletedTaskCount()) {
			return false;
		} else if (taskQueue.size() > 0 || consumerPool.getPoolSize() > 0
				|| isBossThreadHoldingTasks) {
			return false;
		} else if (null != followedConverters) {
			for (Converter<? extends Task, T> followedExecutor : followedConverters) {
				if (!followedExecutor.isTerminated()) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isTerminating() {
		return isTerminating;
	}

	/**
	 * 输出执行器在执行过程中不可预见的异常信息，主要用于执行器本身的debug。
	 * 
	 * @param message
	 *            异常描述
	 * @param e
	 *            异常
	 */
	public void logException(String message, Throwable e) {
		if (null != consoleLogger && (null != e || null != message)) {
			consoleLogger.error(message, e);
		}
	}

	/**
	 * 输出异常的执行日志。
	 * 
	 * @param message
	 *            日志文本
	 * @param e
	 *            异常
	 */
	public void logExecutingFail(String message, Throwable e) {
		if (null == consoleLogger || !isLoggingExecution()) {
			return;
		}
		consoleLogger.error(message, e);
	}

	/**
	 * 输出正常的执行日志。
	 * 
	 * @param message
	 *            日志文本
	 */
	public void logExecutingSuccess(String message) {
		if (null == consoleLogger || !isLoggingExecution()) {
			return;
		}
		consoleLogger.info(message);
	}

	/**
	 * 输出任务唤醒环节的任务日志。
	 * 
	 * @param ancestor
	 *            被唤醒的子任务
	 * @param task
	 *            触发唤醒的任务
	 * @param elapsedMillis
	 *            唤醒耗时
	 */
	public void logTaskAwaking(Task ancestor, Task task, long elapsedMillis) {
		if (!isLoggingTask()) {
			return;
		}
		Logger log = LoggerHolder.get(ancestor.getClass());
		String logInfo = LogFormator.formatAwakingInfo(ancestor, task,
				elapsedMillis);
		if (ancestor.isFailedToBeAwaked()) {
			log.error(logInfo, ancestor.getFailedCauseOfAwaking());
		} else {
			log.info(logInfo);
		}
	}

	/**
	 * 输出任务完成环节的任务日志。
	 * 
	 * @param task
	 *            指定的任务
	 */
	public void logTaskCompletion(Task task) {
		if (!isLoggingTask()) {
			return;
		}
		Logger log = LoggerHolder.get(task.getClass());
		String info = LogFormator.formatCompletionInfo(task);
		if (task.isFailed()) {
			log.error(info, task.getFailedCause());
		} else {
			log.info(info);
		}
	}

	/**
	 * 输出任务生成环节的任务日志。
	 * 
	 * @param task
	 *            指定的任务
	 */
	public void logTaskGeneration(Task task) {
		if (!isLoggingTask()) {
			return;
		}
		Logger log = LoggerHolder.get(task.getClass());
		String info = LogFormator.formatGenerationInfo(task);
		if (task.isFailed()) {
			log.error(info, task.getFailedCause());
		} else {
			log.info(info);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setConsumerThreadMaxSize(int size) {
		if (0 < size) {
			maxConsumerPoolSize = size;
			consumerPool.setCorePoolSize(maxConsumerPoolSize);
			consumerPool.setMaximumPoolSize(maxConsumerPoolSize);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setLoggingExecution(boolean isLoggingEnable) {
		isLoggingExecution = isLoggingEnable;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setLoggingTask(boolean isLoggingEnable) {
		isLoggingTask = isLoggingEnable;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setProducerThreadMaxSize(int size) {
		if (0 < size) {
			maxProducerPoolSize = size;
			producerPool.setCorePoolSize(maxProducerPoolSize);
			producerPool.setMaximumPoolSize(maxProducerPoolSize);
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @throws IllegalStateException
	 *             当前执行器是常驻执行器
	 * @throws IllegalStateException
	 *             至少有一个与当前执行器直接或间接连接的转换器未处于正在终止或以终止状态
	 */
	@Override
	public void terminate() {
		if (isTerminating) {
			return;
		}
		if (isResident()) {
			throw new IllegalStateException(
					"resident executor cannot be terminated");
		}
		lock.lock();
		try {
			if (null != followedConverters) {
				for (Converter<? extends Task, T> followedExecutor : followedConverters) {
					if (!followedExecutor.isTerminating()) {
						throw new IllegalStateException();
					}
				}
			}
			isTerminating = true;
			producerPool.setKeepAliveTime(1L, TimeUnit.MILLISECONDS);
			consumerPool.setKeepAliveTime(1L, TimeUnit.SECONDS);
			producerPool.allowCoreThreadTimeOut(true);
			consumerPool.allowCoreThreadTimeOut(true);
		} finally {
			lock.unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @throws IllegalStateException
	 *             当前执行器是常驻执行器
	 * @throws IllegalStateException
	 *             至少有一个与当前执行器直接或间接连接的转换器未处于正在终止或以终止状态
	 * @throws InterruptedException
	 *             阻塞过程中当前线程被中断
	 */
	@Override
	public void terminateAndAwait() throws InterruptedException {
		terminate();
		while (!isTerminated()) {
			TimeUnit.SECONDS.sleep(1);
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @throws IllegalStateException
	 *             当前执行器是常驻执行器
	 * @throws IllegalStateException
	 *             至少有一个与当前执行器直接或间接连接的转换器未处于正在终止或以终止状态
	 * @throws InterruptedException
	 *             阻塞过程中当前线程被中断
	 */
	@Override
	public boolean terminateAndAwait(long timeout, TimeUnit unit)
			throws InterruptedException {
		terminate();
		long start = System.currentTimeMillis();
		long millis = unit.toMillis(timeout);
		while (!isTerminated()) {
			TimeUnit.SECONDS.sleep(1);
			if ((start + millis) > System.currentTimeMillis()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 判断指定的生产者对象是否已被添加至执行器.
	 * 
	 * @param producer
	 *            指定的生产者对象
	 * @return 已添加，返回true；<br>
	 *         参数producer为null，返回false；<br>
	 *         未被添加，返回false；
	 */
	private boolean checkDuplicateProducer(TaskProducer<T> producer) {
		if (null == producer) {
			return false;
		}
		lock.lock();
		try {
			if (null == producers) {
				return false;
			}
			for (TaskProducer<T> existingProducer : producers) {
				if (producer == existingProducer) {
					return true;
				}
			}
			return false;
		} finally {
			lock.unlock();
		}
	}

	private TaskState getTaskState(Task task) {
		if (null == task) {
			return null;
		}

		return task.getTaskState();
	}

	/**
	 * 将生产者提交至线程池执行，如果当前执行器已终止或正在终止，该操作将不被允许，如果当前执行器被强制只消费，该操作不起任何作用
	 * 
	 * @param producer
	 *            生产者对象
	 * @throws IllegalArgumentException
	 *             producer为null
	 * @throws IllegalArgumentException
	 *             该producer已被添加过
	 * @throws IllegalStateException
	 *             当前执行器已终止或正在终止
	 */
	private void submitProducerThread(TaskProducer<T> producer) {
		if (isTerminating()) {
			throw new IllegalStateException(
					"executor do not accept producer after termination");
		}
		if (null == producer) {
			throw new IllegalArgumentException("the producer is null");
		}
		producerPool.execute(new ProducerRunner(producer));
	}

	/**
	 * 获取当前执行器用于同步控制的锁。
	 * 
	 * @return 当前执行器的锁
	 */
	protected Lock getLock() {
		return lock;
	}

	/**
	 * 消费者线程调度类，主要的工作分为两步，第一步取任务，第二步把任务交给消费者，向线程池提交消费者线程。 总体原则是：
	 * <ol>
	 * <li>交给消费者的整批任务必须是同一数据源</li>
	 * <li>尽可能的满足消费者的最大处理能力</li>
	 * <li>如果满足不了，也不能让消费者线程等太久</li>
	 * </ol>
	 * 
	 * 为满足整批任务同数据源的要求，为每个数据源设置一个任务缓存池，用于积累同数据源的任务。<br>
	 * 主要的逻辑在取任务上，详细步骤如下：
	 * <ol>
	 * <li>取任务前，先判断是否有缓存，如果有缓存，进入步骤2；如果没有缓存，采用阻塞的方法从任务队列取任务，取到后缓存该任务，进入步骤2</li>
	 * <li>判断是否有某个数据源的任务缓存数达到了消费者的最大处理能力，如果有，进入步骤4，如果没有，进入步骤3</li>
	 * <li>采用非阻塞的方式不断从任务队列中取任务并缓存，直到以下任一条件满足：
	 * <ol>
	 * <li>任务队列被取尽</li>
	 * <li>有某个数据源的缓存队列任务数达到了消费者的最大处理能力</li>
	 * </ol>
	 * </li>
	 * <li>从最大的缓存队列中，取足够(或取尽、或数量达到消费者的最大处理能力)的任务交给消费者，提交消费者线程</li>
	 * </ol>
	 * 
	 * 注意：这不是个线程安全的类<br>
	 * 注意：缓存使用了无界队列，如果数据源多的离谱，并且消费者的处理能力很大，会导致内存占用过高
	 * 
	 * @author Hu Ruomin
	 */
	private class BossRunner implements Runnable {

		/**
		 * 当调度线程无法向线程池提交任务时，等待的时长，单位：纳秒
		 */
		private static final long PARK_NANOS = 1000 * 100;

		/**
		 * 用于缓存多数据源的情况下各数据源的任务<br>
		 * 注意：缓存使用了无界队列，如果数据源多的离谱，并且消费者的处理能力很大，可能导致内存占用过高
		 */
		private final Map<DataSource, LinkedList<T>> cachedTaskMap = new HashMap<DataSource, LinkedList<T>>();

		/**
		 * 消费者对象
		 */
		private final TaskProcessor<T> consumer;

		/**
		 * 构造函数
		 * 
		 * @param consumer
		 *            消费者对象
		 * @throws IllegalArgumentException
		 *             consumer为null
		 */
		public BossRunner(TaskProcessor<T> consumer) {
			if (null == consumer) {
				throw new IllegalArgumentException();
			}
			this.consumer = consumer;
		}

		/**
		 * 执行调度
		 */
		@Override
		public void run() {
			while (true) {
				DataSource maxCachedDataSourceEnum = getMaxCachedDataSourceEnum();
				// 如果有缓存
				if (null != cachedTaskMap.get(maxCachedDataSourceEnum)
						&& 0 < cachedTaskMap.get(maxCachedDataSourceEnum)
								.size()) {
					int maxCachedSize = cachedTaskMap.get(
							maxCachedDataSourceEnum).size();

					// 如果缓存不足，尝试从任务队列中取任务塞缓存，直到任务队列取尽，或者缓存充足
					if (maxCachedSize < consumerCapacity()) {
						T task;
						do {
							task = taskQueue.poll();
							if (null != task) {
								int thisCachedSize = cacheTask(task);
								if (thisCachedSize > maxCachedSize) {
									maxCachedSize = thisCachedSize;
									maxCachedDataSourceEnum = task
											.getDefaultDataSource();
								}
							}
						} while (null != task
								&& maxCachedSize < consumerCapacity());
					}

					/*
					 * 此时，以下2个条件必然有一个已满足： 1.缓存充足 2.缓存不充足但队列取尽，因此，提交消费者线程
					 */
					try {
						submitConsumerThread(maxCachedDataSourceEnum);
					} catch (InterruptedException e) {
						logException(null, e);
						return;
					}

					// 如果没缓存,用阻塞的方式取任务，取到任务后，结束本次循环，下一次循环会解决问题
				} else {
					T task;
					try {
						task = taskQueue.take();
					} catch (InterruptedException e) {
						logException(null, e);
						return;
					}
					cacheTask(task);
				}
			}
		}

		/**
		 * 缓存任务
		 * 
		 * @param task
		 *            需缓存的任务
		 * @return 该任务被缓存后，对应缓存队列的大小，task为null时返回0
		 */
		private int cacheTask(T task) {
			if (null != task) {
				DataSource dataSource = task.getDefaultDataSource();
				LinkedList<T> cacheList = cachedTaskMap.get(dataSource);
				if (null == cacheList) {
					cacheList = new LinkedList<T>();
					cachedTaskMap.put(dataSource, cacheList);
				}
				cacheList.add(task);
				return cacheList.size();
			}
			return 0;
		}

		/**
		 * 获取消费者的最大处理能力
		 * 
		 * @return 消费者的最大处理能力
		 */
		private int consumerCapacity() {
			try {
				return consumer.getCapacity() > 0 ? consumer.getCapacity() : 1;
			} catch (Exception e) {
				logException(null, e);
				return 1;
			}
		}

		/**
		 * 获取当前缓存了任务数最多的数据源<br>
		 * 
		 * @return 当前缓存了任务数最多的数据源
		 */
		private DataSource getMaxCachedDataSourceEnum() {
			DataSource maxDataSourceEnum = null;
			int max = 0;
			for (Map.Entry<DataSource, LinkedList<T>> entry : cachedTaskMap
					.entrySet()) {
				if (null != entry.getValue() && entry.getValue().size() > max) {
					max = entry.getValue().size();
					maxDataSourceEnum = entry.getKey();
				}
			}
			return maxDataSourceEnum;
		}

		/**
		 * 把消费者提交到线程池执行，如果线程池满，休眠当前线程，待唤醒后，继续尝试提交，直到提交成功为止。<br>
		 * 如果参数非法，该方法不起任何作用
		 * 
		 * @param fromWhichDataSourceEnum
		 *            从哪个数据源的缓存取任务提交
		 * @throws InterruptedException
		 *             当阻塞时线程被中断
		 */
		private void submitConsumerThread(DataSource fromWhichDataSourceEnum)
				throws InterruptedException {
			// 如果没有任务可提交消费者线程池，直接返回
			if (null == cachedTaskMap.get(fromWhichDataSourceEnum)
					|| 0 == cachedTaskMap.get(fromWhichDataSourceEnum).size()) {
				return;
			}

			// 取指定数据源的任务并提交线程池
			isBossThreadHoldingTasks = true;
			try {
				LinkedList<T> from = cachedTaskMap.get(fromWhichDataSourceEnum);
				int fromSize = from.size();
				List<T> tasks = new LinkedList<T>();
				for (int i = 0; i < consumerCapacity() && i < fromSize; i++) {
					tasks.add(from.removeFirst());
				}
				while (true) {
					try {
						consumerPool.execute(new ConsumerRunner(consumer,
								tasks, fromWhichDataSourceEnum));
						break;
					} catch (RejectedExecutionException e) {
						LockSupport.parkNanos(PARK_NANOS);
					}
					if (Thread.interrupted()) {
						throw new InterruptedException();
					}
				}
			} finally {
				isBossThreadHoldingTasks = false;
			}
		}

	}

	/**
	 * 消费者线程类
	 * 
	 * @author Hu Ruomin
	 */
	private class ConsumerRunner implements Runnable {

		/**
		 * 消费者对象
		 */
		private final TaskProcessor<T> consumer;

		/**
		 * 消费者的默认数据源
		 */
		private final DataSource defaultDataSource;

		/**
		 * 秒表
		 */
		private Stopwatch stopwatch = new Stopwatch();

		/**
		 * 传送给消费者的任务列表
		 */
		private final TaskList<T> taskList;

		/**
		 * 待消费者处理的任务
		 */
		private final List<T> tasks;

		/**
		 * 构造函数
		 * 
		 * @param consumer
		 *            消费者对象
		 * @param tasks
		 *            待消费者处理的任务
		 * @param defaultDataSource
		 *            处理这批任务所使用的数据源
		 * @throws IllegalArgumentException
		 *             任一参数为null
		 * @throws IllegalArgumentException
		 *             tasks.size() <= 0
		 */
		public ConsumerRunner(TaskProcessor<T> consumer, List<T> tasks,
				DataSource defaultDataSource) {
			if (null == consumer || null == tasks) {
				throw new IllegalArgumentException();
			} else if (0 >= tasks.size()) {
				throw new IllegalArgumentException();
			}
			this.consumer = consumer;
			this.tasks = tasks;
			for (T task : tasks) {
				task.getTaskState().setExecutedConsumer(consumer);
			}
			this.taskList = new TaskList<T>(tasks, AbstractExecutor.this);
			this.defaultDataSource = defaultDataSource;
		}

		/**
		 * 执行消费者，处理任务
		 */
		@Override
		public void run() {
			try {
				setThreadName();
				DynamicDataSourceHolder.change(defaultDataSource);
				// 执行消费，输出日志
				try {
					stopwatch.start();
					consumer.execute(taskList);
					stopwatch.stop();
					logExecutingSuccess(LogFormator
							.formatConsumingSuccessedInfo(consumer.getClass(),
									stopwatch.elapsedMillis()));
				} catch (Exception e) {
					stopwatch.stop();
					logExecutingFail(
							LogFormator.formatConsumingFailedInfo(
									consumer.getClass(),
									stopwatch.elapsedMillis()), e);
				}

				try {
					// 根据需要输出任务完成日志
					logTasksCompletion(tasks);
					// 处理唤醒
					handleAwaking(tasks);
				} catch (Exception e) {
					logException(null, e);
				}

				// 增加已完成的任务总数
				completedTaskCount.addAndGet(taskList.size());
			} finally {
				stopwatch.reset();
				LockSupport.unpark(bossThread);
			}
		}

		/**
		 * 判断指定祖先任务是否完成了指定任务类型的子任务生产
		 * 
		 * @param sonClass 指定的子任务类型
		 * @param ancestor 指定的祖先任务
		 * @return
		 */
		private boolean checkGeneration(Class<? extends Task> sonClass,
				Task ancestor) {
			TaskState ancestorState = ancestor.getTaskState();

			Class<? extends Task> parentClass = TaskState.getParentClass(sonClass);
			if (parentClass == ancestor.getClass()) {
				return true;
			}

			boolean isCheckOK = true;
			while (parentClass != ancestor.getClass() && null != parentClass) {
				try {
					if (0L < ancestorState
							.getCountOfUnAwakedSubtask(parentClass)) {
						isCheckOK = false;
					} else if (!ancestorState.isAllSubtaskGenerated(parentClass)) {
						ancestorState.addWaitingForRecheckSubclass(parentClass, sonClass);
						isCheckOK = false;
					}
				} finally {
					parentClass = TaskState.getParentClass(parentClass);
				}
			}
			return isCheckOK;
		}

		/**
		 * 使指定任务所有祖先针对指定任务类型的未过唤醒环节的子任务数-1
		 * 
		 * @param task 指定任务
		 */
		private void decreaseAncestorUnAwakedSubtask(Task task) {
			Task parent = task.getParent();
			TaskState parentState = getTaskState(parent);
			while (null != parentState) {
				parentState.decreaseUnAwakedSubtask(task.getClass());
				parent = parent.getParent();
				parentState = getTaskState(parent);
			}
		}

		/**
		 * 当任务通过唤醒环节完后的处理，包括：
		 * <p>
		 * (先定义两个名词：当前类型-task的类型，儿子类型-将task视为父任务的任务类型，可能为null)
		 * <p>
		 * 1.使当前任务所有祖先针对当前类型的未过唤醒环节子任务数-1。
		 * <p>
		 * 2.任务通过执行和唤醒环节后，就不会再产生儿子类型的子任务，因此，如果儿子类型不为null，需设置当前任务已完成儿子类型子任务的生产。
		 * <p>
		 * 3.再递归检查当前任务的所有祖先是否已完成儿子类型子任务生产。
		 * <p>
		 * 检查的标准：
		 * <p>
		 * a.祖先是否还存在其他未过唤醒环节的儿子类型子任务，如果存在，该祖先就没有完成儿子类型子任务的生产，检查失败。
		 * <p>
		 * b.再从任务类型关系链上来看，从祖先类型开始（不包括祖先类型），一直到儿子类型的上一级，也就是当前类型，祖先是否已完成这些类型的子任务生产
		 * ，如果完成，那么该祖先就可以判定为已完成了儿子类型的子任务生产，检查成功。如果没有完成，那么当祖先在未来某时完成这类子任务生产时，
		 * 需要再回头检查一下完成了该儿子类型的子任务生产。
		 * <p>
		 * 针对步骤3和4中满足检查标准的任务类型，需要再递归Recheck一下，看这些任务满足标准之后，是否还会使该祖先更多的子任务类型也满足标准。
		 * <p>
		 * 最终，能够获取到一份该祖先已完成生产的子任务类型列表，再看祖先针对这些类型的子任务全部生产完成后，
		 * 是否需要触发对应的子任务进行唤醒该祖先任务，如果需要，就唤醒。
		 * <p>
		 * 
		 * @param task
		 * @throws InterruptedException
		 */
		private void doAfterAwaked(Task task) throws InterruptedException {
			if (null == task || null == task.getTaskState()) {
				throw new IllegalArgumentException();
			}

			// 当前任务所有祖先针对当前类型的未过唤醒环节的子任务数-1
			decreaseAncestorUnAwakedSubtask(task);

			// 儿子类型
			Class<? extends Task> sonClass = TaskState.getSubClass(task
					.getClass());
			if (null == sonClass) {
				return;
			}

			// 这个方法内可能会触发当前任务的祖先任务被其他子任务唤醒，该变量用来存放触发的唤醒事件
			// Map<Task, Task> - 一个唤醒事件， 其中：Task - 被唤醒的祖先任务， Task - 触发唤醒的子任务
			List<Map<Task, Task>> awakingEvents = new LinkedList<Map<Task, Task>>();

			Task parent = task;
			TaskState parentState = getTaskState(parent);
			while (null != parentState) {
				synchronized (parentState) {
					try {
						// 如果儿子类型针对当前祖先检查不成功，跳过该祖先
						if (!checkGeneration(sonClass, parent)) {
							continue;
						}
						
						// 初始化针对该祖先通过检查的子任务类型结果
						List<Class<? extends Task>> checkPassedResult = new LinkedList<Class<? extends Task>>();

						// 设置该祖先已完成了儿子类型的子任务的生产，并将儿子任务放入检查OK的结果中
						parentState.setAllSubtaskGenerated(sonClass);
						checkPassedResult.add(sonClass);

						// 再Recheck一下，儿子类型子任务全部生产后，是否还使得其他类型的子任务也完成了生产
						List<Class<? extends Task>> recheckPassedResults = parentState
								.recheckSubtaskGeneration(sonClass);
						if (null != recheckPassedResults) {
							checkPassedResult.addAll(recheckPassedResults);
						}

						// 到此为止，针对该祖先的所有已完成生产的子任务类型已经全部拿到，下面开始判断是否需要触发唤醒事件
						for (Class<? extends Task> passedSubclass : checkPassedResult) {
							// 先判断该祖先任务的等待唤醒列表中，是否有相应类型的子任务
							Task passedSubtask = parentState
									.removeWaitingSubtask(passedSubclass);
							TaskState passedtaskState = getTaskState(passedSubtask);

							// 如果没有，放弃此次循环，检查下一个子任务类型
							if (null == passedtaskState) {
								continue;
							}

							// 如果有，再判断一下该祖先任务是否已被唤醒过，以及该祖先任务是否还存在未过执行环节的子任务，如果都OK，那就可以唤醒
							if (parentState.isBeenAwaked(passedSubclass) || 0L < parentState
									.getCountOfUnExecutedSubtask(passedSubclass)) {
								// 如果当前任务无法触发唤醒，下面的操作一定要做
								decreaseAncestorUnAwakedSubtask(passedSubtask);
								continue;
							}

							passedtaskState.decreaseWaitingForAwakeAncestor();
							Map<Task, Task> awakingEvent = new HashMap<Task, Task>();
							awakingEvent.put(passedSubtask, parent);
							awakingEvents.add(awakingEvent);
							parentState.setBeenAwaked(passedSubclass);
						}
					} finally {
						parent = parent.getParent();
						parentState = getTaskState(parent);
					}
				}
			}

			// 触发唤醒
			for (Map<Task, Task> awakingEvent : awakingEvents) {
				for (Map.Entry<Task, Task> entry : awakingEvent.entrySet()) {
					Task sub = entry.getKey();
					Task ancestor = entry.getValue();
					doAwake(ancestor, sub);
					doAfterAwaked(sub);
				}
			}
		}

		/**
		 * 当任务通过执行环节后的处理，包括：
		 * <p>
		 * 1.使当前任务所有祖先的未过执行环节子任务数-1
		 * <p>
		 * 2.任务通过执行环节后就可触发唤醒，因此需判断当前任务是否需要唤醒祖先，如果需要，就唤醒祖先
		 * <p>
		 * 判断的标准：
		 * <p>
		 * a.祖先的未过执行环节子任务数-1后不大于0
		 * <p>
		 * b.祖先的子任务已全部生产完成
		 * <p>
		 * 
		 * @param task
		 *            执行完的任务
		 * @throws InterruptedException
		 *             如果线程休眠时被中断
		 * @throws IllegalArgumentException
		 *             如果任一参数为null
		 */
		private void doAfterExecuted(Task task) throws InterruptedException {
			if (null == task || null == task.getTaskState()) {
				throw new IllegalArgumentException();
			}

			// 用来存放需要被唤醒的祖先
			List<Task> awakableAncestors = new LinkedList<Task>();

			Class<? extends Task> taskClass = task.getClass();
			TaskState taskState = task.getTaskState();
			
			Task parent = task.getParent();
			TaskState parentState = getTaskState(parent);

			// 递归找所有祖先任务
			while (null != parentState) {
				try {
					// 对祖先的操作和判断均需要同步，但唤醒不能放在同步里，因为唤醒过程中可能会阻塞，从而死锁
					synchronized (parentState) {
						if (0L < parentState
								.decreaseUnExecutedSubtask(taskClass)) {
							continue;
						}

						if (!consumer.getAwakableClasses().contains(
								parent.getClass())
								|| parentState.isBeenAwaked(taskClass)) {
							continue;
						}

						if (!parentState.isAllSubtaskGenerated(taskClass)) {
							if (parentState.addWaitingSubtask(task)) {
								taskState.increaseWaitingForAwakeAncestor();
							}
							continue;
						}

						// 在同步语句块内必须设置祖先已被唤醒过，否则一出同步块就可能被其他子任务唤醒
						parentState.setBeenAwaked(taskClass);
						awakableAncestors.add(parent);
						
					}
				} finally {
					parent = parent.getParent();
					parentState = getTaskState(parent);
				}
			}

			for (Task awakableAncestor : awakableAncestors) {
				doAwake(awakableAncestor, task);
			}
		}

		/**
		 * 执行唤醒。
		 * 
		 * @param ancestor
		 *            待唤醒的祖先任务
		 * @param task
		 *            触发唤醒的子任务
		 * @throws InterruptedException
		 *             如果线程休眠时被中断
		 * @throws IllegalArgumentException
		 *             如果任一参数为null
		 */
		private void doAwake(Task ancestor, Task task)
				throws InterruptedException {
			if (null == task || null == ancestor) {
				throw new IllegalArgumentException();
			}

			stopwatch.reset();
			stopwatch.start();
			try {
				if (null != ancestor.getDefaultDataSource()) {
					DynamicDataSourceHolder.change(ancestor
							.getDefaultDataSource());
				}
				task.getTaskState().getExecutedConsumer().awake(ancestor, task);
			} catch (Exception e) {
				logException(null, e);
			} finally {
				stopwatch.stop();
				logTaskAwaking(ancestor, task, stopwatch.elapsedMillis());
			}

			// 如果唤醒过程中抛出的异常是InterruptedException，则继续抛出
			if (task.getFailedCauseOfAwaking() instanceof InterruptedException) {
				throw (InterruptedException) task.getFailedCauseOfAwaking();
			}
		}

		/**
		 * 处理任务唤醒。
		 * 
		 * @param tasks
		 *            待处理的任务
		 * @throws InterruptedException
		 *             如果线程休眠时被中断
		 */
		private void handleAwaking(List<T> tasks) throws InterruptedException {
			if (null == tasks) {
				return;
			}

			for (T task : tasks) {
				if (null == task) {
					continue;
				}
				
				doAfterExecuted(task);

				// 如果当前任务不再需要做唤醒操作
				if (0 == getTaskState(task).getCountOfWaitingForAwakeAncestor()) {
					doAfterAwaked(task);
				}
				
				
			}
		}

		/**
		 * 输出指定任务的正常结束或失败结束记录，只有当消费者使用了任务列表的一次性获取方式时，该方法才会输出日志
		 * 
		 * @param tasks
		 *            待输出日志的任务
		 */
		private void logTasksCompletion(List<T> tasks) {
			if (!taskList.toListInvoked()
					|| !AbstractExecutor.this.isLoggingTask()) {
				return;
			}
			if (null == tasks || 0 == tasks.size()) {
				return;
			}

			for (T task : tasks) {
				logTaskCompletion(task);
			}
		}

		/**
		 * 设置线程名，格式：消费者类名(yyyy-MM-dd HH:mm:ss, 待消费的任务数)<br>
		 * 不抛出任何异常
		 */
		private void setThreadName() {
			try {
				Thread.currentThread().setName(
						consumer.getClass().getSimpleName() + "("
								+ tasks.size() + ")");
			} catch (Exception e) {
				if (null != consoleLogger) {
					consoleLogger.error(null, e);
				}
			}
		}
	}

	/**
	 * 任务生产者线程类，在消费者执行前，会将数据源切换至生产者{@link TaskProducer#getDefaultDataSource()}
	 * 方法返回值所对应的数据源，如果返回null，且整个进程有默认数据源，则切换至默认数据源
	 * 
	 * @author Hu Ruomin
	 */
	private class ProducerRunner implements Runnable {

		/**
		 * 生产者对象
		 */
		private final TaskProducer<T> producer;

		/**
		 * 任务生产者向当前执行器传输错误的传送管道
		 */
		private final ProducingErrorPipe producingErrorPipe = new ProducingErrorPipe(
				errorPipe);

		/**
		 * 秒表
		 */
		private final Stopwatch stopwatch = new Stopwatch();

		/**
		 * 构造函数
		 * 
		 * @param producer
		 *            生产者对象
		 * @throws IllegalArgumentException
		 *             producer为null
		 */
		public ProducerRunner(TaskProducer<T> producer) {
			if (null == producer) {
				throw new IllegalArgumentException();
			}
			this.producer = producer;
		}

		/**
		 * 执行任务生产者
		 */
		@Override
		public void run() {
			setThreadName();
			try {
				// 如果生产者指定了数据源，就切换至该数据源；如果没有，但系统有默认数据源设置，就切换至默认数据源
				if (null != producer.getDefaultDataSource()) {
					DynamicDataSourceHolder.change(producer
							.getDefaultDataSource());
				} else if (null != DynamicDataSourceHolder.getDefault()) {
					DynamicDataSourceHolder.change(DynamicDataSourceHolder
							.getDefault());
				}

				stopwatch.start();
				producer.execute(taskPipe, producingErrorPipe);
				stopwatch.stop();
				logExecutingSuccess(LogFormator.formatConsumingSuccessedInfo(
						producer.getClass(), stopwatch.elapsedMillis()));
			} catch (Exception e) {
				stopwatch.stop();
				logExecutingFail(
						LogFormator.formatConsumingFailedInfo(
								producer.getClass(), stopwatch.elapsedMillis()),
						e);
				return;
			} finally {
				// 切换回至默认数据源
				if (null != DynamicDataSourceHolder.getDefault()) {
					DynamicDataSourceHolder.change(DynamicDataSourceHolder
							.getDefault());
				}
			}
		}

		/**
		 * 设置线程名，格式：生产者类名<br>
		 * 不抛出任何异常
		 */
		private void setThreadName() {
			try {
				Thread.currentThread().setName(
						producer.getClass().getSimpleName());
			} catch (Exception e) {
				logException(null, e);
			}
		}
	}

	/**
	 * 任务生产的错误日志输出线程类，该类负责从生产错误传输管道中获取错误信息，输出日志
	 * 
	 * 
	 * @author Hu Ruomin
	 */
	private class ProducingLoggerRunner implements Runnable {

		/**
		 * 获取错误，输出日志
		 */
		@Override
		public void run() {
			Logger log;
			while (true) {
				try {
					ProducingError error = errorPipe.take();
					if (isLoggingTask()) {
						log = LoggerFactory.getLogger(error.getClass());
						log.error(error.getMessage(), error.getCause());
					}
				} catch (InterruptedException e) {
					logException(null, e);
					return;
				}
			}
		}

	}

}
