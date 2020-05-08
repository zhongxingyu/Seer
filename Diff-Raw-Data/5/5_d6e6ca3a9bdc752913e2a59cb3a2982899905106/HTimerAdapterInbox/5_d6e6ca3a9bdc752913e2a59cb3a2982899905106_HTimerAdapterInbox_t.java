 /*
  * Copyright (c) Novedia Group 2012.
  *
  *     This file is part of Hubiquitus.
  *
  *     Hubiquitus is free software: you can redistribute it and/or modify
  *     it under the terms of the GNU General Public License as published by
  *     the Free Software Foundation, either version 3 of the License, or
  *     (at your option) any later version.
  *
  *     Hubiquitus is distributed in the hope that it will be useful,
  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *     GNU General Public License for more details.
  *
  *     You should have received a copy of the GNU General Public License
  *     along with Hubiquitus.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.hubiquitus.hubotsdk.adapters;
 
 import static org.quartz.CronScheduleBuilder.cronSchedule;
 import static org.quartz.JobBuilder.newJob;
 import static org.quartz.TriggerBuilder.newTrigger;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.hubiquitus.hapi.exceptions.MissingAttrException;
 import org.hubiquitus.hapi.hStructures.HAlert;
 import org.hubiquitus.hapi.hStructures.HMessage;
 import org.hubiquitus.hubotsdk.AdapterInbox;
 import org.hubiquitus.util.TimerClass;
 import org.json.JSONObject;
 import org.quartz.JobDataMap;
 import org.quartz.JobDetail;
 import org.quartz.Scheduler;
 import org.quartz.SchedulerException;
 import org.quartz.SchedulerFactory;
 import org.quartz.Trigger;
 import org.quartz.impl.StdSchedulerFactory;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class HTimerAdapterInbox extends AdapterInbox{
 
 	final Logger logger = LoggerFactory.getLogger(HTimerAdapterInbox.class);
 	
 	private String mode;
 	private String crontab;
 	private int period;
 	
 	private Scheduler scheduler = null;
 	private Timer timer = null;
 		
 	public HTimerAdapterInbox() {}
 		
 	public HTimerAdapterInbox(String actor) {
 		this.actor = actor;
 	}
 
 	@Override
 	public void start() {
 		//Timer using millisecond
 		if(mode.equalsIgnoreCase("millisecond"))
 		{
 			if(period > 0) {
 				timer = new Timer();
 				timer.scheduleAtFixedRate(new TimerTask() {
 			        public void run() {
 				      sendMessage();
 			        }
 			    }, 0, period);
 			} else {
 				logger.error("crontab malformat");
 			}
 		}
 		//Timer using crontab
 		if(mode.equalsIgnoreCase("crontab")) {
 			try {
 				JobDataMap jdm = new JobDataMap(); // pass the sendMessage data to the job class.
 				jdm.put("actor", actor);
 				SchedulerFactory sf = new StdSchedulerFactory();
 				scheduler = sf.getScheduler();
 				// define the job and tie it to the TimerClass
 				JobDetail job = newJob(TimerClass.class)
				    .withIdentity(actor + "timerJob", "group1")
 				    .usingJobData(jdm)
 				    .build();
 				// Trigger the job to run now and use the crontab
 				Trigger trigger = newTrigger()
				    .withIdentity(actor + "Trigger", "group1")
 				    .startNow()
 				    .withSchedule(cronSchedule(crontab))
 				    .build();
 				// Tell quartz to schedule the job using our trigger
 				scheduler.scheduleJob(job, trigger);
 				scheduler.start();
 			} catch (Exception e) {
 				logger.error(e.toString());
 			}
 		}
 	}
 	
 	public void sendMessage() {
 		HMessage timerMessage = new HMessage();
 		timerMessage.setAuthor(actor);
 		timerMessage.setType("hAlert");
 		HAlert halert = new HAlert();
 		try {
 			halert.setAlert(actor);
 		} catch (MissingAttrException e) {
 			logger.error("message: ", e);
 		}
 		timerMessage.setPayload(halert);
 		put(timerMessage);
 	}
 	
 	@Override
 	public void stop() {
 		if(mode.equalsIgnoreCase("crontab") && scheduler != null) {
 			try {
 				scheduler.shutdown();
 			} catch (SchedulerException e) {
 				logger.error(e.toString());
 			}
 		}
 		if(mode.equalsIgnoreCase("millisecond") && timer != null) {
 			timer.cancel();
 		}
 	}
 
 
 	@Override
 	public void setProperties(JSONObject properties) {	
 		if(properties != null) {
 			try {
 				if(properties.has("mode")){
 					this.mode = properties.getString("mode");
 				}
 				if(properties.has("crontab")){
 					this.crontab = properties.getString("crontab");
 				}
 				if(properties.has("period")){
 					this.period = properties.getInt("period");
 				}
 			} catch (Exception e) {
 				logger.debug("message: ", e);
 			}
 		}
 	}
 }
