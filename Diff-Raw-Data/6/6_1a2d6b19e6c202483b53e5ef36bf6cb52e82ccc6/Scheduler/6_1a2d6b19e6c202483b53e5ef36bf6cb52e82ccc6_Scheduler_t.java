 /*
  * Copyright 2002-2012 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.bia.monitor.service;
 
 import com.bia.monitor.dao.GenericDao;
 import com.bia.monitor.data.Job;
 import java.util.List;
 import java.util.concurrent.ScheduledThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 import javax.annotation.PostConstruct;
 import javax.annotation.PreDestroy;
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import static org.springframework.data.mongodb.core.query.Criteria.where;
 import static org.springframework.data.mongodb.core.query.Query.query;
 import org.springframework.stereotype.Component;
 
 /**
  * Schedular creates jobs which runs on given time intervals
  * @author Intesar Mohammed
  */
 @Component
 public class Scheduler {
 
 	protected static final Logger logger = Logger.getLogger(Scheduler.class);
     
 	static final int UP_CHECK_INTERVAL = 15; 
 	static final int DOWN_CHECK_INTERVAL = 1;
 	
 	protected GenericDao generciDao;
 	private ScheduledThreadPoolExecutor executor;
     
     @Autowired
     public Scheduler(GenericDao generciDao) {
     	this.generciDao = generciDao;
     }
    
     @PostConstruct
     protected void setUpExecutor() {
 
         executor = new ScheduledThreadPoolExecutor(10);
         
     	// prod
     	// 15 minutes check for up sites
         executor.scheduleAtFixedRate(new VerifyMethod(generciDao, false), 0, UP_CHECK_INTERVAL, TimeUnit.MINUTES);
         // one minute check for only down sites
         executor.scheduleAtFixedRate(new VerifyMethod(generciDao, true), 0, DOWN_CHECK_INTERVAL, TimeUnit.MINUTES);
 
         // dev env
         //executor.scheduleAtFixedRate(new VerifyMethod(generciDao, false), 0, 2, TimeUnit.MINUTES);
         logger.info("setting up executor!");
         
     }
 
     
     private class VerifyMethod implements Runnable {
 
     	protected final Logger logger_ = Logger.getLogger(VerifyMethod.class);
     	
         private boolean checkDown;
         private GenericDao genericDao_;
 
         VerifyMethod(GenericDao genericDao_, boolean checkDown) {
             this.checkDown = checkDown;
             this.genericDao_ = genericDao_;
             logger_.info(" checker added for site-up=" + checkDown + " genericDao=" + genericDao_);
         }
 
         @Override
         public void run() {
         	List<Job> list;
             try {
 	            if (checkDown) {
 	                list = genericDao_.getMongoTemplate().find(query(where("lastUp").is(Boolean.FALSE)), Job.class);
 	                logger_.info("checking " + (list != null ? list.size() : 0 ) + " down sites!");
 	                //list = mongoOps.findAll(Job.class);
 	            } else {
 	                list = genericDao_.getMongoTemplate().find(query(where("lastUp").is(Boolean.TRUE)), Job.class);
 	                logger_.info("checking " + (list != null ? list.size() : 0 ) + " up sites!");
 	            }
 	            for (Job job : list) {
                        if ( job.getEmail() != null && !job.getEmail().isEmpty() ) {
                            Runnable job_ = new JobCheck(genericDao_.getMongoTemplate(), job);
                            executor.schedule(job_, 10, TimeUnit.MILLISECONDS);
                        }
 	            }
             } catch (Exception e) {
             	logger_.error(e.getMessage(), e);
             }
         }
     }
 
     @PreDestroy
     public void shutdown() {
         this.executor.shutdown();
     }
 }
