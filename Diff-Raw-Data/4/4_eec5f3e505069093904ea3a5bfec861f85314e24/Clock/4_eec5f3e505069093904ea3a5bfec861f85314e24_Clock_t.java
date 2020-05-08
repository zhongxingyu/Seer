 package com.pjf.mat.sim;
 
 import com.pjf.mat.api.MatLogger;
 import com.pjf.mat.api.Timestamp;
 import com.pjf.mat.sim.model.ClockTick;
 import com.pjf.mat.sim.model.SimAccess;
 
 public class Clock extends Thread implements ClockTick{
 	private MatLogger logger;
 	private final int TICK_RATIO = 10;	// # microticks per tick
 	private SimAccess host;
 	private Timestamp simTime;
 	private int timestamp;
 	private int counter;
 	private boolean shutdown;
 	
 	public Clock(SimAccess host, int periodMs, MatLogger logger) {
 		this.logger = logger;
 		this.setName("Clock");
 		this.host = host;
 		simTime = new Timestamp();
 		timestamp = 0;
 		counter = 0;
 		shutdown = false;
 	}
 	
 	public void reset() {
 		this.timestamp = 0;
 	}
 	
	/**
	 * returns a copy of the current time
	 * @return
	 */
 	public Timestamp getSimTime() {
 		return new Timestamp(simTime);
 	}
 	
 	@Override
 	public void run() {
 		while (!shutdown) {
 			processMicroTick();
 		}
 		logger.info("Shutdown.");
 	}
 
 	private void processMicroTick() {
 		simTime.add(1);		// count another microtick in the sim time
 		// FIXME should be trace
 //		logger.debug("processMicroTick(): " + simTime);
 		host.publishMicroTick(getSimTime());
 		counter++;
 		if (counter >= TICK_RATIO) {
 			counter = 0;
 			timestamp = (timestamp + 1) & 0xffff;
 			logger.debug("Tick - " + this);
 			host.publishClockTick(this);  
 		}
 	}
 
 	@Override
 	public int getTimestamp() {
 		return timestamp;
 	}
 
 	public void shutdown() {
 		logger.debug("Shutting down ...");
 		shutdown = true;
 	}
 	
 	@Override
 	public String toString() {
 		return "simtime=" + simTime + ", timestamp=" + timestamp;
 	}
 
 }
