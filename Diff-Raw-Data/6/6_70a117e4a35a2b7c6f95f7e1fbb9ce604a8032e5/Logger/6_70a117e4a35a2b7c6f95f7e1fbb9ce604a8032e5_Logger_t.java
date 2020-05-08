 /**
  * 
  */
 package com.seanmadden.logger;
 
 import java.text.DecimalFormat;
 import java.util.*;
 import java.io.*;
 
 /**
  * @author spmfa
  *
  */
 public class Logger {
 
 	private static Hashtable<Object, Logger> loggers = new Hashtable<Object, Logger>();
 	private Vector<Writer> outStreams = new Vector<Writer>();
 	private static long startMilli = System.currentTimeMillis();
 	private static DecimalFormat formatter = new DecimalFormat("00000000");
 	private Object key = null;
 	
 	/**
 	 * 
 	 */
 	private Logger(Object key) {
 		this.key = key;
 		addOutputStream(System.out);
 	}
 	
 	private Logger(Object key, OutputStream out){
 		this.key = key;
 		addOutputStream(out);
 	}
 	
 	public static synchronized Logger getLogger(Object toLog){
 		if(loggers.get(toLog) == null){
 			Logger log = new Logger(toLog);
 			loggers.put(toLog, log);
 		}
 		return loggers.get(toLog);
 	}
 	
 	public static synchronized Logger getLogger(Object toLog, OutputStream out){
 		if(loggers.contains(toLog)){
 			Logger log = loggers.get(toLog);
 			log.addOutputStream(out);
 			return log;
 		}else{
 			Logger log = new Logger(toLog, out);
 			loggers.put(toLog, log);
 			return log;
 		}
 	}
 	
 	public void debug(LogEvent ev, String message) {
 		long curMilli = System.currentTimeMillis() - startMilli;
 		String toWrite = "["+formatter.format(curMilli)+"] " + key+" [" + ev.toString() + "] " + message + "\n";
 		for(Writer out : outStreams){
 			synchronized(Logger.class){
 				try {
 					out.write(toWrite);
 					out.flush();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 	
 	public synchronized void setOptions(){
 		
 	}
 	
 	public synchronized void addWriter(Writer writer){
 		outStreams.add(writer);
 	}
 	
 	public synchronized void addOutputStream(OutputStream out){
 		outStreams.add(new OutputStreamWriter(out));
 	}
 	public static void main(String[] args){
 		Logger log = Logger.getLogger(Logger.class);
 		log.debug(LogEvent.DEBUG, "This is a debug message");
 		try {
 			Thread.sleep(1000);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		log.debug(LogEvent.FATAL, "FATAL ERROR");
 		
 		try {
			FileWriter writer = new FileWriter("test.log");
			log.addWriter(writer);
 			log.debug(LogEvent.INFO, "I should appear in the file");
 			log.debug(LogEvent.TRACE, "SO SHOULD I");
			writer.flush();
			writer.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 }
