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
