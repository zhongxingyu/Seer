 
 package org.paxle.se.index.lucene.impl;
 
 import java.io.IOException;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
import org.paxle.core.prefs.Properties;
 
 public class TimerLuceneManager extends AFlushableLuceneManager {
 	
 	private final Timer timer = new Timer("LuceneFlushTimer");
 	private final TimerTask flushTTask = new FlushTTask();
 	private final Log logger = LogFactory.getLog(TimerLuceneManager.class);
 	
 	private class FlushTTask extends TimerTask {
 		@Override
 		public void run() {
 			try {
 				TimerLuceneManager.this.checkFlush();
 			} catch (IOException e) {
 				logger.error("I/O exception while flushing LuceneManager", e);
 			}
 		}
 	}
 	
	public TimerLuceneManager(String path, final PaxleAnalyzer analyzer, final Properties properties, int flushDelay, int flushPeriod) throws IOException {
		super(path, analyzer, properties);
 		this.timer.schedule(this.flushTTask, flushDelay, flushPeriod);
 	}
 	
 	@Override
 	public void close() throws IOException {
 		this.timer.cancel();
 		super.close();
 	}
 }
