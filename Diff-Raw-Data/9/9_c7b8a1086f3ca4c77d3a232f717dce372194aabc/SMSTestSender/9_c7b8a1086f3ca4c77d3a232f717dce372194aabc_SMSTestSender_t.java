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
 
 import org.aaron.sms.api.SMSConnection;
 import org.aaron.sms.api.SMSConnectionListener;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class SMSTestSender implements Runnable {
 
 	private static final Logger log = LoggerFactory
 			.getLogger(SMSTestSender.class);
 
 	private final String topicName;
 
 	public SMSTestSender(String topicName) {
 		this.topicName = topicName;
 	}
 
 	@Override
 	public void run() {
 		try {
 			final SMSConnection smsConnection = new SMSConnection("127.0.0.1",
 					10001);
 
 			smsConnection.setListener(new SMSConnectionListener() {
 
 				@Override
 				public void handleIncomingMessage(String topicName,
 						byte[] message) {
 					log.debug("handleIncomingMessage topic {} length {}",
 							topicName, message.length);
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
 
 			final byte[] buffer = new byte[5000];
 			while (true) {
 				smsConnection.writeToTopic(topicName, buffer);
 				Thread.sleep(1);
 			}
 		} catch (Exception e) {
 			log.warn("main", e);
 		}
 	}
 
 	public static void main(String[] args) {
 		final ArrayList<Thread> threadList = new ArrayList<Thread>();
 		for (int i = 0; i < 50; ++i) {
 			final Thread t = new Thread(new SMSTestSender("test.topic." + i));
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
