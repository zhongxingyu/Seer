 package org.aaron.sms.examples;
 
 /*
  * #%L
 * Simple Message Service Examples
  * %%
  * Copyright (C) 2013 Aaron Riekenberg
  * %%
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  * #L%
  */
 
 import java.util.ArrayList;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import org.aaron.sms.api.SMSConnection;
 import org.aaron.sms.api.SMSConnectionListener;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class SMSTestReceiver implements Runnable {
 
 	private static final Logger log = LoggerFactory
 			.getLogger(SMSTestReceiver.class);
 
 	private static final Timer timer = new Timer(true);
 
 	private final AtomicInteger messagesReceived = new AtomicInteger(0);
 
 	private final String topicName;
 
 	public SMSTestReceiver(String topicName) {
 		this.topicName = topicName;
 	}
 
 	@Override
 	public void run() {
 		try {
 			final SMSConnection smsConnection = new SMSConnection("127.0.0.1",
 					10001);
 
 			timer.scheduleAtFixedRate(new TimerTask() {
 				@Override
 				public void run() {
 					log.info(topicName + " messages received last second = "
 							+ messagesReceived.getAndSet(0));
 				}
 			}, 1000, 1000);
 
 			smsConnection.setListener(new SMSConnectionListener() {
 
 				@Override
 				public void handleIncomingMessage(String topicName,
 						byte[] message) {
 					log.debug("handleIncomingMessage topic {} length {}",
 							topicName, message.length);
 					messagesReceived.getAndIncrement();
 				}
 
 				@Override
 				public void handleConnectionOpen() {
 					log.info("handleConnectionOpen");
 				}
 
 				@Override
 				public void handleConnectionClosed() {
 					log.info("handleConnectionClosed");
 				}
 			});
 
 			smsConnection.start();
 
 			smsConnection.subscribeToTopic(topicName);
 
 			while (true) {
 				Thread.sleep(1000);
 			}
 		} catch (Exception e) {
 			log.warn("main", e);
 		}
 	}
 
 	public static void main(String[] args) {
 		final ArrayList<Thread> threadList = new ArrayList<Thread>();
 		for (int i = 0; i < 50; ++i) {
 			final Thread t = new Thread(new SMSTestReceiver("test.topic." + i));
 			t.start();
 			threadList.add(t);
 		}
 		for (Thread t : threadList) {
 			try {
 				t.join();
 			} catch (InterruptedException e) {
 				log.warn("main", e);
 			}
 		}
 	}
 
 }
