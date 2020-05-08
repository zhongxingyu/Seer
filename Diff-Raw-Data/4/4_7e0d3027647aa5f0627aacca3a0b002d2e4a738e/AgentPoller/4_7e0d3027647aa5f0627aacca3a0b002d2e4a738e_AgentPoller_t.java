 /**************************************************************************************
  * Copyright (C) 2008 Progress Software, Inc. All rights reserved.                    *
  * http://fusesource.com                                                              *
  * ---------------------------------------------------------------------------------- *
  * The software in this package is published under the terms of the AGPL license      *
  * a copy of which has been included with this distribution in the license.txt file.  *
  **************************************************************************************/
 package org.fusesource.cloudmix.agent;
 
import java.net.ConnectException;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.Callable;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.beans.factory.DisposableBean;
 import org.springframework.beans.factory.InitializingBean;
 
 /**
  * A polling spring bean which can poll any of the available agents such
  * as {@link InstallerAgent}
  *
  * @version $Revision: 1.1 $
  */
 public class AgentPoller implements InitializingBean, DisposableBean {
     private static final transient Log LOG = LogFactory.getLog(AgentPoller.class);
     private Callable<Object> agent = new InstallerAgent();
     private Timer timer;
     private long pollingPeriod = 1000L;
     private long initialPollingDelay = 500L;
 
     public AgentPoller() {
     }
 
     public AgentPoller(Callable<Object> agent) {
         this.agent = agent;
     }
 
     public void afterPropertiesSet() throws Exception {
         start();
     }
 
     public void start() throws Exception {
         timer = new Timer(true);
         timer.scheduleAtFixedRate(new TimerTask() {
             public void run() {
                 agentPoll();
             }
         }, initialPollingDelay, pollingPeriod);
     }
 
     public void agentPoll() {
         try {
             agent.call();
        } catch (ConnectException e) {
            LOG.debug("polling attempt failed: depot server unavailable");
         } catch (Exception e) {
             LOG.warn("Caught exception while polling Agent: ", e);
         }
     }
 
     public void destroy() throws Exception {
         timer.cancel();
     }
 
     // Properties
     //-------------------------------------------------------------------------
 
     public long getPollingPeriod() {
         return pollingPeriod;
     }
 
     public void setPollingPeriod(long pollingPeriod) {
         this.pollingPeriod = pollingPeriod;
     }
 
     public long getInitialPollingDelay() {
         return initialPollingDelay;
     }
 
     public void setInitialPollingDelay(long initialPollingDelay) {
         this.initialPollingDelay = initialPollingDelay;
     }
 
     public Timer getTimer() {
         return timer;
     }
 
     public void setTimer(Timer timer) {
         this.timer = timer;
     }
 
     public Callable<Object> getAgent() {
         return agent;
     }
 
     public void setAgent(Callable<Object> agent) {
         this.agent = agent;
     }
 }
