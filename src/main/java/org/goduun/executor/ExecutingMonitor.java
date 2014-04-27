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

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.goduun.executor.datasource.DataSource;
import org.goduun.executor.datasource.DynamicDataSourceHolder;

/**
 * 执行器状态监控器。
 * <p>
 * 定期获取执行器的执行状态，并通过监控实施器完成对执行器的监控。
 * <p>
 * 监控器实现了Runnable接口，推荐将监控器应作为一个daemon线程来启动。
 * 
 * @see ExecutingStateMonitor
 * @author Hu Ruomin
 */
public class ExecutingMonitor implements Runnable {

	/**
	 * 默认的监控实施间隔，单位：秒
	 */
	public static final long DEFAULT_MONITORING_INTERVAL = 5;

	/**
	 * 实施监控时所需使用的数据源
	 */
	private DataSource dataSource;

	/**
	 * 待监控对象中的所有执行器
	 */
	private final Map<String, Executor<? extends Task>> executors = new HashMap<String, Executor<? extends Task>>();

	/**
	 * 上一次状态记录时消费完的任务总数
	 */
	private final Map<String, Long> lastConsumedCounts = new HashMap<String, Long>();

	/**
	 * 上一次状态记录时的消费者线程完成总数
	 */
	private final Map<String, Long> lastConsumerThreadCompletedCounts = new HashMap<String, Long>();

	/**
	 * 上一次状态记录时的生产者线程完成总数
	 */
	private final Map<String, Long> lastProducerThreadCompletedCounts = new HashMap<String, Long>();

	/**
	 * 上一次状态记录时进入任务队列的任务总数
	 */
	private final Map<String, Long> lastQueuedCounts = new HashMap<String, Long>();

	/**
	 * 监控实施间隔，单位：秒
	 */
	private long monitoringInterval = DEFAULT_MONITORING_INTERVAL;

	/**
	 * 执行器状态监控实施对象
	 */
	private final ExecutingStateMonitor stateMonitor;

	/**
	 * 待监控的对象
	 */
	private final Object target;

	/**
	 * 构造函数。
	 * 
	 * @param target
	 *            待监控的对象，该对象中的所有执行器对象会被监控器通过反射获取。<font
	 *            color=red>特别注意：该对象中的所有执行器对象必须被定义为成员变量，
	 *            且在调用该方法前必须完成初始化，否则监控器将无法通过反射获取</font>
	 * @param stateMonitor
	 *            监控实施器，监控器通过该实施器来完成保存执行状态、获取新的线程池上限等操作
	 * 
	 * @throws IllegalArgumentException
	 *             target为null
	 * @throws IllegalArgumentException
	 *             stateMonitor为null
	 */
	public ExecutingMonitor(Object target, ExecutingStateMonitor stateMonitor) {
		if (null == target || null == stateMonitor) {
			throw new IllegalArgumentException();
		}
		this.target = target;
		this.stateMonitor = stateMonitor;
	}

	/**
	 * 构造函数。
	 * 
	 * @param target
	 *            待监控的对象，该对象中的所有执行器对象会被监控器通过反射获取。<font
	 *            color=red>特别注意：该对象中的所有执行器对象必须被定义为成员变量，
	 *            且在调用该方法前必须完成初始化，否则监控器将无法通过反射获取</font>
	 * @param stateMonitor
	 *            监控实施器，监控器通过该实施器来完成保存执行状态、获取新的线程池上限等操作
	 * @param defaultDataSource
	 *            实施监控时所使用的数据源，可为null，如果null，监控器将不使用数据源
	 * @throws IllegalArgumentException
	 *             target为null
	 * @throws IllegalArgumentException
	 *             stateMonitor为null
	 */
	public ExecutingMonitor(Object target, ExecutingStateMonitor stateMonitor,
			DataSource defaultDataSource) {
		this(target, stateMonitor);
		this.dataSource = defaultDataSource;
	}

	/**
	 * 获取实施监控时所需使用的数据源。
	 * 
	 * @return 实施监控时所需使用的数据源
	 */
	public DataSource getDataSource() {
		return dataSource;
	}

