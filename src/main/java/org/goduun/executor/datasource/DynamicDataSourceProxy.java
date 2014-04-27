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
 * 数据源代理接口。
 *
 * @author Hu Ruomin
 */
public interface DynamicDataSourceProxy {
	
	/**
	 * 获取默认数据源。
	 * 
	 * @return 默认数据源
	 */
	DataSource getDefault();
	
	/**
	 * 切换当前线程的数据源。
	 * 
	 * @param dataSource
	 *            数据源的key值
	 */
	void change(DataSource dataSource);
	
	/**
	 * 获取当前线程正在使用的数据源。
	 * 
	 * @return 当前正在使用的数据源
	 */
	DataSource getCurrent();
}
