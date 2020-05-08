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
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.sangupta.amass.core.AfterCrawlHandler;
 import com.sangupta.amass.core.BeforeCrawlHandler;
 import com.sangupta.amass.core.CrawlHandler;
 import com.sangupta.amass.domain.AmassSignal;
 import com.sangupta.amass.domain.CrawlJob;
 import com.sangupta.jerry.http.WebResponse;
 
 /**
  * A single thread that crawls one job at a time.
  * 
  * @author sangupta
  *
  */
 public class CrawlingWorker implements Runnable {
 	
 	private static final Logger LOGGER = LoggerFactory.getLogger(CrawlingWorker.class);
 	
 	private static final int THREAD_SLEEP_INTERVAL_ON_PAUSE = 1000;
 	
 	private final CrawlingQueue crawlingQueue;
 	
 	private final BeforeCrawlHandler beforeCrawlHandler;
 	
 	private final CrawlHandler crawlHandler;
 	
 	private final AfterCrawlHandler afterCrawlHandler;
 	
 	private final AmassSignal amassSignal;
 	
 	private volatile boolean working = false;
 	
 	/**
 	 * Indicates if a closure of this thread has been seeked.
 	 * 
 	 */
 	private volatile boolean closureSeeked;
 	
 	public CrawlingWorker(CrawlingQueue crawlingQueue, BeforeCrawlHandler beforeCrawlHandler, CrawlHandler crawlHandler, AfterCrawlHandler afterCrawlHandler, AmassSignal amassSignal) {
 		this.crawlingQueue = crawlingQueue;
 		
 		this.beforeCrawlHandler = beforeCrawlHandler;
 		if(crawlHandler == null) {
 			this.crawlHandler = new DefaultCrawlHandler();
 		} else {
 			this.crawlHandler = crawlHandler;
 		}
 		
 		this.afterCrawlHandler = afterCrawlHandler;
 		
 		this.amassSignal = amassSignal;
 	}
 
 	/**
 	 * Method that gets called by the wrapping {@link Thread} instance.
 	 * 
 	 * @see java.lang.Runnable#run()
 	 */
 	@Override
 	public void run() {
 		this.working = true;
 		runCrawlingJob();
 		this.working = false;
 	}
 	
 	/**
 	 * Seek closure of this worker. The current running job will be completed
 	 * and no more jobs will be picked up from the queue.
 	 * 
 	 */
 	public void seekClosure() {
 		this.closureSeeked = true;
 	}
 	
 	/**
 	 * Run the ever-running job of crawling picking jobs from the {@link CrawlingQueue}
 	 * and executing them as needed.
 	 * 
 	 */
 	private void runCrawlingJob() {
 		do {
 			CrawlJob job = null;
 			try {
 				job = this.crawlingQueue.take();
 			} catch(Throwable t) {
 				LOGGER.debug("Unable to fetch element from queue", t);
 			}
 			
 			// check for stop/pause signal
 			if(this.amassSignal.isStopping()) {
 				break;
 			}
 			
 			pauseIfNeeded();
 			
 			if(job == null) {
 				if(this.closureSeeked) {
 					return;
 				}
 				
 				continue;
 			}
 			
 			boolean crawl = true; 
 			if(this.beforeCrawlHandler != null) {
 				try {
 					crawl = this.beforeCrawlHandler.beforeCrawl(job.getCrawlableURL(), job.getPriority().get());
 				} catch(Throwable t) {
 					// catch all otherwise thread will break
 					LOGGER.error("Unable to run before-crawl handler on url {}", job);
 				}
 
 				// check again for stop/pause as the before operation
 				// may have been an expensive operation
 				if(this.amassSignal.isStopping()) {
 					break;
 				}
 				pauseIfNeeded();
 			}
 			
 			if(!crawl) {
 				continue;
 			}
 			
 			LOGGER.debug("Crawling URL: " + job.getCrawlableURL().getURL() + "... ");
 			
 			long start = System.currentTimeMillis();
 			long end = 0;
 			Throwable throwable = null;
 			WebResponse webResponse= null;
 			try {
 				webResponse = this.crawlHandler.crawl(job.getCrawlableURL());
 			} catch(Throwable t) {
 				throwable = t;
 				LOGGER.error("Unable to execute crawl handler on url {}", job, throwable);
 			} finally {
 				end = System.currentTimeMillis();
 			}
 			final long timeConsumed = end - start;
 
 			// after crawl handler
			if(throwable == null) {
 				try {
 					this.afterCrawlHandler.afterCrawl(job.getCrawlableURL(), job.getPriority().get(), webResponse, timeConsumed);
 				} catch(Throwable t) {
 					LOGGER.error("Unable to execute after-crawl handler on url {}", job, t);
 				}
 			} else {
 				// in case we get an error we must stop crawling now.
 				try {
 					this.afterCrawlHandler.crawlError(job.getCrawlableURL(), job.getPriority().get(), throwable, timeConsumed);
 				} catch(Throwable t1) {
 					LOGGER.error("Unable to execute after-crawl-error handler on url {}", job, t1);
 				}
 			}
 		} while(true);
 	}
 	
 	/**
 	 * Indicates if the crawling thread is working or not.
 	 * 
 	 */
 	public boolean isWorking() {
 		return this.working;
 	}
 	
 	/**
 	 * Pause this thread from execution for a while.
 	 * 
 	 */
 	private void pauseIfNeeded() {
 		do {
 			if(!this.amassSignal.isPaused()) {
 				return;
 			}
 			
 			try {
 				Thread.sleep(THREAD_SLEEP_INTERVAL_ON_PAUSE);
 			} catch (InterruptedException e) {
 				// eat up
 			}
 		} while(true);
 	}
 
 }
