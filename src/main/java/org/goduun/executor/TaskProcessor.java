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

import java.util.Set;

/**
 * 执行器的任务处理者。
 * <p>
 * 执行器的任务处理者负责从任务队列中获取并处理任务，一个执行器会为每个处理者对象分配一个或多个线程来执行，待执行完成后，线程资源将被回收。
 * 只要执行器的任务队列中有任务存在 ，执行器就会不断尝试启动处理者，直到所有的任务都被处理完成。
 * <p>
 * <b><font color=red>请务必采用线程安全的方式实现该接口。</font></b>
 * <p>
 * 处理者处理任务分两步：
 * <ol>
 * <li>执行任务。</li>
 * <li>触发唤醒。</li>
 * </ol>
 * 处理者通过触发唤醒来实现对批量任务的同步控制。
 * <p>
 * <b>唤醒机制</b>
 * <ol>
 * <li>任务与任务之间可以有关联关系，关系描述详见：{@link Task}。</li>
 * <li>在整颗任务树上，如果将一个任务及其同父的兄弟任务视为叶子节点，再将该任务某个指定的祖先任务视为根节点，可以得到整颗任务树中的一颗子树。</li>
 * <li>当处理者执行完任务之后，会判断该任务所属的整颗任务子树是否全部完成，如果已全部完成，会唤醒根节点任务。</li>
 * <li>
 * 在使用唤醒机制之前，必须明确子树的范围，即指定哪一类祖先任务为子树的根节点，否则，不会唤醒。在执行器开始执行后，该设置不能再被改变，否则会引起唤醒混乱。</li>
 * </ol>
 * <b>唤醒场景示例：移历史</b>
 * <p>
 * 需求：从数据库中读取一条记录，生成一个任务（称之为数据任务），随后该任务又被转换成一系列的子任务，待所有的子任务执行完成后，
 * 需要将数据任务对应的数据库记录移到历史表。
 * <p>
 * 解决：将数据任务视为任务树的根节点，将移历史的逻辑写在对子树叶子节点唤醒祖先的方法中，即可解决。
 * 
 * @author Hu Ruomin
 * @param <C>
 *            可处理的任务类型
 */
public interface TaskProcessor<C extends Task> {

	/**
	 * 唤醒祖先任务。
	 * <p>
	 * 唤醒前，执行器会将当前线程的数据源切换至祖先任务的默认数据源。
	 * 
	 * @param ancestor
	 *            被唤醒的祖先任务
	 * @param task
	 *            触发唤醒的任务
	 */
	void awake(Task ancestor, Task task);

	/**
	 * 执行任务。
	 * <p>
	 * 
	 * 如果执行时有异常抛出，执行器会终止本次处理，回收线程资源， 已分配给本次处理的任务不会被重新分配，因此实现该方法时请务必处理掉所有的异常。
	 * <p>
	 * tasks列表中的任务个数，取决于{@link TaskProcessor#getCapacity()}的返回值，以及当前任务队列中的任务是否充足。
	 * <p>
	 * tips： 整批tasks任务具有同样的数据源，并且执行器在调用该方法前，会自动将数据源切换，如无特殊需要，实现该方法时无需判断任务的数据源，
	 * 也无需手工切换数据源。
	 * 
	 * @param tasks
	 *            待消费的任务
	 */
	void execute(TaskList<C> tasks);

	/**
	 * 获取当前处理者需要唤醒的任务类型。
	 * 
	 * @return 当前处理者需要唤醒的任务类型
	 */
	Set<Class<? extends Task>> getAwakableClasses();

	/**
	 * 获取处理者每次执行可处理的最大任务数。
	 * <p>
	 * 如果方法调用时有异常抛出，或返回结果小于1，执行器会默认其返回结果为1。
	 * 
	 * @return 处理者每次执行可处理的最大任务数
	 */
	int getCapacity();

	/**
	 * 设置处理者每次执行可处理的最大任务数。
	 * 
	 * @param capacity
	 *            处理者每次执行可处理的最大任务数
	 */
	void setCapacity(int capacity);

	/**
	 * 判断当前处理者是否需要做唤醒操作。
	 * <p>
	 * 仅当该处理者未设置任何可唤醒的任务类型时，该方法才返回false，否则返回true。
	 * 
	 * @return 需要返回true，否则false
	 */
	boolean shouldAwake();

	/**
	 * 判断当前处理者所处理的任务是否需要唤醒指定的祖先任务类型。
	 * <p>
	 * 由于任务的祖先类型可能很多，而实际使用中，并不是每一类祖先都需要唤醒，因此，执行器通过该方法来减少唤醒的次数，提升执行效率。
	 * 
	 * @param ancestorClass
	 *            祖先任务类型
	 * @return 需要唤醒返回true，否则false
	 */
	boolean shouldAwake(Class<? extends Task> ancestorClass);
}
