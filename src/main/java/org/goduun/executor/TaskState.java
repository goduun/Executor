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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 任务状态，执行器用该状态对任务做唤醒同步控制。
 * <p>
 * 执行器会为每个任务赋予一个任务状态实例，使用者请勿操作该实例，也勿对该实例做synchronized操作。
 * 
 * @author Hu Ruomin
 */
public final class TaskState {

	/**
	 * 父任务关系。 参数说明：Class - 指定的任务类型， Class - 指定任务类型的父任务类型。
	 */
	private static Map<Class<? extends Task>, Class<? extends Task>> parentclassHolder = new HashMap<Class<? extends Task>, Class<? extends Task>>();

	/**
	 * 子任务关系。 参数说明：Class - 指定的任务类型， Class - 指定任务类型的子任务类型。
	 */
	private static Map<Class<? extends Task>, Class<? extends Task>> subclassHolder = new HashMap<Class<? extends Task>, Class<? extends Task>>();

	/**
	 * 获取指定任务类型的父任务类型。
	 * 
	 * @param taskClass
	 *            指定的任务类型
	 * @return 指定任务类型的父任务类型，当taskClass为null时返回null
	 */
	public static Class<? extends Task> getParentClass(
			Class<? extends Task> taskClass) {
		return null == taskClass ? null : parentclassHolder.get(taskClass);
	}

	/**
	 * 获取指定任务类型的子任务类型。
	 * 
	 * @param taskClass
	 *            指定的任务类型
	 * @return 指定任务类型的子任务类型，当taskClass为null时返回null
	 */
	public static Class<? extends Task> getSubClass(
			Class<? extends Task> taskClass) {
		return null == taskClass ? null : subclassHolder.get(taskClass);
	}

	/**
	 * 设置任务类型的父子关系。
	 * 
	 * @param subclass
	 *            子任务类型
	 * @param parentclass
	 *            父任务类型
	 */
	public static void setTaskClassRelation(Class<? extends Task> subclass,
			Class<? extends Task> parentclass) {
		//if (null != subclass && !parentclassHolder.containsKey(subclass)) {
		if (null != subclass) {
			synchronized (parentclassHolder) {
				parentclassHolder.put(subclass, parentclass);
			}
		}
		//if (null != parentclass && !subclassHolder.containsKey(parentclass)) {
		if (null != parentclass) {
			synchronized (subclassHolder) {
				subclassHolder.put(parentclass, subclass);
			}
		}
	}

	/**
	 * 当前任务状态所属的任务对象。
	 */
	private final Task belongTask;

	/**
	 * 当前任务的未过唤醒环节的子任务数。 参数说明：Class - 子任务类型， Long - 未过唤醒环节的该类型的子任务数。
	 */
	private final Map<Class<? extends Task>, Long> countOfUnAwakedSubtask = new HashMap<Class<? extends Task>, Long>();

	/**
	 * 当前任务的未过执行环节的子任务数。 参数说明：Class - 子任务类型， Long - 未过执行环节的该类型的子任务数。
	 */
	private final Map<Class<? extends Task>, Long> countOfUnExecutedSubtask = new HashMap<Class<? extends Task>, Long>();

	/**
	 * 等待唤醒的祖先任务数。 当前任务执行完成时，执行器会做2个判断：
	 * <p>
	 * 1.其祖先任务是否还有未执行完的同类任务
	 * <p>
	 * 2.并且祖先任务是否完成了该类型子任务的生产。
	 * <p>
	 * 如果以上2个条件满足，会触发当前任务唤醒该祖先任务。
	 * <p>
	 * 如果1不满足，表示当前无法触发唤醒该祖先任务。
	 * <p>
	 * 如果1满足但2不满足，表示当前任务需等待该祖先任务完成子任务生产后才可能被唤醒，此时，
	 * countOfWaitingForAwakableAncestor值加1.
	 */
	private int countOfWaitingForAwakableAncestor = 0;

