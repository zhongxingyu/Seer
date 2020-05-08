 /*******************************************************************************
  * Forwarding on Gates Simulator/Emulator
  * Copyright (C) 2012, Integrated Communication Systems Group, TU Ilmenau.
  * 
  * This program and the accompanying materials are dual-licensed under either
  * the terms of the Eclipse Public License v1.0 as published by the Eclipse
  * Foundation
  *  
  *   or (per the licensee's choosing)
  *  
  * under the terms of the GNU General Public License version 2 as published
  * by the Free Software Foundation.
  ******************************************************************************/
 package de.tuilmenau.ics.fog;
 
 import java.util.PriorityQueue;
 
 import de.tuilmenau.ics.CommonSim.datastream.StreamTime;
 import de.tuilmenau.ics.fog.Config.Simulator;
 import de.tuilmenau.ics.fog.Config.Simulator.SimulatorMode;
 import de.tuilmenau.ics.fog.ui.Logging;
 import de.tuilmenau.ics.fog.util.Logger;
 
 
 /**
  * Provides event handling with and without time dependency
  * 
  * TODO Future plan: Use CommonSim for event handling based on time!
  */
 public class EventHandler extends Thread
 {
 	private static final boolean DEBUG_OUTPUT = false;
 	private static final double EPSILON = 0.000001d;
 	
 	
 	public EventHandler()
 	{
 		if(Config.Simulator.MODE != SimulatorMode.STEP_SIM) {
 			mCurrentTime = 0;
 			alignSimTimeWithSystemTime();
 			
 			start();
 		} else {
 			mCurrentTime = 0;
 		}
 	}
 	
 	/**
 	 * @return real-time system time stamp in seconds
 	 */
 	private double getSystemTime()
 	{
 		return mSystemTimeOffsetSec + (double) System.currentTimeMillis() / 1000.0d;
 	}
 	
 	/**
 	 * Recalculates difference between system time and simulation time
 	 */
 	private void alignSimTimeWithSystemTime()
 	{
 		mSystemTimeOffsetSec = mCurrentTime -(double) System.currentTimeMillis() / 1000.0d;
 		if(DEBUG_OUTPUT) {
 			mLogger.log(this, "Align with system time by offset " +mSystemTimeOffsetSec);
 		}
 	}
 	
 	/**
 	 * @return Time in seconds
 	 */
 	public double now()
 	{
 		return mCurrentTime;
 	}
 	
 	/**
 	 * @return Time in seconds in stream time object (!= null)
 	 */
 	public StreamTime nowStream()
 	{
 		return new StreamTime(mCurrentTime);
 	}
 	
 	/**
 	 * @return Difference to real time of last event execution in seconds (>0 ahead of real time)
 	 */
 	public double getLastEventDiff()
 	{
 		return mLastDiff;
 	}
 	
 	/**
 	 * @return Amount of events processed
 	 */
 	public long getEventCounter()
 	{
 		return mEventCounter;
 	}
 	
 	/**
 	 * @return Number of events scheduled 
 	 */
 	public long getNumberScheduledEvents()
 	{
 		return mEventQueue.size();
 	}
 
 	/**
 	 * @return Number of packet events scheduled 
 	 */
	public int getNumberScheduledPacketDeliveryEvents()
 	{
 		int tResult = 0;
 		
 		synchronized (mEventQueuePacketDeliveryEvents) {
 			tResult = mEventQueuePacketDeliveryEvents;
 		}
 		
 		return tResult;
 	}
 
	public void incNumberScheduledPacketDeliveryEvents()
 	{
 		synchronized (mEventQueuePacketDeliveryEvents) {
 			mEventQueuePacketDeliveryEvents++;	
 		}
 	}
 	
 
	public void decNumberScheduledPacketDeliveryEvents()
 	{
 		synchronized (mEventQueuePacketDeliveryEvents) {
 			mEventQueuePacketDeliveryEvents--;	
 		}		
 	}
 
 	public IEventRef scheduleAt(double time, IEvent event)
 	{
 		if(event != null) {
 			if(time >= now()) {
 				synchronized(mEventQueue) {
 					if(DEBUG_OUTPUT) {
 						mLogger.trace(this, "Scheduling " +event +" at " +time);
 					}
 					
 					// Test if it is newer than the newest in queue. If so, we
 					// will have to wake up the event handling thread later
 					// on.
 //					boolean newer = time < getNewestEventTime();
 					
 					// Create event and store it in queue
 					EventHolder res = new EventHolder(time, event);
 					mEventQueue.add(res);
 					
 					// wake up thread waiting for newest event
 					// TODO seems not to work, since it confuses somehow the time order of events
 /*					if(newer) {
 						mEventQueue.notify();
 					}*/
 					
 					return res;
 				}
 			} else {
 				throw new RuntimeException("Time from the past. Can not schedule event " +event);
 			}
 		} else {
 			throw new RuntimeException("Invalid null event in EventHandler.scheduleAt");
 		}
 	}
 	
 	public IEventRef scheduleIn(double afterSeconds, IEvent event)
 	{
 		if(afterSeconds < 0) {
 			throw new RuntimeException(this +" - negative paramter afterSeconds is not allowed.");
 		}
 		
 		// synch here in order to prevent time passing by between both calls
 		synchronized(mEventQueue) {
 			return scheduleAt(now() +afterSeconds, event);
 		}			
 	}
 	
 	public void cancelEvent(IEventRef event)
 	{
 		boolean tRes;
 		
 		synchronized(mEventQueue) {
 			tRes = mEventQueue.remove(event);
 		}
 		
 		// Event not found? Maybe it is in the event queue of
 		// the step mode. Try to remove it from there.
 		if(!tRes) {
 			synchronized(this) {
 				if(mStepModeEventQueue != null) {
 					mStepModeEventQueue.remove(event);
 				}
 			}
 		}
 	}
 	
 	public void logSheduledEvents() 
 	{
 		int i = 0;
 		synchronized (mEventQueue) {
 			mLogger.log(this, "Holding " + mEventQueue.size() + " events:");
 			for(EventHolder tEventHolder : mEventQueue){
 				mLogger.log(this, "     ..holding event [" + i + "]: " + tEventHolder.mEvent + ", time: " + tEventHolder.mTime);
 				i++;
 			}
 		}
 	}
 
 	/**
 	 * Real time mode of the event handler. This method tries
 	 * to execute the events based on the real system time.
 	 */
 	public void run()
 	{
 		EventHolder tEvent = null;
 		
 		Thread.currentThread().setName("EventHandler");
 
 		if(Simulator.MODE != SimulatorMode.STEP_SIM) {
 			// increase thread priority in order to favor it over
 			// application threads doing stupid stuff, which would
 			// block execution of the main task of handling events
 			// in time
 			int tPrio = Math.min(getPriority() +3, MAX_PRIORITY);
 			setPriority(tPrio);
 			
 			while(!mExit) {
 				if(!mPaused) {
 					try {
 						tEvent = null;
 						
 						synchronized(mEventQueue) {
 							// Current time is either the simulator time or the real system time.
 							// The simulator time might be ahead of real time in fast mode.
 							double newCurrentTime = getSystemTime();
 							double nextEventTime = getNewestEventTime();
 							
 							// negative waiting time = event too late
 							// positive waiting time = event too early
 							double timeToEventSec = nextEventTime -newCurrentTime;
 							
 							if( (timeToEventSec <= 0) || (mFastMode && !Double.isInfinite(nextEventTime)) ) {
 								tEvent = mEventQueue.poll();
 								if (Config.Connection.LOG_PACKET_STATIONS){
 									Logging.log(this, "Polled event: " + tEvent.mEvent + ", id: " + tEvent.mId);
 									logSheduledEvents();
 								}
 								if(DEBUG_OUTPUT) {
 									mLogger.trace(this, "polled event with time " +tEvent.mTime);
 								}
 								
 								setNewTime(tEvent.mTime);
 							} else {
 								// cut too long waiting times
 								// (because of infinity for no event in queue)
 								double waitTimeSec = timeToEventSec;
 								
 								if(waitTimeSec > EVENT_HANDLER_DELAY_SEC) {
 									waitTimeSec = EVENT_HANDLER_DELAY_SEC;
 									
 									newCurrentTime = mCurrentTime +waitTimeSec;
 								} else {
 									newCurrentTime = nextEventTime;
 								}
 								
 								long waitTimeMSec = toMSec(waitTimeSec);
 								if(DEBUG_OUTPUT) {
 									mLogger.trace(this, "waiting " +waitTimeMSec +" msec; set time to " +newCurrentTime);
 								}
 								setNewTime(newCurrentTime);
 								if(waitTimeMSec > 0) mEventQueue.wait(waitTimeMSec);
 							}
 						}
 						
 						// do not synchronize the event execution itself,
 						// because it might call some methods in EventHandler
 						if(tEvent != null) {
 							mLastDiff = tEvent.mTime -getSystemTime();
 							
 							executeEvent(tEvent);
 						}
 					}
 					catch(Exception exc) {
 						mLogger.err(this, "Fatal exception in event loop.", exc);
 						if(tEvent != null) {
 							// Clear event, maybe it was the reason for the error.
 							// Do not try to print the event, because the error
 							// might be in toString.
 							mLogger.err(this, "Skipping current event of type " +tEvent.mEvent.getClass());
 						}
 					}
 				} else {
 					// event execution paused
 					// -> wait for some time and check condition again
 					synchronized(this) {
 						if(mPaused) {
 							try {
 								wait(toMSec(EVENT_HANDLER_DELAY_SEC));
 							} catch (InterruptedException tExc) {
 								// ignore it and continue
 							}
 						}
 					}
 				}
 			} 
 		}
 		// else: nothing to do -> step mode
 	}
 	
 	/**
 	 * Step mode: Executes only the next event from the event queue.
 	 */
 	public void process()
 	{
 		EventHolder tEvent = null;
 
 		synchronized (mEventQueue) {
 			tEvent = mEventQueue.poll();
 			if(tEvent != null) {
 				executeEvent(tEvent);
 			}
 		}
 	}
 	
 	public void setFastMode(boolean pFastMode)
 	{
 		synchronized (mEventQueue) {
 			boolean tSlowDown = mFastMode && !pFastMode;
 
 			if(tSlowDown) {
 				alignSimTimeWithSystemTime();
 			}
 
 			mFastMode = pFastMode;			
 		}
 	}
 	
 	public boolean isInFastMode()
 	{
 		return mFastMode;
 	}
 	
 	/**
 	 * @return true if caller is event thread itself or if there is no event thread at all; otherwise false
 	 */
 	public boolean inEventThread()
 	{
 		if(Simulator.MODE == SimulatorMode.STEP_SIM) return true;
 		
 		return (Thread.currentThread() == this);
 	}
 	
 	public synchronized void pause(boolean pPausing)
 	{
 		synchronized (mEventQueue) {
 			// do we start again after a pause?
 			if(mPaused && !pPausing) {
 				alignSimTimeWithSystemTime();
 			}
 			
 			mPaused = pPausing;
 			
 			// inform threads waiting for resuming
 			if(mPaused == false) {
 				notifyAll();
 			}
 		}
 	}
 	
 	public boolean isPaused()
 	{
 		return mPaused;
 	}
 	
 	public void exit()
 	{
 		mExit = true;
 		
 		try {
 			// if the event thread itself called the exit method,
 			// we do not need to wait.
 			if(Thread.currentThread() != this) {
 				if(isAlive()) join();
 			}
 		}
 		catch(Exception exc) {
 			// ignore it
 		}
 		
 		// cleanup
 		mEventQueue.clear();
 	}
 	
 	public void waitForEmptyQueue()
 	{
 		while(!mEventQueue.isEmpty()) {
 			// run time command automatically until queue is empty,
 			// if there is no event handle thread active
 			if(!isAlive()) {
 				process();
 			}
 			// else: EventThread will empty the queue
 			
 			try {
 				Thread.sleep(1000);
 			} catch (InterruptedException tExc) {
 				mLogger.warn(this, "Failure during wait for event queue. Waiting again...", tExc);
 			}
 		}
 	}
 	
 	private void setNewTime(double newTime)
 	{
 		if(mCurrentTime <= newTime) {
 			mCurrentTime = newTime;
 		} else {
 			if(Math.abs(mCurrentTime -newTime) > EPSILON) {
 				// should not happen; internal error of event handler
 				if(Config.Simulator.MODE != SimulatorMode.FAST_SIM) {
 					String errorMsg = "Event " +((mCurrentTime -newTime) *1000.0d) +" msec behind current time while next event would be " + mEventQueue.peek() + ".";
 					mLogger.err(this, errorMsg);
 				} 
 				// else: ignore it in long running simulations
 			}
 		}
 	}
 	
 	/**
 	 * Executes an event and sets the current time to the time of
 	 * this event.
 	 * 
 	 * @param event Event to be executed (!= null)
 	 */
 	private void executeEvent(EventHolder event)
 	{
 		// Set current time to event time. It should stay constant during
 		// the event execution.
 		setNewTime(event.mTime);
 		
 		try {
 			if(DEBUG_OUTPUT) {
 				mLogger.trace(this, now() +" - firing " +event.mEvent);
 			}
 			mEventCounter++;
 			
 			event.mEvent.fire();
 		} catch(Exception exc) {
 			// do not call toString because that might be the reason for the exception
 			mLogger.err(this, "Exception in event " +event.mEvent.getClass(), exc);
 			for (StackTraceElement tStep : Thread.currentThread().getStackTrace()){
 			    Logging.err(this, "    .." + tStep);
 			}
 			System.exit(1);
 		}
 	}
 	
 	/**
 	 * @return Time of the newest event currently in the queue
 	 */
 	private double getNewestEventTime()
 	{
 		if(mEventQueue.size() > 0) {
 			EventHolder event = mEventQueue.peek();
 			
 			return event.mTime;
 		} else {
 			return Double.POSITIVE_INFINITY;
 		}
 	}
 	
 	/**
 	 * Converts a double value [sec] to a long [msec]. The value
 	 * is round up in order to avoid numerical problems with
 	 * summing up int and double. Rounding down leads to
 	 * summing integer zeros, but double values are non-zero.   
 	 *   
 	 * @param seconds Seconds
 	 * @return Milliseconds (rounded up) 
 	 */
 	private long toMSec(double seconds)
 	{
 		return (long)Math.ceil(seconds * 1000.0d);
 	}
 
 	/**
 	 * Helper class storing an event and its time.
 	 * It is used as implementation of the IEventRef needed
 	 * as result of the schedule operation.
 	 */
 	private static class EventHolder implements Comparable<EventHolder>, IEventRef
 	{
 		private final double mTime;
 		private final IEvent mEvent;
 		static long mGlobalId = 0;
 		long mId = 0;
 		
 		public EventHolder(double time, IEvent event) {
 			mTime  = time;
 			mEvent = event;
 			mId = ++mGlobalId;
 		}
 
 		@Override
 		public int hashCode()
 		{
 			return super.hashCode();
 		}
 
 		@Override
 		public boolean equals(Object o)
 		{
 			if (o instanceof EventHolder) {
 				if(this == o) return true;
 				
 				return (mTime == (((EventHolder)o).mTime)) && mEvent.equals(((EventHolder)o).mEvent);				
 			}
 			return false;
 		}
 
 		@Override
 		public int compareTo(EventHolder o) {
 			if (this.mTime < o.mTime) {
 				return -1;
 			}
 			else if (this.mTime > o.mTime) {
 				return 1;
 			}
 			else {
 				if (this.mId < o.mId) {
 					return -1;
 				}
 				else if (this.mId > o.mId) {
 					return 1;
 				}
 				else {
 					Logging.getInstance().err(null, "We should never reach this point but we did!");
 					return 0;
 				}
 			}
 		}
 		
 		@Override
 		public String toString()
 		{
 			return this.getClass().getSimpleName() + ":" + (mEvent != null ? mEvent.getClass().getSimpleName() : "!NO-EVENT!");
 		}
 	}
 	
 	private static final double EVENT_HANDLER_DELAY_SEC = ((double)Config.Simulator.REAL_TIME_GRANULARITY_MSEC) / 1000.0d;
 	
 	// flag for terminating the real time event execution
 	private volatile boolean mExit = false;
 	
 	// indicates if the simulation should run in fast or real-time mode
 	private volatile boolean mFastMode = (Config.Simulator.MODE == SimulatorMode.FAST_SIM);
 	
 	// indicates if time handling is running or paused
 	private volatile boolean mPaused = false;
 	
 	// current time of the event handling
 	private volatile double mCurrentTime;
 	
 	// time difference to real time of last event execution
 	private volatile double mLastDiff = 0;
 	
 	// number handled events
 	private volatile long mEventCounter = 0;
 	
 	// difference of system time and last fast mode end
 	private double mSystemTimeOffsetSec = 0;
 	
 	private Integer mEventQueuePacketDeliveryEvents = 0;
 	
 	// event queue
 	private PriorityQueue<EventHolder> mEventQueue = new PriorityQueue<EventHolder>();
 	
 	// event queue for the step mode
 	private PriorityQueue<EventHolder> mStepModeEventQueue = null;
 	
 	// logger for output of event handling
 	private Logger mLogger = Logging.getInstance();
 
 }
