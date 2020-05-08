 /**
  *
  * lineup - In-Memory high-throughput queue
  * Copyright (c) 2013, Sandeep Gupta
  * 
  * http://www.sangupta/projects/lineup
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  * 		http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * 
  */
 
 package com.sangupta.lineup.queues;
 
 import java.util.ArrayList;
 import java.util.Enumeration;
import java.util.Iterator;
 import java.util.List;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.PriorityBlockingQueue;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.sangupta.lineup.domain.QueueMessage;
 
 
 /**
  * @author sangupta
  *
  */
 public class PriorityInternalQueue extends AbstractInternalQueue {
 	
 	/**
 	 * Logger
 	 */
 	private final static Logger LOGGER = LoggerFactory.getLogger(PriorityInternalQueue.class);
 	
 	/**
 	 * Current entries in the map
 	 */
 	private final ConcurrentHashMap<String, QueueMessage> myMessages = new ConcurrentHashMap<String, QueueMessage>();
 	
 	/**
 	 * Keys that need to be removed
 	 */
 	private final List<String> keysToBeRemoved = new ArrayList<String>();
 	
 	/**
 	 * Construct this queue.
 	 * 
 	 */
 	public PriorityInternalQueue(int delaySeconds) {
 		super(delaySeconds);
 	}
 
 	/**
 	 * Create a new instance of the backing {@link PriorityBlockingQueue}.
 	 * 
 	 * @see com.sangupta.lineup.queues.AbstractInternalQueue#getBackingQueue()
 	 */
 	protected BlockingQueue<QueueMessage> getBackingQueue() {
 		return new PriorityBlockingQueue<QueueMessage>();
 	}
 
 	/**
 	 * @see com.sangupta.lineup.queues.AbstractInternalQueue#addMessage(java.lang.String, int)
 	 */
 	@Override
 	public QueueMessage addMessage(final String message, int delaySeconds, int priority) {
 		// clear up any previous backlog
 		if(!this.keysToBeRemoved.isEmpty()) {
			Iterator<String> keyToRemove = this.keysToBeRemoved.iterator();
			while(keyToRemove.hasNext()) {
				this.myMessages.remove(keyToRemove.next());
 			}
 		}
 		
 		// now look up if we want to add the message
 		if(this.myMessages.containsKey(message)) {
 			// increase its priority
 			QueueMessage qm = this.myMessages.get(message);
 			if(qm != null) {
 				LOGGER.debug("Message already exists, incrementing priority: {}", message);
 				qm.incrementPriority(priority);
 				
 				// check if this is not in internal queue
 				return qm;
 			}
 		}
 		
 		// message not already present
 		QueueMessage qm = super.addMessage(message, delaySeconds, priority);
 		if(qm == null) {
 			LOGGER.debug("Unable to add message to queue: {}", message);
 			return null;
 		}
 		
 		QueueMessage older = this.myMessages.putIfAbsent(message, qm);
 		if(older != null) {
 			LOGGER.debug("Concurrency conflict, incrementing priority: {}", message);
 			older.incrementPriority(priority);
 			qm = older;
 		}
 		
 		return qm;
 	}
 	
 	/**
 	 * @see com.sangupta.lineup.queues.AbstractInternalQueue#clear()
 	 */
 	@Override
 	public void clear() {
 		super.clear();
 		
 		// clear the backing map as well
 		this.myMessages.clear();
 	}
 	
 	/**
 	 * @see com.sangupta.lineup.queues.AbstractInternalQueue#removeMessage(com.sangupta.lineup.domain.QueueMessage)
 	 */
 	@Override
 	protected void removeMessage(QueueMessage queueMessage) {
 		if(queueMessage == null) {
 			return;
 		}
 		
 		QueueMessage qm = this.myMessages.remove(queueMessage.getBody());
 		if(qm == null) {
 			LOGGER.debug("Unable to find a message corresponding to key {} in concurrent-map", queueMessage.getBody());
 			
 			// let's clean it up in next thread interaction
 			this.keysToBeRemoved.add(queueMessage.getBody());
 			
 			// dump all keys inside the map
 			Enumeration<String> keys = myMessages.keys();
 			StringBuilder builder = new StringBuilder();
 			while(keys.hasMoreElements()) {
 				builder.append(keys.nextElement());
 				builder.append(", ");
 			}
 			
 			LOGGER.debug("Current keys in map: {}", builder.toString());
 		}
 	}
 }
