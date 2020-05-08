 /*
 Copyright 2008 Flaptor (flaptor.com) 
 
 Licensed under the Apache License, Version 2.0 (the "License"); 
 you may not use this file except in compliance with the License. 
 You may obtain a copy of the License at 
 
     http://www.apache.org/licenses/LICENSE-2.0 
 
 Unless required by applicable law or agreed to in writing, software 
 distributed under the License is distributed on an "AS IS" BASIS, 
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 See the License for the specific language governing permissions and 
 limitations under the License.
 */
 
 package com.flaptor.util;
 
 import java.io.Serializable;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.apache.log4j.Logger;
 
 import com.flaptor.hist4j.AdaptiveHistogram;
 
 /**
  * class for keeping statistics of events. Events can be registered and kept track of.
  * Event data is collected in periods. You can get the accumulated data from start, the data from the
  * last period and the data gathered in this (unfinished) period.   
  * 
  * There is a singleton instance configured in either common.properties (statistics.period) 
  * or 1 minute if thats missing
  * 
  * @author Martin Massera
  */
 public class Statistics {
     private static final Logger logger = Logger.getLogger(com.flaptor.util.Execute.whoAmI());
 	private static final Statistics instance;
 	
 	static {
 	    int time;
 	    try{ 
 	        time = Config.getConfig("common.properties").getInt("statistics.period");
 	    } catch (IllegalStateException e) {
 	        time = 60000;
 	        logger.warn("statistics.period not found in common.properties");
 	    }
 	    instance = new Statistics(time);
 	}
 	//returns a static instance that updates every minute 
 	public static Statistics getStatistics() {
 		return instance;
 	}
 	
 	//map of event -> AccumulatedStats, thisPeriodStats, lastPeriodStats
 	private Map<String, Triad<EventStats,EventStats,EventStats>> eventStatistics = new HashMap<String, Triad<EventStats,EventStats,EventStats>>();
 	private int periodLength;
 	
 	Statistics(int periodLength) {
 		this.periodLength = periodLength;
         new Timer().schedule(new StatisticsTask(), 0, periodLength);
 	}
 
 	/**
 	 * notifies the value of an event
 	 * @param eventName
 	 * @param value
 	 */
 	public void notifyEventValue(String eventName, float value) {
 	    Triad<EventStats,EventStats,EventStats> eventStats = getOrCreateStats(eventName);
 		eventStats.first().addSample(value);
 		eventStats.second().addSample(value);		
 	}
 
 	/**
 	 * notifies an error of an event 
 	 * @param eventName
 	 */
 	public void notifyEventError(String eventName) {
 	    Triad<EventStats,EventStats,EventStats> eventStats = getOrCreateStats(eventName);
 		eventStats.first().addError();
 		eventStats.second().addError();		
 	}
 
 	private Triad<EventStats,EventStats,EventStats> getOrCreateStats(String eventName) {
 	    Triad<EventStats,EventStats,EventStats> eventStats = eventStatistics.get(eventName);
 		if (eventStats == null) {
 			eventStats = new Triad<EventStats,EventStats,EventStats>(new EventStats(), new EventStats(), new EventStats());
 			eventStatistics.put(eventName, eventStats);
 		}
 		return eventStats;
 	}
 
     public Set<String> getEvents() {
         return eventStatistics.keySet();
     }
 
     /**
      * 
      * @param eventName
      * @return the statistics in form <accumulatedStats, thisPeriodStats, lastPeriodStats>
      */
 	public Triad<EventStats,EventStats,EventStats> getStats(String eventName) {
 		return eventStatistics.get(eventName);
 	}
 
     public EventStats getAccumulatedStats(String eventName) {
         return eventStatistics.get(eventName).first();
     }
 
     public EventStats getThisPeriodStats(String eventName) {
 		return eventStatistics.get(eventName).second();
 	}
 
     public EventStats getLastPeriodStats(String eventName) {
         return eventStatistics.get(eventName).third();
     }
 
 	public void clearAccumulatedStats(String eventName) {
 	    eventStatistics.get(eventName).first().clear();
 	}
 
 	public int getPeriodLength() {
 		return periodLength;
 	}
 	
 	public static class EventStats implements Serializable {
 		public long numCorrectSamples;
 		public long numErrors;
 		public float errorRatio;
 
 		public float sampleAverage;
 		
 		public float maximum;
 		public float minimum;
 		
 		AdaptiveHistogram histogram = new AdaptiveHistogram();
 		
 		public EventStats() {
 			clear();
 		}
 		
 		private synchronized void clear() {
 			numErrors = 0;
 			numCorrectSamples = 0;
 			errorRatio = 0;
 			sampleAverage = 0;
 			maximum= -100000000000000.0f;
 			minimum= 100000000000000.0f;
 			histogram.reset();
 		}
 		
 		private synchronized void addSample(float value) {
 			numCorrectSamples++;
 			sampleAverage = (numCorrectSamples * sampleAverage + value) / numCorrectSamples ;
 			
 			if (value < minimum) minimum = value;
 			if (value > maximum) maximum = value;
 
 			histogram.addValue(value);
 			
             errorRatio = numErrors / (numErrors + numCorrectSamples);
 		}
 
 		private synchronized void addError() {
 			numErrors++;
 			errorRatio = numErrors / (numErrors + numCorrectSamples);
 		}
 	}
 	
 	private class StatisticsTask extends TimerTask {
 		public void run() {
 			for (Triad<EventStats, EventStats, EventStats> stats : eventStatistics.values()) {
				//clear the previous lastPeriodStats and swap with thisPeriodStats
			    EventStats temp = stats.third();
                temp.clear();
 				stats.setThird(stats.second());
				stats.setSecond(temp);
 			}
 		}
 	}
 }
