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
 
 package com.sangupta.amass;
 
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import com.sangupta.amass.core.AfterCrawlHandler;
 import com.sangupta.amass.core.BeforeCrawlHandler;
 import com.sangupta.amass.core.CrawlHandler;
 import com.sangupta.amass.core.CrawlingHandler;
 import com.sangupta.amass.core.QueueMessageConverter;
 import com.sangupta.amass.domain.AmassSignal;
 import com.sangupta.amass.domain.CrawlableURL;
 import com.sangupta.amass.impl.CrawlingQueue;
 import com.sangupta.amass.impl.CrawlingWorker;
 
 
 /**
  * Amass is a crawler that allows you to crawl a given number of URLs,
  * allowing both pre-processing and post-processing. It also allows to
  * submit a URL more than once, which then increases its priority to be
  * crawled.
  * 
  * Amass is a high-throughput concurrent library for gathering data from
  * the Internet. It also supports minimum time-thresholds before a URL
  * is crawled again.
  * 
  * @author sangupta
  *
  */
 public class Amass {
 	
 	/**
 	 * Helps identify thread groups uniquely when multiple {@link Amass} instances
 	 * are created.
 	 */
 	private static final AtomicInteger AMASS_INSTANCE_COUNT = new AtomicInteger(1);
 	
 	/**
 	 * The total number of worker thread of this {@link Amass} instance.
 	 */
 	private final int numThreads;
 	
 	/**
 	 * The handler that needs to be executed before each URL is crawled by
 	 * this {@link Amass} instance.
 	 * 
 	 */
 	private final BeforeCrawlHandler beforeCrawlHandler;
 	
 	/**
 	 * The handler that needs to be executed after each URL is crawled by
 	 * this {@link Amass} instance.
 	 * 
 	 */
 	private final AfterCrawlHandler afterCrawlHandler;
 	
 	/**
 	 * The handler that needs to be executed for crawling each URL.
 	 * 
 	 */
 	private final CrawlHandler crawlHandler;
 	
 	/**
 	 * The job queue that is used by this {@link Amass} instance.
 	 */
 	private final CrawlingQueue crawlingQueue;
 	
 	/**
 	 * The thread-group that this {@link Amass} instance creates.
 	 * 
 	 */
 	private final ThreadGroup workerGroup;
 	
 	/**
 	 * The actual worker threads of this {@link Amass} instance.
 	 * 
 	 */
 	private final Thread[] workerThreads;
 	
 	/**
 	 * The actual worker objects that have been created for this {@link Amass} instance.
 	 */
 	private final CrawlingWorker[] workers; 
 	
 	/**
 	 * The state signal for this {@link Amass} instance.
 	 */
 	private final AmassSignal amassSignal;
 	
 	/**
 	 * Indicates whether closure of this {@link Amass} instance has been seeked.
 	 * Once the instance is closed, no more crawling jobs can be submitted to this
 	 * instance.
 	 */
 	private volatile boolean closed = false;
 	
 	public Amass(int numThreads, AfterCrawlHandler afterCrawlHandler) {
 		this(numThreads, null, null, null, null, afterCrawlHandler);
 	}
 	
 	/**
 	 * Create a new instance of {@link Amass} that uses the given
 	 * number of threads for crawling purposes.
 	 * 
 	 * @param numThreads
 	 */
 	public Amass(final int numThreads, final BeforeCrawlHandler beforeCrawlHandler, final AfterCrawlHandler afterCrawlHandler) {
 		this(numThreads, null, null, beforeCrawlHandler, null, afterCrawlHandler);
 	}
 	
 	public Amass(int numThreads, CrawlingHandler crawlingHandler) {
 		this(numThreads, null, null, crawlingHandler, crawlingHandler, crawlingHandler);
 	}
 	
 	public Amass(int numThreads, BlockingQueue<Object> backingQueue, QueueMessageConverter<? extends Object> queueMessageConverter, CrawlingHandler crawlingHandler) {
 		this(numThreads, backingQueue, queueMessageConverter, crawlingHandler, crawlingHandler, crawlingHandler);
 	}
 	
 	/**
 	 * Create a new instance of {@link Amass} that uses the given number of threads
 	 * for crawling purposes and the given backing {@link BlockingQueue} to read
 	 * crawling jobs from.
 	 * 
 	 * @param numThreads
 	 * @param backingQueue
 	 * @param beforeCrawlHandler
 	 * @param afterCrawlHandler
 	 */
 	public Amass(final int numThreads, final BlockingQueue<Object> backingQueue, final QueueMessageConverter<? extends Object> queueMessageConverter, final BeforeCrawlHandler beforeCrawlHandler, final CrawlHandler crawlHandler, final AfterCrawlHandler afterCrawlHandler) {
 		if(numThreads <= 0) {
 			throw new IllegalArgumentException("Number of threads cannot be less than one.");
 		}
 		
 		if(afterCrawlHandler == null) {
 			throw new IllegalArgumentException("After crawl handler cannot be null.");
 		}
 		
 		this.numThreads = numThreads;
 		
 		this.beforeCrawlHandler = beforeCrawlHandler;
 		this.crawlHandler = crawlHandler;
 		this.afterCrawlHandler = afterCrawlHandler;
 		
 		this.amassSignal = new AmassSignal();
 		this.crawlingQueue = new CrawlingQueue(backingQueue, queueMessageConverter, this.amassSignal);
 		
 		this.workerGroup = new ThreadGroup("Amass-Workers-" + AMASS_INSTANCE_COUNT.getAndIncrement());
 		this.workers = new CrawlingWorker[this.numThreads];
 		this.workerThreads = new Thread[this.numThreads];
 		
 		initializeCrawlingThreads();
 	}
 	
 	/**
 	 * Pause all crawling now. Only the current jobs that are running 
 	 * will go to completion. If the crawler is already paused, this
 	 * method has no effect. If the crawler is initializing or is stopped,
 	 * the method throws {@link IllegalStateException}.
 	 * 
 	 */
 	public void pause() {
 		this.amassSignal.pause();
 	}
 	
 	/**
 	 * Resume all crawling now. If the crawler is already running
 	 * this method has no effect. If the crawler is initializing
 	 * or is stopped, the method throws {@link IllegalStateException}.
 	 * 
 	 */
 	public void resume() {
 		this.amassSignal.resume();
 	}
 	
 	/**
 	 * Add the given URL to the queue of crawling jobs. The job is added with a
 	 * default priority of ONE. If the URL is already present, its priority is
 	 * incremented by ONE.
 	 * 
 	 * @param url
 	 *            the URL to be crawled.
 	 * 
 	 * @throws IllegalArgumentException
 	 *             if the URL is empty or null
 	 * 
 	 * @throws IllegalStateException
 	 *             if this {@link Amass} instance has been closed.
 	 */
 	public void crawl(final String url) {
 		if(this.closed) {
 			throw new IllegalStateException("Amass instance has already been closed.");
 		}
 		
 		this.crawlingQueue.submitURL(url);
 	}
 	
 	/**
 	 * Add the given URL to the queue of crawling jobs with the given priority.
 	 * If the URL is already present, its priority is incremented with the given
 	 * amount.
 	 * 
 	 * @param url
 	 *            the URL to be crawled.
 	 * @param priority
 	 *            the priority to assign to this URL.
 	 * 
 	 * @throws IllegalArgumentException
 	 *             if the URL is empty or null
 	 * 
 	 * @throws IllegalStateException
 	 *             if this {@link Amass} instance has been closed.
 	 */
 	public void crawl(final String url, final int priority) {
 		if(this.closed) {
 			throw new IllegalStateException("Amass instance has already been closed.");
 		}
 		
 		this.crawlingQueue.submitURL(url, priority);
 	}
 	
 	/**
 	 * Add the given {@link CrawlableURL} instance to the queue of crawling
 	 * jobs. The job is added with a default priority of ONE. If the job is
 	 * already present, its priority is incremented by ONE.
 	 * 
 	 * @param crawlableURL
 	 *            the job to be added
 	 * 
 	 * @throws IllegalArgumentException
 	 *             if the URL is empty or null
 	 * 
 	 * @throws IllegalStateException
 	 *             if this {@link Amass} instance has been closed.
 	 */
 	public void crawl(CrawlableURL crawlableURL) {
 		if(this.closed) {
 			throw new IllegalStateException("Amass instance has already been closed.");
 		}
 		
 		this.crawlingQueue.submitURL(crawlableURL);
 	}
 	
 	/**
 	 * Add the given {@link CrawlableURL} instance to the queue of crawling jobs
 	 * with the given priority. if the job is already present, its priority is
 	 * incremented by the given value.
 	 * 
 	 * @param crawlableURL
 	 *            the job to be added
 	 * 
 	 * @param priority
 	 *            the priority to use when adding/increment this job
 	 * 
 	 * @throws IllegalArgumentException
 	 *             if the URL is empty or null
 	 * 
 	 * @throws IllegalStateException
 	 *             if this {@link Amass} instance has been closed.
 	 */
 	public void crawl(CrawlableURL crawlableURL, final int priority) {
 		if(this.closed) {
 			throw new IllegalStateException("Amass instance has already been closed.");
 		}
 		
 		this.crawlingQueue.submitURL(crawlableURL, priority);
 	}
 	
 	/**
 	 * Indicates if this {@link Amass} instance has pending work left or not.
 	 *  
 	 * @return
 	 */
 	public boolean hasPendingWork() {
 		return false;
 	}
 
 	// Internal methods follow
 	
 	/**
 	 * Initialize all worker threads and set the state of this instance to
 	 * that of intialized.
 	 * 
 	 */
 	protected void initializeCrawlingThreads() {
 		this.amassSignal.setInitializing();
 		
 		final CrawlingWorker crawlingThread = new CrawlingWorker(this.crawlingQueue, this.beforeCrawlHandler, this.crawlHandler, this.afterCrawlHandler, this.amassSignal);
 		for(int index = 0; index < this.numThreads; index++) {
 			Thread thread = new Thread(this.workerGroup, crawlingThread, "Amass-Worker-" + index);
 			
 			this.workers[index] = crawlingThread;
 			this.workerThreads[index] = thread;
 			
 			thread.start();
 		}
 		
 		this.amassSignal.setInitialized();
 	}
 	
 	/**
 	 * Close this instance and stop accepting more jobs.
 	 * 
 	 */
 	public void close() {
 		this.closed = true;
 	}
 	
 	/**
 	 * Wait till the completion of all jobs and then shut down. This method
 	 * will auto-close this instance, and no more crawling jobs will be accepted.
 	 * 
 	 */
 	public void waitAndShutdown() {
 		this.close();
 		
 		// check for closure of queue
		this.crawlingQueue.waitForClosure(false);
 		
 		// check for closure of all crawling threads
 		waitForClosureOfCrawlingThreads(false);
 	}
 	
 	/**
 	 * Cancel all current jobs and then shut down. This method will auto-close
 	 * this instance, and no more crawling jobs will be accepted.
 	 * 
 	 */
 	public void cancelAndShutdown() {
 		this.close();
 		
 		// signal stopping of everything right away
 		this.amassSignal.stop();
 		
 		// interrupt all running worker threads
 		this.workerGroup.interrupt();
 		
 		// check for closure of queue
		this.crawlingQueue.waitForClosure(true);
 		
 		// check for closure of all crawling threads
 		waitForClosureOfCrawlingThreads(true);
 	}
 	
 	/**
 	 * Method that checks for closure of all worker crawling threads. The method
 	 * waits till all running worker threads do not indicate that they are done.
 	 * 
 	 * @param interruptThreads
 	 *            Indicates if the threads need to be interrupted. This means
 	 *            that this is a forced shutdown of the crawling instance.
 	 */
 	protected void waitForClosureOfCrawlingThreads(final boolean interruptThreads) {
 		for(int index = 0; index < this.numThreads; index++) {
 			final CrawlingWorker worker = this.workers[index];
 			final Thread workerThread = this.workerThreads[index];
 
 			do {
 				if(!workerThread.isAlive()) {
 					break;
 				}
 				
 				worker.seekClosure();
 				
 				if(interruptThreads) {
 					workerThread.interrupt();
 				}
 				
 				try {
 					workerThread.join();
 				} catch (InterruptedException e) {
 					// eat up
 				}
 			} while(true);
 		}
 	}
 
 	/**
 	 * Output the debug information about various jobs that
 	 * we have.
 	 */
 	protected void debugJobInfo() {
 		this.crawlingQueue.debugJobInfo();
 	}
 	
 	// Usual accessors follow
 
 	/**
 	 * @return the numThreads
 	 */
 	public int getNumThreads() {
 		return this.numThreads;
 	}
 
 	/**
 	 * @return the beforeCrawlHandler
 	 */
 	public BeforeCrawlHandler getBeforeCrawlHandler() {
 		return this.beforeCrawlHandler;
 	}
 
 	/**
 	 * @return the afterCrawlHandler
 	 */
 	public AfterCrawlHandler getAfterCrawlHandler() {
 		return this.afterCrawlHandler;
 	}
 
 }
