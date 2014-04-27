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
 * 执行器日志的格式化工具。
 * <p>
 * 负责对执行器输出的日志进行统一的格式化。
 * 
 * @author Hu Ruomin
 */
public final class LogFormator {

	/**
	 * 当task为null时的日志信息
	 */
	private static final String NULL_TASK_MSG = "log string building error because task is a null value";

	/**
	 * 当producerClazz为null时的日志信息
	 */
	private static final String NULL_PRODUCER_CLASS_MSG = "log string building error because producerClass is a null value";

	/**
	 * 当consumerClazz为null时的日志信息
	 */
	private static final String NULL_CONSUMER_CLASS_MSG = "log string building error because consumerClass is a null value";

	/**
	 * 日志信息中，任务标识字段的标题
	 */
	public static final String ID_TITLE = ", id=";

	/**
	 * 日志信息中，任务信息字段的标题
	 */
	public static final String INFO_TITLE = ", info=";

	/**
	 * 日志信息中，任务执行耗时字段的标题
	 */
	public static final String COST_TITLE = ", ms=";

	/**
	 * 日志信息中，任务执行描述字段的标题
	 */
	public static final String MSG_TITLE = ", msg=";

	/**
	 * 日志信息中，父任务类型字段的标题
	 */
	public static final String PARENT_TITLE = ", p=";

	/**
	 * 日志信息中，父任务标识字段的标题
	 */
	public static final String PARENT_ID_TITLE = ", pid=";

	/**
	 * 日志信息中，唤醒触发者的任务类型字段的标题
	 */
	public static final String AWAKED_BY_TITLE = ", awkby=";

	/**
	 * 日志信息中，唤醒触发者的任务标识字段的标题
	 */
	public static final String AWAKED_BY_ID_TITLE = ", awkid=";

	/**
	 * 日志信息中，唤醒结果描述字段的标题
	 */
	public static final String AWAKING_MSG_TITLE = ", awkmsg=";

	/**
	 * 格式化任务唤醒日志信息。
	 * @param ancestor
	 *            被唤醒的任务
	 * @param task
	 *            触发唤醒的任务
	 * @param awakingCost
	 *            唤醒过程耗时
	 * 
	 * @return 任务唤醒日志信息
	 */
	public static String formatAwakingInfo(Task ancestor, Task task, long awakingCost) {
		if (null == task || null == ancestor) {
			return NULL_TASK_MSG;
		}

		String msg = ancestor.getClass().getSimpleName() + " - ";
		if (ancestor.isFailedToBeAwaked()) {
			msg += "awaking failed";
		} else {
			msg += "awaked";
		}

		if (null != ancestor.getId()) {
			msg += ID_TITLE + ancestor.getId();
		}
		if (null != ancestor.getId()) {
			msg += AWAKED_BY_TITLE + task.getClass().getSimpleName()
					+ AWAKED_BY_ID_TITLE + task.getId();
		}
		msg += COST_TITLE + awakingCost;
		if (null != ancestor.getMessageOfAwaking()) {
			msg += AWAKING_MSG_TITLE + ancestor.getMessageOfAwaking();
		}

		return msg;
	}

	/**
	 * 格式化任务结束日志信息。
	 * 
	 * @param task
	 *            任务
	 * @return 任务结束日志信息
	 */
	public static String formatCompletionInfo(Task task) {
		if (null == task) {
			return NULL_TASK_MSG;
		}

		String msg = task.getClass().getSimpleName() + " - ";
		if (task.isFailed()) {
			msg += "failed";
		} else {
			msg += "completed";
		}

		if (null != task.getId()) {
			msg += ID_TITLE + task.getId();
		}
		msg += COST_TITLE + task.getExecutingMillis();
		if (null != task.getMessage()) {
			msg += MSG_TITLE + task.getMessage();
		}

		return msg;
	}

	/**
	 * 格式化消费者执行成功日志信息。
	 * 
	 * @param consumerClass
	 *            消费者类型
	 * @param cost
	 *            执行耗时
	 * @return 消费者执行成功日志信息
	 */
	public static String formatConsumingFailedInfo(Class<?> consumerClass, long cost) {
		return formatConsumingInfo(consumerClass, cost, true);
	}

	/**
	 * 格式化消费者执行失败日志信息。
	 * 
	 * @param consumerClass
	 *            消费者类型
	 * @param cost
	 *            执行耗时
	 * @return 消费者执行失败日志信息
	 */
	public static String formatConsumingSuccessedInfo(Class<?> consumerClass,
			long cost) {
		return formatConsumingInfo(consumerClass, cost, false);
	}

	/**
	 * 格式化任务生成日志信息。
	 * 
	 * @param task
	 *            任务
	 * @return 任务生成日志信息
	 */
	public static String formatGenerationInfo(Task task) {
		if (null == task) {
			return NULL_TASK_MSG;
		}

		String msg = task.getClass().getSimpleName() + " - generated";
		if (null != task.getId()) {
			msg += ID_TITLE + task.getId();
		}
		if (null != task.getInfo()) {
			msg += INFO_TITLE + task.getInfo();
		}
		if (null != task.getParent()) {
			msg += PARENT_TITLE + task.getParent().getClass().getSimpleName()
					+ PARENT_ID_TITLE + task.getParent().getId();
		}
		return msg;
	}

	/**
	 * 格式化生产者执行成功日志信息。
	 * 
	 * @param producerClass
	 *            生产者类型
	 * @param cost
	 *            执行耗时
	 * @return 生产者执行成功日志信息
	 */
	public static String formatProducingFailedInfo(Class<?> producerClass, long cost) {
		return formatProducingInfo(producerClass, cost, true);
	}

	/**
	 * 格式化生产者执行失败日志信息。
	 * 
	 * @param producerClass
	 *            生产者类型
	 * @param cost
	 *            执行耗时
	 * @return 生产者执行失败日志信息
	 */
	public static String formatProducingSuccessedInfo(Class<?> producerClass,
			long cost) {
		return formatProducingInfo(producerClass, cost, false);
	}

	/**
	 * 格式化消费者执行日志信息。
	 * 
	 * @param consumerClass
	 *            消费者类型
	 * @param cost
	 *            执行耗时
	 * @param isFailed
	 *            是否执行失败
	 * @return 消费者执行日志信息
	 */
	private static String formatConsumingInfo(Class<?> consumerClass, long cost,
			boolean isFailed) {
		if (null == consumerClass) {
			return NULL_CONSUMER_CLASS_MSG;
		}
		String msg;
		if (isFailed) {
			msg = consumerClass.getSimpleName() + " failed";
		} else {
			msg = consumerClass.getSimpleName() + " done";
		}
		msg += COST_TITLE + cost;
		return msg;
	}

	/**
	 * 格式化生产者执行日志。
	 * 
	 * @param producerClass
	 *            生产者类型
	 * @param cost
	 *            执行耗时
	 * @isFailed 是否执行失败
	 * @return 生产者执行日志
	 */
	private static String formatProducingInfo(Class<?> producerClass, long cost,
			boolean isFailed) {
		if (null == producerClass) {
			return NULL_PRODUCER_CLASS_MSG;
		}
		String msg;
		if (isFailed) {
			msg = producerClass.getSimpleName() + " failed";
		} else {
			msg = producerClass.getSimpleName() + " done";
		}
		msg += COST_TITLE + cost;
		return msg;
	}

	/**
	 * 不允许实例化。
	 */
	private LogFormator() {

	}
}
