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
