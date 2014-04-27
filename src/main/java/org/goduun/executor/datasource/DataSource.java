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
