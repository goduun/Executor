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
import java.util.Date;

/**
 * 执行器运行状态信息。
 * 
 * @author Hu Ruomin
 */
public class ExecutingState implements Serializable {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 7587523451433620216L;

	/**
	 * 从执行器启动到本次状态记录时间为止，消费完的任务总数
	 */
	private long consumedCount;

	/**
	 * 从上一次状态记录到本次记录时间为止，消费完的任务数
	 */
	private long consumedNum;

	/**
	 * 当前正在执行生产的消费者线程数
	 */
	private int consumerThreadActiveCount;

	/**
	 * 从执行器启动到本次状态记录时间为止，已执行完成的消费者线程总数
	 */
	private long consumerThreadCompletedCount;

	/**
	 * 从上一次状态记录到本次记录时间为止，已执行完成的消费者线程数
	 */
	private long consumerThreadCompletedNum;

	/**
	 * 当前消费者线程池的最大线程上限
	 */
	private int consumerThreadMaxSize;

	/**
	 * 从上一次状态记录到本次记录时间为止，消费者线程的执行速度，单位：任务个数/秒
	 */
	private float consumerThreadRunningRate;

	/**
	 * 从上一次状态记录到本次记录时间为止，消费的速度，单位：任务个数/秒
	 */
	private float consumingRate;

	/**
	 * 执行器名称
	 */
	private String executorName;

	/**
	 * 当前正在执行生产的生产者线程数
	 */
	private int producerThreadActiveCount;

	/**
	 * 从执行器启动到本次状态记录时间为止，已执行完成的生产者线程总数
	 */
	private long producerThreadCompletedCount;

	/**
	 * 从上一次状态记录到本次记录时间为止，已执行完成的生产者线程数
	 */
	private long producerThreadCompletedNum;

	/**
	 * 当前生产者线程池的最大线程上限
	 */
	private int producerThreadMaxSize;

	/**
	 * 从上一次状态记录到本次记录时间为止，生产者线程的执行速度，单位：任务个数/秒
	 */
	private float producerThreadRunningRate;

	/**
	 * 当前任务队列能存储的任务数上限
	 */
	private int queueCapacity;

	/**
	 * 从执行器启动到本次状态记录时间为止，进入任务队列的任务总数
	 */
	private long queuedCount;

	/**
	 * 从上一次状态记录到本次记录时间为止，进入任务队列的任务数
	 */
	private long queuedNum;

	/**
	 * 从上一次状态记录到本次记录时间为止，任务进入队列的速度，单位：任务个数/秒
	 */
	private float queueingRate;

	/**
	 * 当前任务队列中的任务总数
	 */
	private int queueSize;

	/**
	 * 状态记录时间
	 */
	private Date stateDate = new Date();

	/**
	 * 获取从执行器启动到本次状态记录时间为止，消费完的任务总数。
	 * 
	 * @return 从执行器启动到本次状态记录时间为止，消费完的任务总数
	 */
	public long getConsumedCount() {
		return consumedCount;
	}

	/**
	 * 获取从上一次状态记录到本次记录时间为止，消费完的任务数。
	 * 
	 * @return 从上一次状态记录到本次记录时间为止，消费完的任务数
	 */
	public long getConsumedNum() {
		return consumedNum;
	}

	/**
	 * 获取当前正在执行生产的消费者线程数。
	 * 
	 * @return 当前正在执行生产的消费者线程数
	 */
	public int getConsumerThreadActiveCount() {
		return consumerThreadActiveCount;
	}

	/**
	 * 获取从执行器启动到本次状态记录时间为止，已执行完成的消费者线程总数。
	 * 
	 * @return 从执行器启动到本次状态记录时间为止，已执行完成的消费者线程总数
	 */
	public long getConsumerThreadCompletedCount() {
		return consumerThreadCompletedCount;
	}

	/**
	 * 获取从上一次状态记录到本次记录时间为止，已执行完成的消费者线程数。
	 * 
	 * @return 从上一次状态记录到本次记录时间为止，已执行完成的消费者线程数
	 */
	public long getConsumerThreadCompletedNum() {
		return consumerThreadCompletedNum;
	}

	/**
	 * 获取当前消费者线程池的最大线程上限。
	 * 
	 * @return 当前消费者线程池的最大线程上限
	 */
	public int getConsumerThreadMaxSize() {
		return consumerThreadMaxSize;
	}

	/**
	 * 获取从上一次状态记录到本次记录时间为止，消费者线程的执行速度，单位：任务个数/秒。
	 * 
	 * @return 从上一次状态记录到本次记录时间为止，消费者线程的执行速度，单位：任务个数/秒
	 */
	public float getConsumerThreadRunningRate() {
		return consumerThreadRunningRate;
	}

	/**
	 * 获取从上一次状态记录到本次记录时间为止，消费的速度，单位：任务个数/秒。
	 * 
	 * @return 从上一次状态记录到本次记录时间为止，消费的速度，单位：任务个数/秒
	 */
	public float getConsumingRate() {
		return consumingRate;
	}