	/**
	 * 获取监控实施间隔，单位：秒。
	 * 
	 * @return 监控实施间隔，单位：秒
	 */
	public long getMonitoringingInterval() {
		return monitoringInterval;
	}

	/**
	 * 运行监控器。当所有执行器都已终止后，该方法才会返回。
	 * 
	 */
	@Override
	public void run() {
		if (null != dataSource) {
			DynamicDataSourceHolder.change(dataSource);
		} else {
			DynamicDataSourceHolder.changeToDefault();
		}

		try {
			findExecutors();
		} catch (Exception e) {
			return;
		}

		while (!isAllExecutorTerminated()) {
			try {
				adjustThreadPoolSize();
				saveState();
				TimeUnit.SECONDS.sleep(monitoringInterval);
			} catch (InterruptedException e) {
				break;
			} catch (Exception e) {
				continue;
			}
		}
	}

	/**
	 * 设置实施监控时所需使用的数据源。
	 * 
	 * @param dataSource
	 *            实施监控时所需使用的数据源
	 */
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * 设置监控实施间隔，单位：秒。
	 * 
	 * @param monitoringingInterval
	 *            监控实施间隔，单位：秒
	 */
	public void setMonitoringingInterval(long monitoringingInterval) {
		this.monitoringInterval = monitoringingInterval;
	}

	/**
	 * 通过监控实施器获取待调整的执行器线程池上限，调整执行器。
	 */
	private void adjustThreadPoolSize() {
		if (null == stateMonitor || null == executors || 0 == executors.size()) {
			return;
		}
		// 遍历所有待监控的执行器，逐一获取其待调整的线程池上限，调整执行器
		for (Map.Entry<String, Executor<? extends Task>> executorEntry : executors
				.entrySet()) {
			String name = new String(executorEntry.getKey());
			Executor<? extends Task> executor = executorEntry.getValue();

			// 调整生产者线程池
			int newProducerThreadMaxSize;
			try {
				newProducerThreadMaxSize = stateMonitor
						.newProducerThreadMaxSize(name);
			} catch (Exception e) {
				newProducerThreadMaxSize = 0;
			}
			if (0 < newProducerThreadMaxSize
					&& newProducerThreadMaxSize != executor
							.getProducerThreadMaxSize()) {
				executor.setProducerThreadMaxSize(newProducerThreadMaxSize);
			}

			// 调整消费者线程池
			int newConsumerThreadMaxSize;
			try {
				newConsumerThreadMaxSize = stateMonitor
						.newConsumerThreadMaxSize(name);
			} catch (Exception e) {
				newConsumerThreadMaxSize = 0;
			}
			if (0 < newConsumerThreadMaxSize
					&& newConsumerThreadMaxSize != executor
							.getConsumerThreadMaxSize()) {
				executor.setConsumerThreadMaxSize(newConsumerThreadMaxSize);
			}
		}
	}

	/**
	 * 通过反射查找监控目标中的待监控执行器。
	 * 
	 * @throws IllegalAccessException
	 *             反射查找过程中抛出的异常
	 */
	private void findExecutors() throws IllegalAccessException {
		if (null == target) {
			return;
		}

		Field[] fields = target.getClass().getDeclaredFields();
		if (null == fields || 0 == fields.length) {
			return;
		}

		for (Field field : fields) {
			field.setAccessible(true);
			Object obj = field.get(target);
			if (Executor.class.isInstance(obj) && !isExecutorExists(obj)) {
				String name = field.getName();
				executors.put(name, (Executor<?>) obj);
			}
		}
	}

