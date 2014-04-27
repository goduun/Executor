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

import org.goduun.executor.datasource.DataSource;
import org.goduun.executor.datasource.DynamicDataSourceHolder;

/**
 * 抽象任务，实现了绝大部分任务接口中的方法，并提供了一系列工具方法。
 * <p>
 * 提供的默认值：
 * <ol>
 * <li>默认任务执行结果为成功。</li>
 * <li>默认任务未被唤醒过。</li>
 * <li>默认任务唤醒结果为成功。</li>
 * <li>默认数据源为所属进程的默认数据源。</li>
 * </ol>
 * 
 * @author Hu Ruomin
 */
public abstract class AbstractTask implements Task {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -8151210897952227336L;

	/**
	 * 处理当前任务所使用的默认数据源
	 */
	private DataSource defaultDataSource = DynamicDataSourceHolder.getDefault();

	/**
	 * 任务失败原因
	 */
	private Throwable failedCause = null;

	/**
	 * 任务唤醒失败原因
	 */
	private Throwable failedCauseOfAwaking = null;

	/**
	 * 任务是否失败
	 */
	private boolean isFailed = false;

	/**
	 * 任务唤醒是否失败
	 */
	private boolean isFailedToBeAwaked = false;

	/**
	 * 任务执行结果描述
	 */
	private String message = null;

	/**
	 * 任务唤醒结果描述
	 */
	private String messageOfAwaking = null;

	/**
	 * 父任务
	 */
	private final Task parent;

	/**
	 * 任务开始时间
	 */
	private long startedAt;

	/**
	 * 任务完成时间
	 */
	private long stoppedAt;

	/**
	 * 任务状态，用于唤醒同步控制
	 */
	private TaskState taskState;

	/**
	 * 构造函数。
	 */
	public AbstractTask() {
		this(null);
	}

	/**
	 * 构造函数。
	 * 
	 * @param parent
	 *            父任务
	 */
	public AbstractTask(Task parent) {
		this.parent = parent;
	}

	/**
	 * 设置当前任务执行失败。
	 */
	public void fail() {
		this.isFailed = true;
	}

	/**
	 * 设置当前任务执行失败。
	 * 
	 * @param message
	 *            执行情况描述
	 */
	public void fail(String message) {
		this.message = message;
		this.isFailed = true;
	}

	/**
	 * 设置当前任务执行失败。
	 * 
	 * @param message
	 *            执行情况描述
	 * @param failedCause
	 *            执行失败原因
	 */
	public void fail(String message, Throwable failedCause) {
		this.message = message;
		this.failedCause = failedCause;
		this.isFailed = true;
	}

	/**
	 * 设置当前任务执行失败。
	 * 
	 * @param failedCause
	 *            执行失败原因
	 */
	public void fail(Throwable failedCause) {
		this.failedCause = failedCause;
		this.isFailed = true;
	}

	/**
	 * 设置任务被唤醒为失败。
	 */
	public void failToBeAwaked() {
		isFailedToBeAwaked = true;
	}

	/**
	 * 设置任务被唤醒为失败。
	 * 
	 * @param message
	 *            被唤醒情况描述
	 */
	public void failToBeAwaked(String message) {
		messageOfAwaking = message;
		isFailedToBeAwaked = true;
	}

	/**
	 * 设置任务被唤醒为失败。
	 * 
	 * @param message
	 *            被唤醒情况描述
	 * @param failedCause
	 *            被唤醒失败原因
	 */
	public void failToBeAwaked(String message, Throwable failedCause) {
		messageOfAwaking = message;
		failedCauseOfAwaking = failedCause;
		isFailedToBeAwaked = true;
	}

	/**
	 * 设置任务被唤醒为失败。
	 * 
	 * @param failedCause
	 *            被唤醒失败原因
	 */
	public void failToBeAwaked(Throwable failedCause) {
		failedCauseOfAwaking = failedCause;
		isFailedToBeAwaked = true;
	}

	/**
	 * 获取指定类型的祖先任务。
	 * 
	 * @param ancestorClass
	 *            指定的祖先任务类型
	 * @return 指定类型的祖先任务对象，如为null表示未找到，可能的原因是：1.未正确设置parent关系；2.祖先类型指定错误
	 * 
	 */
	public <T extends Task> T findAncestor(Class<T> ancestorClass) {
		if (null == ancestorClass) {
			return null;
		}
		Task parent = this.getParent();
		while (parent != null) {
			if (parent.getClass().isAssignableFrom(ancestorClass)) {
				return ancestorClass.cast(parent);
			}
			parent = parent.getParent();
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 * 
	 */
	@Override
	public DataSource getDefaultDataSource() {
		return defaultDataSource;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getExecutingMillis() {
		return stoppedAt - startedAt;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Throwable getFailedCause() {
		return failedCause;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Throwable getFailedCauseOfAwaking() {
		return failedCauseOfAwaking;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public abstract String getId();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getInfo() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getMessage() {
		return message;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getMessageOfAwaking() {
		return messageOfAwaking;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Task getParent() {
		return parent;
	}

	/**
	 * 获取当前任务的开始执行时间。
	 */
	public long getStartedAt() {
		return startedAt;
	}

	/**
	 * 获取当前任务的执行结束时间。
	 * 
	 * @return 当前任务的执行结束时间
	 */
	public long getStoppedAt() {
		return stoppedAt;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TaskState getTaskState() {
		return taskState;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isFailed() {
		return isFailed;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isFailedToBeAwaked() {
		return isFailedToBeAwaked;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @throws IllegalArgumentException
	 *             数据源为空
	 */
	@Override
	public void setDefaultDataSource(DataSource dataSource) {
		if (null == dataSource) {
			throw new IllegalArgumentException();
		}
		this.defaultDataSource = dataSource;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * {@inheritDoc}
	 * 
	 */
	@Override
	public void setMessageOfAwaking(String message) {
		messageOfAwaking = message;
	}

	/**
	 * 设置当前任务的开始执行时间。
	 * 
	 * @param start
	 *            当前任务的开始执行时间
	 */
	public void setStartedAt(long start) {
		this.startedAt = start;
	}

	/**
	 * 设置当前任务的执行结束时间。
	 * 
	 * @param complete
	 *            当前任务的执行结束时间
	 */
	public void setStoppedAt(long complete) {
		this.stoppedAt = complete;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @throws IllegalArgumentException
	 *             如果taskState为null
	 */
	@Override
	public void setTaskState(TaskState taskState) {
		if (null == taskState) {
			throw new IllegalArgumentException();
		}
		this.taskState = taskState;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void startExecuting() {
		this.startedAt = System.currentTimeMillis();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void stopExecuting() {
		this.stoppedAt = System.currentTimeMillis();
	}
}