	/**
	 * 当前任务被执行的消费者对象。
	 */
	private TaskProcessor<? extends Task> executedConsumer;

	/**
	 * 当前任务是否已完成了子任务的全部生产。 参数说明：Class - 子任务类型， Boolean - 是否完成了该类型子任务的全部生产。
	 */
	private final Map<Class<? extends Task>, Boolean> isAllSubtaskGenerated = new HashMap<Class<? extends Task>, Boolean>();

	/**
	 * 当前任务是否已被子任务唤醒过。 参数说明：Class - 子任务类型， Boolean - 是否被该类型子任务唤醒过。
	 */
	private final Map<Class<? extends Task>, Boolean> isBeenAwaked = new HashMap<Class<? extends Task>, Boolean>();

	/**
	 * 待检查的子任务类型。
	 * <p>
	 * 如果当前任务的某子类型isAllSubtaskGenerated为true时，
	 * 可能触发其他子类型的isAllSubtaskGenerated变为true。
	 * <p>
	 * 该属性记录着正在等待当前任务某子类型isAllSubtaskGenerated为true后，需重新检查的子类型列表。
	 * <p>
	 * 参数说明：Class - 当前任务的某子类型，List - 需重新检查的子类型列表。
	 */
	private final Map<Class<? extends Task>, List<Class<? extends Task>>> subclassesOfWaitingForRecheck = new HashMap<Class<? extends Task>, List<Class<? extends Task>>>();

	/**
	 * 等待唤醒当前任务的子任务列表。
	 * <p>
	 * 如果当前某子类型isAllSubtaskGenerated为true时，可能触发某个该类型的子任务对当前任务进行唤醒。
	 * <p>
	 * 该属性记录着正在等待当前任务某子类型isAllSubtaskGenerated为true后，需对当前任务进行唤醒的子任务列表。
	 * <p>
	 * 参数说明：Class - 当前任务的某子类型，Task - 需对当前任务进行唤醒的子任务。
	 */
	private final Map<Class<? extends Task>, Task> subtasksOfWaitingForAwakeSelf = new HashMap<Class<? extends Task>, Task>();

	/**
	 * 构造函数
	 * 
	 * @param belongTask
	 *            当前任务状态所属的任务对象
	 * @throws IllegalArgumentException
	 *             如果belongTask为null
	 */
	public TaskState(Task belongTask) {
		if (null == belongTask) {
			throw new IllegalArgumentException();
		}
		this.belongTask = belongTask;
	}

	/**
	 * 增加重检查的子任务类型。
	 * <p>
	 * 当一个任务的某一类子任务完成生产后，往往会触发其他类型的子任务也完成生产，通过该方法设置了重检查的子任务类型后，
	 * 执行器可在指定类型的子任务完成生产后，重新检查其他类型子任务是否也完成了全部生产。
	 * 
	 * @param subclass
	 *            等待完成生产的子任务类型
	 * @param recheckClass
	 *            需检查的子任务类型
	 * @throws addWaitingForRecheckSubclass
	 *             如果subclass为null
	 * 
	 * @see #recheckSubtaskGeneration(Class)
	 */
	public synchronized void addWaitingForRecheckSubclass(
			Class<? extends Task> subclass, Class<? extends Task> recheckClass) {
		if (null == subclass) {
			throw new IllegalArgumentException();
		}

		List<Class<? extends Task>> recheckList = subclassesOfWaitingForRecheck
				.get(subclass);
		if (null == recheckList) {
			recheckList = new LinkedList<Class<? extends Task>>();
			recheckList.add(recheckClass);
			subclassesOfWaitingForRecheck.put(subclass, recheckList);
		} else {
			if (!recheckList.contains(recheckClass)) {
				recheckList.add(recheckClass);
			}
		}
	}

