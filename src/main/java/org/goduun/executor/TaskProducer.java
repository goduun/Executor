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

import org.goduun.executor.datasource.DataSource;

/**
 * 执行器的任务生产者。
 * <p>
 * 生产者负责创建任务，并将任务放入任务队列。一个执行器会为每个生产者对象分配一个线程来执行，待执行完成后，线程资源将被回收。只要生产者不停止生产，
 * 执行器就不会停止，因此，生产者决定整个执行器能否终止。
 * 
 * @author Hu Ruomin
 * @param <P>
 *            可生产出的任务类型
 */
public interface TaskProducer<P extends Task> {

	/**
	 * 获取执行任务生产时所默认使用的数据源。
	 * <p>
	 * 如果方法返回一个有效的数据源，那么在执行任务生产前，执行器会自动将当前线程的数据源切换至该数据源。
	 * 如果方法返回null或一个未定义的数据源或发生异常，执行器将使用系统默认数据源。
	 * 
	 * @return 执行任务生产时所默认使用的数据源
	 */
	DataSource getDefaultDataSource();

	/**
	 * 执行任务生产。
	 * <p>
	 * 在执行过程中，如果有错误信息需要执行器作为日志进行输出，可使用参数中的errorPipe对象，详见：
	 * {@link ProducingErrorPipe}。
	 * <p>
	 * 如果该方法有异常抛出，执行器会终止该生产者，回收线程资源并不再调度该生产者。
	 * 
	 * @param pipe
	 *            任务传送管道，生产者通过该管道向执行器任务队列输送任务
	 * @param errorPipe
	 *            错误输出管道，输出的错误将被执行器作为日志输出
	 * 
	 */
	void execute(TaskPipe<P> pipe, ProducingErrorPipe errorPipe);
}
