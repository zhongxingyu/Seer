 /**
  *
  * amass - web crawling made easy
  * Copyright (c) 2011-2013, Sandeep Gupta
  * 
  * http://www.sangupta/projects/amass
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
 
 package com.sangupta.amass.impl;
 
 import java.util.Enumeration;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.PriorityBlockingQueue;
 
 import com.sangupta.amass.core.QueueMessageConverter;
 import com.sangupta.amass.domain.AmassSignal;
 import com.sangupta.amass.domain.CrawlJob;
 import com.sangupta.amass.domain.CrawlableURL;
 import com.sangupta.amass.domain.DefaultCrawlableURL;
 
 /**
  * A priority based queue, that collects all URLs that need to be crawled.
  * All crawling threads use this queue instance to work upon.
  * 
  * This queue uses an integer priority to sort items. The higher the priority
  * value the earlier the URL will be crawled.
  *  
  * @author sangupta
  *
  */
 public class CrawlingQueue {
 	
 	/**
 	 * The default priority of any item when this is added to the
 	 * internal queue.
 	 */
 	private static final int DEFAULT_PRIORITY = 1;
 
 	/**
 	 * The map that maps the URL to a crawling job so that the same URL does
 	 * not get crawled by two different threads at the same go.
 	 */
 	private final ConcurrentHashMap<String, CrawlJob> jobs;
 	
 	/**
 	 * The embeddded priority queue that serves worker threads.
 	 * 
 	 */
 	private final BlockingQueue<CrawlJob> internalQueue;
 	
 	/**
 	 * A blocking queue that is provided from outside.
 	 */
 	private final BlockingQueue<Object> externalQueue;
 	
 	/**
 	 * Converter to use to read from the {@link #externalQueue}.
 	 */
 	@SuppressWarnings("rawtypes")
 	private final QueueMessageConverter queueMessageConverter;
 	
 	/**
 	 * The signal object that let's workers and everyone know if
 	 * the jobs are paused, resumed, or stopped.
 	 * 
 	 */
 	private final AmassSignal amassSignal;
 	
 	/**
 	 * Indicates if a closure of this queue has been seeked.
 	 * 
 	 */
 	private volatile boolean closureSeeked;
 	
 	/**
 	 * Constructor that creates an object of the crawling queue.
 	 * 
 	 * @param externalQueue
 	 * @param amassSignal
 	 */
 	public CrawlingQueue(BlockingQueue<Object> externalQueue, QueueMessageConverter<? extends Object> queueMessageConverter, AmassSignal amassSignal) {
 		this.amassSignal = amassSignal;
 		
 		if(externalQueue != null) {
 			if(queueMessageConverter == null) {
 				throw new IllegalArgumentException("QueueMessageConverter cannot be null when specifying an external queue.");
 			}
 
 			this.jobs = null;
 			this.externalQueue = externalQueue;
 			this.queueMessageConverter = queueMessageConverter;
 			this.internalQueue = null;
 		} else {
 			if(queueMessageConverter != null) {
 				throw new IllegalArgumentException("QueueMessageConverter must be null when using an internal queue.");
 			}
 			
 			this.jobs = new ConcurrentHashMap<String, CrawlJob>();
 			this.internalQueue = new PriorityBlockingQueue<CrawlJob>();
 			this.externalQueue = null;
 			this.queueMessageConverter = null;
 		}
 	}
 	
 	/**
 	 * 
 	 * @param url
 	 * @return
 	 */
 	public boolean submitURL(String url) {
 		return this.submitURL(url, DEFAULT_PRIORITY);
 	}
 
 	/**
 	 * 
 	 * @param url
 	 * @param priority
 	 * @return
 	 */
 	public boolean submitURL(final String url, final int priority) {
 		if(url == null) {
 			return false;
 		}
 		
 		return submitURL(new DefaultCrawlableURL(url), priority);
 	}
 	
 	/**
 	 * 
 	 * @param crawlableURL
 	 * @return
 	 */
 	public boolean submitURL(CrawlableURL crawlableURL) {
 		return this.submitURL(crawlableURL, DEFAULT_PRIORITY);
 	}
 	
 	/**
 	 * 
 	 * @param crawlableURL
 	 * @return
 	 */
 	public boolean submitURL(final CrawlableURL crawlableURL, final int priority) {
 		if(this.internalQueue == null) {
 			throw new IllegalArgumentException("Jobs can only be submitted to internal queue implementations.");
 		}
 		
 		if(crawlableURL == null) {
 			return false;
 		}
 		
 		CrawlJob job = this.jobs.get(crawlableURL.getURL());
 		if(job == null) {
 			job = new CrawlJob(crawlableURL, priority);
 		}
 		
 		CrawlJob previous = this.jobs.putIfAbsent(crawlableURL.getURL(), job);
 		if(previous == null) {
 			// no previous jobs
 			// submit this one up
 			this.internalQueue.offer(job);
 		} else {
 			// there seems to be a job previously submitted
 			// let's increase its priority
 			previous.incrementPriority(priority);
 		}
 		
 		return true;
 	}
 	
 	/**
 	 * Get a crawling job out of this {@link CrawlingQueue}. If no element
 	 * is available in this queue, this method will wait till one is available.
 	 * 
 	 * If this queue is shutting down, it will return a <code>null</code>.
 	 * 
 	 * @return
 	 */
 	public CrawlJob take() {
 		CrawlJob job = null;
 		do {
 			if(isInternalQueueBacked()) {
 				// read from the internal queue if we are using one
 				job = this.internalQueue.poll();
 
 			} else {
 			
 				// else read from the external queue
 				try {
 					Object message = this.externalQueue.take();
 					if(message != null) {
 						// convert this message
 						@SuppressWarnings("unchecked")
 						CrawlableURL crawlableURL = this.queueMessageConverter.convert(message);
 						
 						// if the obtained URL is not null, return back
 						// us a crawling job
 						if(crawlableURL != null) {
 							job = new CrawlJob(crawlableURL);
 						}
 					}
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 			}
 			
 			// see if we are stopping by
 			if(this.amassSignal.isStopping()) {
 				return null;
 			}
 			
 			// any null attributes indicate that there is nothing
 			// in the queue and we might need to wait more.
 			
 			if(job != null) {
 				break;
 			}
 			
 			if(this.closureSeeked) {
 				return null;
 			}
 			
 			// wait for 100 millis before retrying
 			try {
 				Thread.sleep(100);
 			} catch (InterruptedException e) {
 				// eat up
 			}
 		} while(true);
 
 		// remove from the jobs map
 		if(this.jobs != null) {
 			this.jobs.remove(job.getCrawlableURL().getURL());
 		}
 		
 		return job;
 	}
 
 	/**
 	 * Output the debug information on all jobs. This works only for all internal
 	 * jobs.
 	 * 
 	 */
 	public void debugJobInfo() {
 		if(!isInternalQueueBacked()) {
 			return;
 		}
 		
 		for (Enumeration<CrawlJob> myJobs = this.jobs.elements(); myJobs.hasMoreElements(); ) {
 			CrawlJob myJob = myJobs.nextElement();
 			System.out.println("URL " + myJob.getCrawlableURL().getURL() + " with priority of " + myJob.getPriority().get());
 		}
 	}
 	
 	/**
 	 * Specifies if we are running using an internal queue
 	 * backed implementation.
 	 * 
 	 * @return
 	 */
 	public boolean isInternalQueueBacked() {
 		return this.internalQueue != null;
 	}
 
 	/**
 	 * Clear all pending internal jobs and close it out. We do
 	 * not clean up any external queue that is provided, and it's
 	 * responsibility lies with the using application.
 	 * 
 	 */
	private void clearAllJobs() {
 		if(isInternalQueueBacked()) {
 			this.internalQueue.clear();
 		}
 		
		if(this.jobs != null) {
			this.jobs.clear();
		}
 	}
 
 	/**
 	 * Wait for the closure of this queue. The closure time is the time
 	 * till all jobs have been read from this queue.
 	 * 
 	 */
	public void waitForClosure(boolean clearJobs) {
		if(clearJobs) {
			clearAllJobs();
		}
		
 		// we need not wait for any external queue to close
 		// and thus we can shutdown immediately when using such
 		// a queue.
 		if(!isInternalQueueBacked()) {
 			this.closureSeeked = true;
 			return;
 		}
 		
 		// we are using an internal queue, we must wait
 		// till it gets cleared up
 		CrawlJob job = null;
 		do {
 			job = this.internalQueue.peek();
 			if(job == null) {
 				break;
 			}
 			
 			// wait for 250ms
 			try {
 				Thread.sleep(250);
 			} catch (InterruptedException e) {
 				// eat up
 			}
 		} while(true);
 		
 		this.closureSeeked = true;
 	}
 
 }