	/**
	 * 增加正在等待唤醒当前任务的子任务。同类子任务只能有一个等待唤醒当前任务。
	 * 
	 * @param subtask
	 *            等待唤醒当前任务的子任务
	 * @return 如果尚未有同类的任务正在等待唤醒当前任务，返回true，否则便无法增加成功，返回false
	 */
	public synchronized boolean addWaitingSubtask(Task subtask) {
		if (null == subtask) {
			throw new IllegalArgumentException();
		}
		if (subtasksOfWaitingForAwakeSelf.containsKey(subtask.getClass())) {
			return false;
		} else {
			subtasksOfWaitingForAwakeSelf.put(subtask.getClass(), subtask);
			return true;
		}
	}

	/**
	 * 使当前任务的未过唤醒环节的子任务数减1。
	 * 
	 * @param subclass
	 *            待减1的子任务类型
	 * @return 减1后的值
	 */
	public synchronized long decreaseUnAwakedSubtask(
			Class<? extends Task> subclass) {
		if (null == subclass) {
			throw new IllegalArgumentException();
		}

		Long sum = countOfUnAwakedSubtask.get(subclass);
		sum = null == sum ? Long.valueOf(-1L) : Long
				.valueOf(sum.longValue() - 1L);
		countOfUnAwakedSubtask.put(subclass, sum);

		return sum;
	}

	/**
	 * 使当前任务的未过执行环节的子任务数减1。
	 * 
	 * @param subclass
	 *            待减1的子任务类型
	 * @return 减1后的值
	 */
	public synchronized long decreaseUnExecutedSubtask(
			Class<? extends Task> subclass) {
		if (null == subclass) {
			throw new IllegalArgumentException();
		}

		Long sum = countOfUnExecutedSubtask.get(subclass);
		sum = null == sum ? Long.valueOf(-1L) : Long
				.valueOf(sum.longValue() - 1L);
		countOfUnExecutedSubtask.put(subclass, sum);

		return sum;
	}

	/**
	 * 使当前任务等待唤醒的祖先任务数减1。
	 * 
	 * @return 减1后的值
	 */
	public synchronized int decreaseWaitingForAwakeAncestor() {
		return --countOfWaitingForAwakableAncestor;
	}

	/**
	 * 获取前任务的未过唤醒环节的子任务数。
	 * 
	 * @param subclass
	 *            子任务类型
	 * @return 前任务的未过唤醒环节的子任务数
	 */
	public synchronized long getCountOfUnAwakedSubtask(
			Class<? extends Task> subclass) {
		if (null == subclass) {
			throw new IllegalArgumentException();
		}

		Long count = countOfUnAwakedSubtask.get(subclass);

		return null == count ? 0L : count.longValue();
	}

	/**
	 * 获取前任务的未过执行环节的子任务数。
	 * 
	 * @param subclass
	 *            子任务类型
	 * @return 前任务的未过执行环节的子任务数
	 */
	public synchronized long getCountOfUnExecutedSubtask(
			Class<? extends Task> subclass) {
		if (null == subclass) {
			throw new IllegalArgumentException();
		}

		Long count = countOfUnExecutedSubtask.get(subclass);

		return null == count ? 0L : count.longValue();
	}

	/**
	 * 获取当前任务等待唤醒的祖先任务数。
	 * 
	 * @return 当前任务等待唤醒的祖先任务数
	 */
	public synchronized int getCountOfWaitingForAwakeAncestor() {
		return countOfWaitingForAwakableAncestor;
	}

	/**
	 * 获取当前任务被执行的消费者对象。
	 * 
	 * @return 当前任务被执行的消费者对象
	 */
	public TaskProcessor<? extends Task> getExecutedConsumer() {
		return executedConsumer;
	}

	/**
	 * 获取当前任务的父任务类型。
	 * 
	 * @return 当前任务的父任务类型
	 */
	public Class<? extends Task> getParentClass() {
		return parentclassHolder.get(belongTask.getClass());
	}

	/**
	 * 获取当前任务的子任务类型。
	 * 
	 * @return 当前任务的子任务类型
	 */
	public Class<? extends Task> getSubClass() {
		return subclassHolder.get(belongTask.getClass());
	}