	/**
	 * 判断所有待监控的执行器是否已终止。
	 * 
	 * @return 是返回true，否则false
	 */
	private boolean isAllExecutorTerminated() {
		if (null == executors || 0 == executors.size()) {
			return true;
		}
		for (Map.Entry<String, Executor<? extends Task>> executorEntry : executors
				.entrySet()) {
			if (!executorEntry.getValue().isTerminated()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 判断指定的执行器是否已通过反射找到过。
	 * 
	 * @param executor
	 *            指定的执行器
	 * @return 是返回true，否则false
	 */
	private boolean isExecutorExists(Object executor) {
		for (Map.Entry<String, Executor<? extends Task>> executorEntry : executors
				.entrySet()) {
			if (executorEntry.getValue().hashCode() == executor.hashCode()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 保存当前的执行器状态。
	 */
	private void saveState() {
		if (null == stateMonitor || null == executors || 0 == executors.size()) {
			return;
		}
		// 遍历所有待监控的执行器，逐一获取状态，并调用监控实施器的saveState方法
		for (Map.Entry<String, Executor<? extends Task>> executorEntry : executors
				.entrySet()) {
			String name = new String(executorEntry.getKey());
			Executor<? extends Task> executor = executorEntry.getValue();
			float fControllingInterval = Float.parseFloat(Long
					.toString(monitoringInterval));

			// 计算线程相关的状态
			long producerThreadCompletedCount = executor
					.getProducerThreadCompletedCount();
			long consumerThreadCompletedCount = executor
					.getConsumerThreadCompletedCount();
			long lastProducerThreadCompletedCount = null == lastProducerThreadCompletedCounts
					.get(name) ? 0 : lastProducerThreadCompletedCounts
					.get(name);
			long lastConsumerThreadCompletedCount = null == lastConsumerThreadCompletedCounts
					.get(name) ? 0 : lastConsumerThreadCompletedCounts
					.get(name);
			float producerThreadRunningRate = (producerThreadCompletedCount - lastProducerThreadCompletedCount)
					/ fControllingInterval;
			float consumerThreadRunningRate = (consumerThreadCompletedCount - lastConsumerThreadCompletedCount)
					/ fControllingInterval;

			// 计算任务相关的状态
			long queuedCount = executor.getQueuedTaskCount();
			long consumedCount = executor.getConsumedTaskCount();
			long lastQueuedCount = null == lastQueuedCounts.get(name) ? 0
					: lastQueuedCounts.get(name);
			long lastConsumedCount = null == lastConsumedCounts.get(name) ? 0
					: lastConsumedCounts.get(name);
			float queueingRate = (queuedCount - lastQueuedCount)
					/ fControllingInterval;
			float consumingRate = (consumedCount - lastConsumedCount)
					/ fControllingInterval;

			// 组装状态对象
			ExecutingState state = new ExecutingState();
			state.setExecutorName(name);
			state.setQueueSize(executor.getTaskQueueSize());
			state.setQueueCapacity(executor.getTaskQueueCapacity());
			state.setQueuedCount(queuedCount);
			state.setQueuedNum(queuedCount - lastQueuedCount);
			state.setQueueingRate(queueingRate);
			state.setConsumedCount(consumedCount);
			state.setConsumedNum(consumedCount - lastConsumedCount);
			state.setConsumingRate(consumingRate);
			state.setProducerThreadActiveCount(executor
					.getProducerThreadActiveCount());
			state.setProducerThreadMaxSize(executor.getProducerThreadMaxSize());
			state.setProducerThreadCompletedCount(producerThreadCompletedCount);
			state.setProducerThreadCompletedNum(producerThreadCompletedCount
					- lastProducerThreadCompletedCount);
			state.setProducerThreadRunningRate(producerThreadRunningRate);
			state.setConsumerThreadActiveCount(executor
					.getConsumerThreadActiveCount());
			state.setConsumerThreadMaxSize(executor.getConsumerThreadMaxSize());
			state.setConsumerThreadCompletedCount(consumerThreadCompletedCount);
			state.setConsumerThreadCompletedNum(consumerThreadCompletedCount
					- lastConsumerThreadCompletedCount);
			state.setConsumerThreadRunningRate(consumerThreadRunningRate);

			lastProducerThreadCompletedCounts.put(name,
					producerThreadCompletedCount);
			lastConsumerThreadCompletedCounts.put(name,
					consumerThreadCompletedCount);
			lastQueuedCounts.put(name, queuedCount);
			lastConsumedCounts.put(name, consumedCount);

			// 调用监控实施器，保存状态
			try {
				stateMonitor.saveState(state);
			} catch (Exception e) {
				continue;
			}
		}
	}
}
