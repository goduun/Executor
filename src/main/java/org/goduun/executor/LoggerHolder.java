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

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 日志对象的管理器。
 * <p>
 * 执行器需要针对不同类型的对象输出日志，日志管理器负责集中管理这些类型对应的日志对象。
 * 
 * @author Hu Ruomin
 */
public final class LoggerHolder {

	/**
	 * 缓存
	 */
	private static final Map<Class<? extends Task>, Logger> LOGGER_HOLDER = new HashMap<Class<? extends Task>, Logger>();

	/**
	 * 不允许实例化
	 */
	private LoggerHolder() {
	}

	/**
	 * 获取指定对象类型的日志对象，永不返回null。
	 * 
	 * @param clazz
	 *            对象类型
	 * @return 指定对象类型的日志对象
	 */
	public static Logger get(Class<? extends Task> clazz) {
		Logger log = LOGGER_HOLDER.get(clazz);
		if (null == log) {
			log = LoggerFactory.getLogger(clazz);
			LOGGER_HOLDER.put(clazz, log);
		}
		return log;
	}
}