	/**
	 * 获取当前任务的类型。
	 * 
	 * @return 当前任务的类型
	 */
	public Class<? extends Task> getTaskClass() {
		return belongTask.getClass();
	}

	/**
	 * 使当前任务的未过唤醒环节的子任务数加1。
	 * 
	 * @param subclass
	 *            待加1的子任务类型
	 * @return 加1后的值
	 */
	public synchronized long increaseUnAwakedSubtask(
			Class<? extends Task> subclass) {
		if (null == subclass) {
			throw new IllegalArgumentException();
		}

		Long sum = countOfUnAwakedSubtask.get(subclass);
		sum = null == sum ? Long.valueOf(1L) : Long
				.valueOf(sum.longValue() + 1L);
		countOfUnAwakedSubtask.put(subclass, sum);

		return sum;
	}

	/**
	 * 使当前任务的未过执行环节的子任务数加1。
	 * 
	 * @param subclass
	 *            待加1的子任务类型
	 * @return 加1后的值
	 */
	public synchronized long increaseUnExecutedSubtask(
			Class<? extends Task> subclass) {
		if (null == subclass) {
			throw new IllegalArgumentException();
		}

		Long sum = countOfUnExecutedSubtask.get(subclass);
		sum = null == sum ? Long.valueOf(1L) : Long
				.valueOf(sum.longValue() + 1L);
		countOfUnExecutedSubtask.put(subclass, sum);

		return sum;
	}

	/**
	 * 使当前任务等待唤醒的祖先任务数加1。
	 * 
	 * @return 加1后的值
	 */
	public synchronized int increaseWaitingForAwakeAncestor() {
		return ++countOfWaitingForAwakableAncestor;
	}

	/**
	 * 判断当前任务是否已完成了所有子任务的生产。默认为false，通过{@link #setAllSubtaskGenerated(Class)}
	 * 可修改为true。
	 * 
	 * @param subclass
	 *            指定的子任务类型
	 * @return 已完成返回true，否则false
	 */
	public synchronized boolean isAllSubtaskGenerated(
			Class<? extends Task> subclass) {
		if (null == subclass) {
			throw new IllegalArgumentException();
		}

		Boolean result = isAllSubtaskGenerated.get(subclass);

		return null == result ? false : result.booleanValue();

		// if (null == result) {
		// if (isAwaked && TaskState.getParentClass(subclass) == getTaskClass())
		// {
		// return true;
		// } else {
		// return false;
		// }
		// } else {
		// return result.booleanValue();
		// }
	}

	/**
	 * 判断当前任务是否已已被子任务唤醒过。默认为false，通过{@link #setBeenAwaked(Class)}可修改为true。
	 * 
	 * @param subclass
	 *            指定的子任务类型
	 * @return 已被唤醒过返回true，否则false
	 */
	public synchronized boolean isBeenAwaked(Class<? extends Task> subclass) {
		if (null == subclass) {
			throw new IllegalArgumentException();
		}

		Boolean result = isBeenAwaked.get(subclass);

		return null == result ? false : result.booleanValue();
	}