	/**
	 * 获取执行器名称。
	 * 
	 * @return 执行器名称
	 */
	public String getExecutorName() {
		return executorName;
	}

	/**
	 * 获取当前正在执行生产的生产者线程数。
	 * 
	 * @return 当前正在执行生产的生产者线程数
	 */
	public int getProducerThreadActiveCount() {
		return producerThreadActiveCount;
	}

	/**
	 * 获取从执行器启动到本次状态记录时间为止，已执行完成的生产者线程总数。
	 * 
	 * @return 从执行器启动到本次状态记录时间为止，已执行完成的生产者线程总数
	 */
	public long getProducerThreadCompletedCount() {
		return producerThreadCompletedCount;
	}

	/**
	 * 获取从上一次状态记录到本次记录时间为止，已执行完成的生产者线程数。
	 * 
	 * @return 从上一次状态记录到本次记录时间为止，已执行完成的生产者线程数
	 */
	public long getProducerThreadCompletedNum() {
		return producerThreadCompletedNum;
	}

	/**
	 * 获取当前生产者线程池的最大线程上限。
	 * 
	 * @return 当前生产者线程池的最大线程上限
	 */
	public int getProducerThreadMaxSize() {
		return producerThreadMaxSize;
	}

	/**
	 * 获取从上一次状态记录到本次记录时间为止，生产者线程的执行速度，单位：任务个数/秒。
	 * 
	 * @return 从上一次状态记录到本次记录时间为止，生产者线程的执行速度，单位：任务个数/秒
	 */
	public float getProducerThreadRunningRate() {
		return producerThreadRunningRate;
	}

	/**
	 * 获取当前任务队列能存储的任务数上限。
	 * 
	 * @return 当前任务队列能存储的任务数上限
	 */
	public int getQueueCapacity() {
		return queueCapacity;
	}

	/**
	 * 获取从执行器启动到本次状态记录时间为止，进入任务队列的任务总数。
	 * 
	 * @return 从执行器启动到本次状态记录时间为止，进入任务队列的任务总数
	 */
	public long getQueuedCount() {
		return queuedCount;
	}

	/**
	 * 获取从上一次状态记录到本次记录时间为止，进入任务队列的任务数。
	 * 
	 * @return 从上一次状态记录到本次记录时间为止，进入任务队列的任务数
	 */
	public long getQueuedNum() {
		return queuedNum;
	}

	/**
	 * 获取从上一次状态记录到本次记录时间为止，任务进入队列的速度，单位：任务个数/秒。
	 * 
	 * @return 从上一次状态记录到本次记录时间为止，任务进入队列的速度，单位：任务个数/秒
	 */
	public float getQueueingRate() {
		return queueingRate;
	}

	/**
	 * 获取当前任务队列中的任务总数。
	 * 
	 * @return 当前任务队列中的任务总数
	 */
	public int getQueueSize() {
		return queueSize;
	}

	/**
	 * 获取状态记录时间，初始化时会将当前时间作为状态记录时间。
	 * 
	 * @return 状态记录时间
	 */
	public Date getStateDate() {
		return stateDate;
	}

	/**
	 * 设置从执行器启动到本次状态记录时间为止，消费完的任务总数。
	 * 
	 * @param consumedCount
	 *            从执行器启动到本次状态记录时间为止，消费完的任务总数
	 */
	public void setConsumedCount(long consumedCount) {
		this.consumedCount = consumedCount;
	}

	/**
	 * 设置从上一次状态记录到本次记录时间为止，消费完的任务数。
	 * 
	 * @param consumedNum
	 *            从上一次状态记录到本次记录时间为止，消费完的任务数
	 */
	public void setConsumedNum(long consumedNum) {
		this.consumedNum = consumedNum;
	}

	/**
	 * 设置当前正在执行生产的消费者线程数。
	 * 
	 * @param consumerThreadActiveCount
	 *            当前正在执行生产的消费者线程数
	 */
	public void setConsumerThreadActiveCount(int consumerThreadActiveCount) {
		this.consumerThreadActiveCount = consumerThreadActiveCount;
	}

	/**
	 * 设置从执行器启动到本次状态记录时间为止，已执行完成的消费者线程总数。
	 * 
	 * @param consumerThreadCompletedCount
	 *            从执行器启动到本次状态记录时间为止，已执行完成的消费者线程总数
	 */
	public void setConsumerThreadCompletedCount(
			long consumerThreadCompletedCount) {
		this.consumerThreadCompletedCount = consumerThreadCompletedCount;
	}

	/**
	 * 设置从上一次状态记录到本次记录时间为止，已执行完成的消费者线程数。
	 * 
	 * @param consumerThreadCompletedNum
	 *            从上一次状态记录到本次记录时间为止，已执行完成的消费者线程数
	 */
	public void setConsumerThreadCompletedNum(long consumerThreadCompletedNum) {
		this.consumerThreadCompletedNum = consumerThreadCompletedNum;
	}

