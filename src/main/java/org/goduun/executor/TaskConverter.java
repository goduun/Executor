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

/**
 * 执行器的任务转换者。<p>
 * 执行器的任务转换者是一个处理者，它必须处理任务。但与一般处理者不同的是，它额外获取了一条向另一个执行器传送任务的管道，在转换者处理任务的过程中，
 * 能够生产并通过管道向另一个执行器传送任务．
 * 因此它又像一个任务生产者，不过，它不是正规意义上的生产者，因为虽然获取了管道，但它本身的启动不由任务传送的目标执行器控制
 * ，而是由它作为处理者所属的执行器负责控制。
 * 
 * @author Hu Ruomin
 * @param <C>
 *            转换者作为任务处理者可处理的任务类型
 * @param <P>
 *            转换者作为任务生产者可产出的任务类型、
 * @see TaskProcessor
 */
public interface TaskConverter<C extends Task, P extends Task> extends
		TaskProcessor<C> {

	/**
	 * 为任务转换者设置任务传送管道，任务转换者所生产出的任务，可通过该管道输送到下一个执行器的任务队列中。
	 * 
	 * @param pipe
	 *            任务传送管道
	 */
	void setPipe(TaskPipe<P> pipe);
}
