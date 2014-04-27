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
package org.goduun.executor.datasource;

/**
 * 线程安全的数据源切换器。
 * 
 * @author Hu Ruomin
 */
public final class DynamicDataSourceHolder {

	/**
	 * 数据源操作代理
	 */
	private static DynamicDataSourceProxy proxy;

	/**
	 * 不允许实例化
	 */
	private DynamicDataSourceHolder() {
	}

	/**
	 * 获取默认数据源。
	 * 
	 * @return 默认数据源
	 */
	public static synchronized DataSource getDefault() {
		return null == proxy ? null : proxy.getDefault();
	}

	/**
	 * 切换当前线程的数据源。
	 * 
	 * @param dataSource
	 *            数据源的key值
	 */
	public static void change(DataSource dataSource) {
		if (null != proxy) {
			proxy.change(dataSource);
		}
	}

	/**
	 * 切换当前线程的数据源至默认数据源。
	 */
	public static void changeToDefault() {
		if (null != proxy) {
			proxy.change(proxy.getDefault());
		}
	}

	/**
	 * 获取当前线程正在使用的数据源。
	 * 
	 * @return 当前正在使用的数据源
	 */
	public static DataSource getCurrent() {
		return null == proxy ? null : proxy.getCurrent();
	}

	/**
	 * 设置动态数据源代理。
	 * 
	 * @param proxy
	 *            动态数据源代理
	 * @throws IllegalStateException
	 *             如果已设置过代理
	 * @throws IllegalArgumentException
	 *             如果proxy为null
	 */
	public static void setProxy(DynamicDataSourceProxy proxy) {
		if (null != DynamicDataSourceHolder.proxy) {
			throw new IllegalStateException();
		}
		if (null == proxy) {
			throw new IllegalArgumentException();
		}
		DynamicDataSourceHolder.proxy = proxy;
	}
}