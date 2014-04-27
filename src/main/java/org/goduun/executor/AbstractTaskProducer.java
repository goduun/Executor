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
 * 抽象生产者，实现了绝大部分生产者接口中的方法，并提供了一系列工具方法。<p>
 * 提供的默认值：
 * <ol>
 * <li>默认数据源为所属进程的默认数据源。</li>
 * </ol>
 * 
 * @author Hu Ruomin
 */
public abstract class AbstractTaskProducer<P extends Task> implements TaskProducer<P> {

	/**
	 * 生产者所使用的默认数据源
	 */
	private DataSource defaultDataSource = DynamicDataSourceHolder.getDefault();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DataSource getDefaultDataSource() {
		return defaultDataSource;
	}

	/**
	 * 设置执行任务生产时所默认使用的数据源。
	 * 
	 * @param defaultDataSource
	 *            执行任务生产时所默认使用的数据源
	 */
	public void setDefaultDataSource(DataSource defaultDataSource) {
		this.defaultDataSource = defaultDataSource;
	}

	/**
	 * {@inheritDoc}
	 * 
	 */
	@Override
	public abstract void execute(TaskPipe<P> pipe, ProducingErrorPipe errorPipe);

}