	/**
	 * 重检查当前任务是否完成了子任务的全部生产。
	 * <p>
	 * 当一个任务完成了某一类子任务的全部生产后，往往会标识了其当前任务同时完成了其他类型的子任务的完成生产，
	 * 使用该方法可进行是否完成了其他子任务类型生产的检查。
	 * <p>
	 * 该方法是一个递归的方法，如果检查到已完成了某类子任务生产，会递归对该任务类型再做检查。
	 * 
	 * @param subclass
	 *            已完成全部生产的子任务类型
	 * 
	 * @return 检查出已完成全部生产的子任务类型
	 * @throws IllegalArgumentException
	 *             如果recheckSubtaskGeneration为null
	 */
	public synchronized List<Class<? extends Task>> recheckSubtaskGeneration(
			Class<? extends Task> subclass) {
		if (null == subclass) {
			throw new IllegalArgumentException();
		}

		// 用于存放返回结果
		List<Class<? extends Task>> resule = new LinkedList<Class<? extends Task>>();

		// 取出待检查的任务类型列表
		List<Class<? extends Task>> recheckClasses = subclassesOfWaitingForRecheck
				.remove(subclass);

		if (null == recheckClasses) {
			return null;
		}

		for (Class<? extends Task> recheckClass : recheckClasses) {
			// 先确认一下待检查的任务类型是否已被其他线程搞OK了，免得做了无用功。
			if (isAllSubtaskGenerated(recheckClass)) {
				continue;
			}

			// 先标识为检查成功
			boolean isCheckingOK = true;

			// 检查的标准：
			// 遍历待检查任务类型的所有父任务类型，直到前任务的任务类型为止，判断他们是否满足：
			// 1.针对当前任务而言，这些类型的子任务是否存在未唤醒的情况，如果存在，检查失败。
			// 2.针对当前任务而言，这些类型的子任务是否存在为全部生产完的情况，如果存在，检查失败，并把待检查的任务类型加入到该类型的等待列表中去
			Class<? extends Task> parentClass = getParentClass(recheckClass);
			while (null != parentClass && parentClass != belongTask.getClass()) {
				try {
					if (0L < getCountOfUnAwakedSubtask(parentClass)) {
						isCheckingOK = false;
						break;
					}
					if (!isAllSubtaskGenerated(parentClass)) {
						isCheckingOK = false;
						addWaitingForRecheckSubclass(parentClass, recheckClass);
						break;
					}
				} finally {
					parentClass = getParentClass(parentClass);
				}
			}

			if (!isCheckingOK) {
				continue;
			}
			
			// 检查成功，保存结果
			isAllSubtaskGenerated.put(recheckClass, Boolean.valueOf(true));
			resule.add(recheckClass);
			// 递归再检查刚才检查成功的子任务类型
			List<Class<? extends Task>> recursiveCheckResults = recheckSubtaskGeneration(recheckClass);
			// 保存递归检查的结果
			if (null != recursiveCheckResults) {
				for (Class<? extends Task> recursiveCheckResult : recursiveCheckResults) {
					if (!resule.contains(recursiveCheckResult)) {
						resule.add(recursiveCheckResult);
					}
				}
			}
		}

		return 0 == resule.size() ? null : resule;
	}

	/**
	 * 获取并移除正在等待唤醒当前任务的子任务。
	 * 
	 * @param subclass
	 *            指定的子任务类型
	 * @return 如果存在指定任务类型的正在等待唤醒当前任务的子任务，返回该子任务，否则null，如果subclass为null，返回null
	 */
	public synchronized Task removeWaitingSubtask(Class<? extends Task> subclass) {
		return null == subclass ? null : subtasksOfWaitingForAwakeSelf
				.remove(subclass);
	}

	/**
	 * 设置当前任务已完成子任务的生产。
	 * 
	 * @param subclass
	 *            指定的子任务类型
	 */
	public synchronized void setAllSubtaskGenerated(
			Class<? extends Task> subclass) {
		if (null == subclass) {
			throw new IllegalArgumentException();
		}

		isAllSubtaskGenerated.put(subclass, Boolean.valueOf(true));
	}

	/**
	 * 设置当前任务已被子任务唤醒过。
	 * 
	 * @param subclass
	 *            指定的子任务类型
	 */
	public synchronized void setBeenAwaked(Class<? extends Task> subclass) {
		if (null == subclass) {
			throw new IllegalArgumentException();
		}

		isBeenAwaked.put(subclass, Boolean.valueOf(true));

	}

	/**
	 * 设置当前任务被执行的消费者对象。
	 * 
	 * @param executedConsumer
	 *            当前任务被执行的消费者对象
	 */
	public void setExecutedConsumer(
			TaskProcessor<? extends Task> executedConsumer) {
		if (null == executedConsumer) {
			throw new IllegalArgumentException();
		}
		this.executedConsumer = executedConsumer;
	}
}
