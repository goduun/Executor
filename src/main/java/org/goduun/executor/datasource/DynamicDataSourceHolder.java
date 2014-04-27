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