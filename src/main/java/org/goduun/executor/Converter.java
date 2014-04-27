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
 * 转换器。
 * <p>
 * 转换器是执行器的一种，除了能够完成基本的任务生产与消费功能外，还可在消费过程中生产新的任务，并将新任务放入另一个执行器的任务队列。
 * 
 * @author Hu Ruomin
 */
public interface Converter<C extends Task, P extends Task> extends Executor<C> {

	/**
	 * 设置任务传送管道，当前转换器将通过该管道向其他执行器传送任务。
	 * 
	 * @param pipe
	 *            任务输出管道。
	 * @throws IllegalArgumentException
	 *             pipe为null
	 * @throws IllegalStateException
	 *             当前转换器尚未设置消费者对象
	 * @throws IllegalStateException
	 *             当前转换器已被其他执行器连接
	 */
	void convertTaskTo(TaskPipe<P> pipe);
	
	/**
	 * {@inheritDoc}
	 * 
	 * @throws IllegalStateException
	 *             如果当前执行器正在终止或已终止
	 * @throws IllegalStateException
	 *             如果当前执行器没有设置消费者
	 * @throws IllegalStateException
	 *             如果当前转换器没有被其他执行器连接
	 */
	@Override
	void execute();
	
	/**
	 * {@inheritDoc}
	 * 
	 * @throws IllegalStateException
	 *             如果当前执行器不是常驻执行器
	 * @throws IllegalStateException
	 *             如果当前执行器没有设置消费者
	 * @throws InterruptedException
	 *             休眠过程中当前线程被中断
	 * @throws IllegalStateException
	 *             如果当前转换器没有被其他执行器连接
	 */
	@Override
	void executeAndSleep() throws InterruptedException;

	/**
	 * 判断当前转换器是否已被其他执行器连接，接受当前转换器的任务传送。
	 * 
	 * @return 是返回true，否则false
	 */
	boolean isConverted();

	/**
	 * 设置当前转换器的任务转换者。
	 * <p>
	 * 在转换器启动之前，必须完成任务转换者的设置。
	 * 
	 * @param converter
	 *            转换者对象
	 * @throws IllegalStateException
	 *             如果当前转换器已开始执行
	 * @throws IllegalStateException
	 *             如果当前转换器已设置过converter
	 * @throws IllegalArgumentException
	 *             converter为null
	 */
	void setConsumer(TaskConverter<C, P> converter);
}
