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

import java.io.Serializable;

/**
 * 执行器生产者在生产过程中出现的异常或错误。
 * 
 * @author Hu Ruomin
 */
public final class ProducingError implements Serializable {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -4844682458301363334L;

	/**
	 * 错误消息
	 */
	private String message;

	/**
	 * 错误原因
	 */
	private Throwable cause;

	/**
	 * 产生错误的生产者类型
	 */
	private final Class<?> producerClass;

	/**
	 * 构造函数。
	 * @param producerClass
	 *            产生错误的生产者类型
	 * @param message
	 *            错误消息
	 * 
	 * @throws IllegalArgumentException
	 *             producerClass为null
	 */
	public ProducingError(Class<?> producerClass, String message) {
		this(producerClass);
		this.message = message;
	}

	/**
	 * 构造函数。
	 * @param producerClass
	 *            产生错误的生产者类型
	 * @param message
	 *            错误消息
	 * @param cause
	 *            错误原因
	 * 
	 * @throws IllegalArgumentException
	 *             producerClass为null
	 */
	public ProducingError(Class<?> producerClass, String message,
			Throwable cause) {
		this(producerClass);
		this.message = message;
		this.cause = cause;
	}

	/**
	 * 构造函数。
	 * @param producerClass
	 *            产生错误的生产者类型
	 * @param cause
	 *            错误原因
	 * 
	 * @throws IllegalArgumentException
	 *             producerClass为null
	 */
	public ProducingError(Class<?> producerClass, Throwable cause) {
		this(producerClass);
		this.cause = cause;
	}

	/**
	 * 
	 * 构造函数。
	 * 
	 * @param producerClass
	 *            产生错误的生产者类型
	 * @throws IllegalArgumentException
	 *             producerClass为null
	 */
	private ProducingError(Class<?> producerClass) {
		if (null == producerClass) {
			throw new IllegalArgumentException();
		}
		this.producerClass = producerClass;
	}

	/**
	 * 获取错误原因。
	 * 
	 * @return 错误原因
	 */
	public Throwable getCause() {
		return cause;
	}

	/**
	 * 获取错误消息。
	 * 
	 * @return 错误消息
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * 获取产生错误的生产者类型。
	 * 
	 * @return 产生错误的生产者类型
	 */
	public Class<?> getProducerClass() {
		return producerClass;
	}

	/**
	 * 设置错误原因。
	 * 
	 * @param cause
	 *            错误原因
	 */
	public void setCause(Throwable cause) {
		this.cause = cause;
	}

	/**
	 * 设置错误消息。
	 * 
	 * @param message
	 *            错误消息
	 */
	public void setMessage(String message) {
		this.message = message;
	}

}