	/**
	 * 设置当前消费者线程池的最大线程上限。
	 * 
	 * @param consumerThreadMaxSize
	 *            当前消费者线程池的最大线程上限
	 */
	public void setConsumerThreadMaxSize(int consumerThreadMaxSize) {
		this.consumerThreadMaxSize = consumerThreadMaxSize;
	}

	/**
	 * 设置从上一次状态记录到本次记录时间为止，消费者线程的执行速度，单位：任务个数/秒。
	 * 
	 * @param consumerThreadRunningRate
	 *            从上一次状态记录到本次记录时间为止，消费者线程的执行速度，单位：任务个数/秒
	 */
	public void setConsumerThreadRunningRate(float consumerThreadRunningRate) {
		this.consumerThreadRunningRate = consumerThreadRunningRate;
	}

	/**
	 * 设置从上一次状态记录到本次记录时间为止，消费的速度，单位：任务个数/秒。
	 * 
	 * @param consumingRate
	 *            从上一次状态记录到本次记录时间为止，消费的速度，单位：任务个数/秒
	 */
	public void setConsumingRate(float consumingRate) {
		this.consumingRate = consumingRate;
	}

	/**
	 * 设置执行器名称。
	 * 
	 * @param executorName
	 *            执行器名称
	 */
	public void setExecutorName(String executorName) {
		this.executorName = executorName;
	}

	/**
	 * 设置当前正在执行生产的生产者线程数。
	 * 
	 * @param producerThreadActiveCount
	 *            当前正在执行生产的生产者线程数
	 */
	public void setProducerThreadActiveCount(int producerThreadActiveCount) {
		this.producerThreadActiveCount = producerThreadActiveCount;
	}

	/**
	 * 设置从执行器启动到本次状态记录时间为止，已执行完成的生产者线程总数。
	 * 
	 * @param producerThreadCompletedCount
	 *            从执行器启动到本次状态记录时间为止，已执行完成的生产者线程总数
	 */
	public void setProducerThreadCompletedCount(
			long producerThreadCompletedCount) {
		this.producerThreadCompletedCount = producerThreadCompletedCount;
	}

	/**
	 * 设置从上一次状态记录到本次记录时间为止，已执行完成的生产者线程数。
	 * 
	 * @param producerThreadCompletedNum
	 *            从上一次状态记录到本次记录时间为止，已执行完成的生产者线程数
	 */
	public void setProducerThreadCompletedNum(long producerThreadCompletedNum) {
		this.producerThreadCompletedNum = producerThreadCompletedNum;
	}

	/**
	 * 设置当前生产者线程池的最大线程上限。
	 * 
	 * @param producerThreadMaxSize
	 *            当前生产者线程池的最大线程上限
	 */
	public void setProducerThreadMaxSize(int producerThreadMaxSize) {
		this.producerThreadMaxSize = producerThreadMaxSize;
	}

	/**
	 * 设置从上一次状态记录到本次记录时间为止，生产者线程的执行速度，单位：任务个数/秒。
	 * 
	 * @param producerThreadRunningRate
	 *            从上一次状态记录到本次记录时间为止，生产者线程的执行速度，单位：任务个数/秒
	 */
	public void setProducerThreadRunningRate(float producerThreadRunningRate) {
		this.producerThreadRunningRate = producerThreadRunningRate;
	}

	/**
	 * 设置当前任务队列能存储的任务数上限。
	 * 
	 * @param queueCapacity
	 *            当前任务队列能存储的任务数上限
	 */
	public void setQueueCapacity(int queueCapacity) {
		this.queueCapacity = queueCapacity;
	}

	/**
	 * 设置从执行器启动到本次状态记录时间为止，进入任务队列的任务总数。
	 * 
	 * @param queuedCount
	 *            从执行器启动到本次状态记录时间为止，进入任务队列的任务总数
	 */
	public void setQueuedCount(long queuedCount) {
		this.queuedCount = queuedCount;
	}

	/**
	 * 设置从上一次状态记录到本次记录时间为止，进入任务队列的任务数。
	 * 
	 * @param queuedNum
	 *            从上一次状态记录到本次记录时间为止，进入任务队列的任务数
	 */
	public void setQueuedNum(long queuedNum) {
		this.queuedNum = queuedNum;
	}

	/**
	 * 设置从上一次状态记录到本次记录时间为止，任务进入队列的速度，单位：任务个数/秒。
	 * 
	 * @param queueingRate
	 *            从上一次状态记录到本次记录时间为止，任务进入队列的速度，单位：任务个数/秒
	 */
	public void setQueueingRate(float queueingRate) {
		this.queueingRate = queueingRate;
	}

	/**
	 * 设置当前任务队列中的任务总数。
	 * 
	 * @param queueSize
	 *            当前任务队列中的任务总数
	 */
	public void setQueueSize(int queueSize) {
		this.queueSize = queueSize;
	}

	/**
	 * 设置状态记录时间，初始化时会将当前时间作为状态记录时间。
	 * 
	 * @param stateDate
	 *            状态记录时间
	 */
	public void setStateDate(Date stateDate) {
		this.stateDate = stateDate;
	}

}
