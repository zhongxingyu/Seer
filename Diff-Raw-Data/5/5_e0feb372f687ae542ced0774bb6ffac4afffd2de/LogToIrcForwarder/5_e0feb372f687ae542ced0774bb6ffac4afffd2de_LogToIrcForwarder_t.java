 /*
   Copyright 2011-2013 Faiumoni e. V.
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 */
 package net.sf.ircappender.impl;
 
 
 /**
  * forwards log events from the queue to the IRC connection
  *
  * @author hendrik
  */
 public class LogToIrcForwarder implements Runnable {
 	private final Fifo eventQueue;
 	private final IrcConnection ircConnection;
 	private final long messageDelay;
 	private final String channel;
 
 
 	/**
 	 * creates a new LogToIrcForwarder
 	 *
 	 * @param ircConnection the irc connection to send messages to
 	 * @param eventQueue    the event queue with log events to process
 	 * @param messageDelay  the delay between messages to prevent excess flood kills
 	 * @param channel       the channel to post to
 	 */
 	public LogToIrcForwarder(IrcConnection ircConnection, Fifo eventQueue, long messageDelay, String channel) {
 		this.ircConnection = ircConnection;
 		this.eventQueue = eventQueue;
 		this.messageDelay = messageDelay;
 		this.channel = channel;
 	}
 
 	/**
 	 * a thread forwarding messages from the log event queue to IRC
 	 */
 	public void run() {
 
 		while (ircConnection.isRunning()) {
 			if (!(eventQueue == null) && !eventQueue.isEmpty()) {
 				transferEntry();
 			}
 			try {
 				Thread.sleep(messageDelay);
 			} catch (Exception e) {
 				// ignore
 			}
 		}
 
 		while (!eventQueue.isEmpty() && !ircConnection.isChannelEmpty()) {
 			transferEntry();
 		}
 	}
 
 
 	/**
 	 * Transfers one LoggingEvent Entry from the eventQue to the ircMessage que
 	*/
 	private void transferEntry() {
 		String temp = eventQueue.pop();
 		if (!ircConnection.isChannelEmpty()) {
			ircConnection.sendMessage(channel, temp);
 		}
 	}
 
 }
