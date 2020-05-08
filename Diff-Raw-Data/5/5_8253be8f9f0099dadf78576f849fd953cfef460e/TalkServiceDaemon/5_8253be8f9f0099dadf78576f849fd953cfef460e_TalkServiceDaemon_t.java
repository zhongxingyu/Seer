 /*
 -----------------------------------------------------------------------------
 This source file is part of Cell Cloud.
 
 Copyright (c) 2009-2012 Cell Cloud Team (cellcloudproject@gmail.com)
 
 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:
 
 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.
 
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.
 -----------------------------------------------------------------------------
 */
 
 package net.cellcloud.talk;
 
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Queue;
 
 import net.cellcloud.common.LogLevel;
 import net.cellcloud.common.Logger;
 import net.cellcloud.talk.dialect.ActionDialect;
 import net.cellcloud.talk.dialect.ActionDialectFactory;
 import net.cellcloud.talk.dialect.DialectEnumerator;
 
 /** Talk Service 守护线程。
  * 
  * @author Jiangwei Xu
  */
 public final class TalkServiceDaemon extends Thread {
 
 	private boolean spinning = false;
 	protected boolean running = false;
 	private long tickTime = 0;
 
 	private Queue<Runnable> tasks;
 
 	public TalkServiceDaemon() {
 		super("TalkServiceDaemon");
 		this.tasks = new LinkedList<Runnable>();
 	}
 
 	/** 返回周期时间点。
 	 */
 	protected long getTickTime() {
 		return this.tickTime;
 	}
 
 	protected void joinTask(Runnable task) {
 		synchronized (this.tasks) {
 			this.tasks.offer(task);
 		}
 	}
 
 	@Override
 	public void run() {
 		this.running = true;
 		this.spinning = true;
 
 		TalkService service = TalkService.getInstance();
 
 		int heartbeatCount = 0;
 		int checkSuspendedCount = 0;
 
 		do {
 			// 当前时间
 			this.tickTime = System.currentTimeMillis();
 
 			++heartbeatCount;
 			if (heartbeatCount >= 60) {
 
				// 300 秒一次心跳
 
 				if (null != service.speakers) {
 					Iterator<Speaker> iter = service.speakers.values().iterator();
 					while (iter.hasNext()) {
 						Speaker speaker = iter.next();
 						speaker.heartbeat();
 					}
 				}
 
 				heartbeatCount = 0;
 			}
 
 			// 检查丢失连接的 Speaker
 			if (service.lostSpeakers != null && !service.lostSpeakers.isEmpty()) {
 				Iterator<Speaker> iter = service.lostSpeakers.iterator();
 				while (iter.hasNext()) {
 					Speaker speaker = iter.next();
 
 					if (this.tickTime - speaker.timestamp >= 10000) {
 						StringBuilder buf = new StringBuilder();
 						buf.append("Retry call cellet ");
 						buf.append(speaker.getIdentifier());
 						buf.append(" at ");
 						buf.append(speaker.getAddress().getAddress().getHostAddress());
 						buf.append(":");
 						buf.append(speaker.getAddress().getPort());
 						Logger.d(TalkServiceDaemon.class, buf.toString());
 						buf = null;
 
 						iter.remove();
 
 						// 重连
 						speaker.call(speaker.getAddress());
 					}
 				}
 			}
 
 			// 处理未识别 Session
 			service.processUnidentifiedSessions(this.tickTime);
 
 			// 1 分钟检查一次
 			++checkSuspendedCount;
			if (checkSuspendedCount >= 12) {
 				// 检查并删除挂起的会话
 				service.checkAndDeleteSuspendedTalk();
 				checkSuspendedCount = 0;
 			}
 
 			// 执行简单任务
 			synchronized (this.tasks) {
 				if (!this.tasks.isEmpty()) {
 					for (int i = 0, size = this.tasks.size(); i < size; ++i) {
 						Runnable task = this.tasks.poll();
 						task.run();
 					}
 				}
 			}
 
 			// 休眠 5 秒
 			try {
 				long dt = System.currentTimeMillis() - this.tickTime;
 				if (dt <= 5000) {
 					dt = 5000 - dt;
 				}
 				else {
 					dt = dt % 5000;
 				}
 
 				Thread.sleep(dt);
 			} catch (InterruptedException e) {
 				Logger.logException(e, LogLevel.ERROR);
 			}
 
 		} while (this.spinning);
 
 		// 关闭所有 Speaker
 		if (null != service.speakers) {
 			Iterator<Speaker> iter = service.speakers.values().iterator();
 			while (iter.hasNext()) {
 				Speaker speaker = iter.next();
 				speaker.hangUp();
 			}
 			service.speakers.clear();
 		}
 
 		ActionDialectFactory factory =
 				(ActionDialectFactory) DialectEnumerator.getInstance().getFactory(ActionDialect.DIALECT_NAME);
 		factory.shutdown();
 
 		Logger.i(this.getClass(), "Talk service daemon quit.");
 		this.running = false;
 	}
 
 	public void stopSpinning() {
 		this.spinning = false;
 	}
 }
