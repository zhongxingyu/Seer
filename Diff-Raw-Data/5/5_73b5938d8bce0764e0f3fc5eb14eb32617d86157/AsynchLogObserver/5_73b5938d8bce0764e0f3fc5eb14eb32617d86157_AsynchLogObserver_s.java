 /*******************************************************************************
  * Forwarding on Gates Simulator/Emulator - Eclipse Console
  * Copyright (c) 2012, Integrated Communication Systems Group, TU Ilmenau.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html.
  ******************************************************************************/
 package de.tuilmenau.ics.fog.eclipse.console;
  
 import java.util.LinkedList;
 
 import de.tuilmenau.ics.fog.ui.LogObserver;
 import de.tuilmenau.ics.fog.ui.Logging;
 import de.tuilmenau.ics.fog.ui.Logging.Level;
 import de.tuilmenau.ics.fog.util.Logger;
  
 
 /**
  * Implements a LogObserver, which stores the log message in a queue.
  * It do blocks the entity, which would like to log. The log entries
  * are processed by a separate thread.
  * 
  * Class is required to avoid that blocking operations of the logging
  * block the main simulation. In particular, the Eclipse console
  * might block, which stops the simulation.
  */
 public abstract class AsynchLogObserver extends Thread implements LogObserver
 {
 	private static final int WAIT_TIME_MSEC = 5000;
 	private static final int COLLECT_TIME_MSEC = 200;
 
 
 	public AsynchLogObserver()
 	{    
 	}
 
 	public Level getLogLevel()
 	{
 		return minLevel;
 	}
 
 	public void setLogLevel(Level minimumLevel)
 	{
 		minLevel = minimumLevel;
 	}
 
 	@Override
 	public void log(Level level, Object object, String message)
 	{
 		if(Logging.isLevelAtLeast(minLevel, level)) {
 			String msg = Logger.formatLog(level, object, message);
 			synchronized(messageQueue) {
 				if(!closed) {
 					Entry initEntry = new Entry();
 					initEntry.setlogmsg(msg);
 					initEntry.setlogLevel(level);
 					messageQueue.addLast(initEntry);
 					messageQueue.notify();
 				}
 			}
 		}
 	}
 
 	@Override
 	public void run()
 	{
 		while(!closed) {
 			try {
 				Entry msg = getNext(WAIT_TIME_MSEC);
 
 				if(msg != null) {
 					// wait for some more output
 					Thread.sleep(COLLECT_TIME_MSEC);
 
 					try {
 						print(msg);
 					} catch (Exception exc) {
 						// ignore it
 					}
 				}
 			}
 			catch(InterruptedException exc) {
 				// ignore it
 			}
 		}
 	}
 
 	/**
 	 * Method processes a log message in a separate thread. It might
 	 * block without blocking the simulation.
 	 * 
 	 * @param msg Log entry
 	 * @throws Exception On error
 	 */
 	protected abstract void print(Entry msg) throws Exception;
 
 	/**
 	 * Fetches the next entry in the list of log messages.
 	 * 
 	 * @param waitTimeMSec Max waiting time for a new log message.
 	 * @return Log entry or null, if non available
 	 * @throws InterruptedException On error during waiting
 	 */
 	protected Entry getNext(int waitTimeMSec) throws InterruptedException
 	{
 		synchronized(messageQueue) {
 			if(messageQueue.isEmpty()) {
 				if(waitTimeMSec > 0) {
 					messageQueue.wait(waitTimeMSec);
 					if(!messageQueue.isEmpty())    {
 						Entry initEntry = messageQueue.removeFirst();
 						return initEntry;
 					}
 				}
 			} else {
 				Entry initEntry = messageQueue.removeFirst();
 				return initEntry;
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public void close()
 	{
 		synchronized(messageQueue) {
 			closed = true;
 			messageQueue.clear();
 		}
 	}
 
 	private LinkedList<Entry> messageQueue = new LinkedList<Entry>(); 
 	private Level minLevel = Level.TRACE;
 	private boolean closed = false;
 }
 
 
 /**
  * Helper class to store logmessage and loglevel in class to color logging output.
  */
 class Entry
 {
 	void setlogmsg(String msg)
 	{
 		this.logmsg = msg;
 	}
 	
 	String getlogmsg()
 	{
 		return logmsg;
 	}
 	
 	void setlogLevel(Level level)
 	{
 		logLevel = level;
 	}
 	
 	Level getlogLevel()
 	{
 		return logLevel;
 	}
 	
 	private String logmsg;
 	private Level logLevel;
 }
