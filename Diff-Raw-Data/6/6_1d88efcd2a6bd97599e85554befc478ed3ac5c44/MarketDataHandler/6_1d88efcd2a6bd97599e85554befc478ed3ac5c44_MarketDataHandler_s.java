 package com.fhx.strategy.java;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 import java.text.SimpleDateFormat;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import org.apache.log4j.LogManager;
 import org.apache.log4j.Logger;
 import org.marketcetera.marketdata.interactivebrokers.LatestMarketData;
 
 public class MarketDataHandler implements Runnable {
 	private static Logger log = LogManager.getLogger(MarketDataHandler.class);
 
 	private static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");
     
     private List<String> symbolList;
 	private RandomAccessFile[] symbolDataFileHandles;
 
 	private BlockingQueue<Hashtable<String, LatestMarketData>> mdQueue;
 	private AtomicInteger tickCount = new AtomicInteger(0);
 	
 	public MarketDataHandler(List<String> symbolList, BlockingQueue<Hashtable<String, LatestMarketData>> mdQueue) {
 		this.symbolList = symbolList;
 		this.mdQueue = mdQueue;
 		
 		// create file handle for each symbol
 		// we store data each symbol file
 		String fileDate = DATE_FORMAT.format(new Date());
 		
 		try {
     		File dataDir = new File("/export/data/"+fileDate);
     		if (!dataDir.exists()) {
     			dataDir.mkdirs();
     		}
     		
     		System.out.println("$$$ handling market data for " + symbolList.size() + " symbols.");
     		
     		symbolDataFileHandles = new RandomAccessFile[symbolList.size()];
     		String dataDirPath = dataDir.getAbsolutePath();
     		
 			for (int i=0; i < symbolList.size(); i++) {
 	    		symbolDataFileHandles[i] = new RandomAccessFile(dataDirPath+"/"+symbolList.get(i)+"_"+fileDate+"_tick.csv","rw");
 	    		
 	    		// write header 
 	    		symbolDataFileHandles[i].writeBytes("Symbol,Bid,Ask,TradePrice,TradeSize,SourceTime,CreateTime\n");
 			}
 			
 			// start tick data processing
 			log.info("XXXXXX: Init TickDataContainer");
 			TickDataContainer.INSTANCE.init();
 			 
 		}
 		catch(FileNotFoundException e){
 			e.printStackTrace();
 		}
 		catch(IOException e){
 			e.printStackTrace();
 		}
 		
 	}
 	
 	@Override
 	public void run() {
 		// create basic window ticks 
 		while(true) {
 			try {
 				Hashtable<String, LatestMarketData> ticks = this.mdQueue.take();
 
 				// only log between 9:25 and 16:05
 			    Calendar cal = Calendar.getInstance();
 			    
 			    int hh = cal.get(Calendar.HOUR_OF_DAY);
 			    int mm = cal.get(Calendar.MINUTE);
 			    
 			    if (hh < 9 || (hh == 9 && mm < 29))
 			    	continue;			    	
 //			    else if (hh > 16 || (hh == 16 && mm > 1))
 //			    	continue; 
 			    else {	
 			    	for (Map.Entry<String, LatestMarketData> tick : ticks.entrySet()) {
 			    		String symbol = tick.getKey();
 			    		LatestMarketData data = tick.getValue();
 
 			    		// write to file
 			    		int fileHandleIdx = symbolList.indexOf(symbol);
 
 			    		if (fileHandleIdx >=0 && fileHandleIdx <= symbolDataFileHandles.length) {
 			    			symbolDataFileHandles[fileHandleIdx].writeBytes(data.toString());
 			    		}
 			    		else {
 			    			log.error("ERROR: no file handle is available for symbol: " + symbol + ", index " + fileHandleIdx);
 			    			log.error("symbol list \n" + Arrays.toString(symbolList.toArray()));
 			    		}
 			    	}
 	
 		    		/*
 		    		 * Collect tick data in the TickDataContainer
 		    		 */
			    	if (tickCount.get() % 12 == 0) {
 			    		// log every 60 seconds
			    		log.info("adding ticks ["+tickCount.getAndIncrement()+"] to tickDataContainer");
 			    	}
 		    		TickDataContainer.INSTANCE.addATick(ticks);
 			    }
 			}
 			catch(Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 }
