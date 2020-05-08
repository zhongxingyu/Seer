 package br.ufmg.dcc.vod.spiderpig.jobs;
 
 import java.util.concurrent.TimeUnit;
 
 import org.apache.log4j.Logger;
 
 import com.google.common.base.Stopwatch;
 
 public class ThroughputManager {
 
 	private static final Logger LOG = Logger.getLogger(ThroughputManager.class);
 	private static final int MIN_DELTA = 100;
 	
 	private final Stopwatch stopwatch;
 	
 	private final long timeBetweenRequests;
 	private final long backoffTime;
 
 	public ThroughputManager(long timeBetweenRequests,
 			long backoffTime) {
 		this.stopwatch = new Stopwatch();
 		this.timeBetweenRequests = timeBetweenRequests;
 		this.backoffTime = backoffTime;
 		LOG.info("Will sleep every " + timeBetweenRequests + " ms");
 	}
 	
 	private <T> T getAndStartWatch(String crawlID, Requester<T> requester) 
 			throws Exception {
 		
 		T returnVal = null;
 		boolean backoff = true;
 		while(backoff) {
 			try {
 				returnVal = requester.performRequest(crawlID);
 				backoff = false;
 			} catch (QuotaException qe) {
 				LOG.info("Quota Exceeded. Backing off for " +
 						this.backoffTime + "ms -" + qe);
 				
 				if (this.backoffTime > 0)
 					TimeUnit.MILLISECONDS.sleep(this.backoffTime);
			} catch (Exception e) {
 				this.stopwatch.start();
				throw e;
 			}
 		}
 		this.stopwatch.start();
 		return returnVal;
 	}
 
 	public <T> T sleepAndPerform(String crawlID, Requester<T> requester) 
 			throws Exception {
 		if (!this.stopwatch.isRunning()) {
 			return getAndStartWatch(crawlID, requester);
 		} else {
 			this.stopwatch.stop();
 			long elapsedMillis = this.stopwatch.elapsedMillis();
 			long toSleep = timeBetweenRequests - elapsedMillis;
 			if (toSleep > MIN_DELTA) {
 				LOG.info("" +elapsedMillis + " ms ellapsed since last request."
 						 + " sleeping for " + toSleep + " ms");
 				TimeUnit.MILLISECONDS.sleep(toSleep);
 			}
 			
 			return getAndStartWatch(crawlID, requester);
 		}
 	}	
 }
