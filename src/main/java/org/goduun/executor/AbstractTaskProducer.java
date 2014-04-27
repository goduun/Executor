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
