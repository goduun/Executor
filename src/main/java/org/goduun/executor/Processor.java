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

/**
 * 处理器。<p>
 * 处理器是执行器的一种，能够完成基本的任务生产与消费功能。
 * 
 * @author Hu Ruomin
 */
public interface Processor<C extends Task> extends Executor<C> {

	/**
	 * 设置当前处理器的任务消费者。<p>
	 * 在处理器启动之前，必须完成任务消费者的设置。
	 * 
	 * @param consumer
	 *            消费者对象
	 * 
	 * @throws IllegalStateException
	 *             如果当前处理器已开始执行
	 * @throws IllegalStateException
	 *             如果当前处理器已设置过消费者
	 * @throws IllegalArgumentException
	 *             consumer为null
	 */
	public void setConsumer(TaskProcessor<C> consumer);
}
