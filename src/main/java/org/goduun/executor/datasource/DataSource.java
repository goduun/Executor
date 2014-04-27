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
 * 数据源。
 * <p>
 * 通常且比较方便的实现是一个集合，如：
 * 
 * <pre>
 * public enum DemoDataSources implements DataSourceEnum {
 * 
 * 	DATA_SOURCE1(&quot;key1&quot;), DATA_SOURCE2(&quot;key2&quot;);
 * 
 * 	private final String dataSourceKey;
 * 
 * 	private DemoDataSources(String dataSourceKey) {
 * 		this.dataSourceKey = dataSourceKey;
 * 	}
 * 
 * 	public String getDataSourceKey() {
 * 		return this.dataSourceKey;
 * 	}
 * }
 * </pre>
 * 
 * 
 * @author Hu Ruomin
 */
public interface DataSource {

	/**
	 * 获取数据源的key值。
	 * 
	 * @return 数据源的key值
	 */
	String getDataSourceKey();
}
