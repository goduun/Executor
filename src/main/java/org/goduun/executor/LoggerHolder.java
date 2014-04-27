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
