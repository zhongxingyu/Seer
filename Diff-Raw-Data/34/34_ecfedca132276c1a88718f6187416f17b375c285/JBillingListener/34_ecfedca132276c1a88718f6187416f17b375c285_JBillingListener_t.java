 /*
     jBilling - The Enterprise Open Source Billing System
     Copyright (C) 2003-2009 Enterprise jBilling Software Ltd. and Emiliano Conde
 
     This file is part of jbilling.
 
     jbilling is free software: you can redistribute it and/or modify
     it under the terms of the GNU Affero General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     jbilling is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU Affero General Public License for more details.
 
     You should have received a copy of the GNU Affero General Public License
     along with jbilling.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 package com.sapienter.jbilling.client.util;
 
 import javax.servlet.ServletContext;
 import javax.servlet.ServletContextEvent;
 import javax.servlet.ServletContextListener;
 
 import com.sapienter.jbilling.client.process.JobScheduler;
 import com.sapienter.jbilling.server.pluggableTask.OrderPeriodTask;
 import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskException;
 import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskManager;
 import com.sapienter.jbilling.server.process.task.IScheduledTask;
 import com.sapienter.jbilling.server.user.db.CompanyDAS;
 import com.sapienter.jbilling.server.user.db.CompanyDTO;
 import com.sapienter.jbilling.server.util.*;
 import org.apache.log4j.Logger;
 
 import com.sapienter.jbilling.client.item.CurrencyArrayWrap;
 import com.sapienter.jbilling.client.process.Trigger;
 import com.sapienter.jbilling.common.SessionInternalError;
 import com.sapienter.jbilling.server.list.IListSessionBean;
 import org.quartz.SchedulerException;
 
 /**
  * Listens for servlet context initialization and destruction. Used to
  * start/stop jBilling services when the servlet container 
  * starts/stops.
  */
 public class JBillingListener implements ServletContextListener {
 
     private static final Logger LOG = Logger.getLogger(JBillingListener.class);
 
     public void contextInitialized(ServletContextEvent event) {
         
         // validate that the java version is correct
         validateJava();
         
         // initializes scheduled billing batch processes
         Trigger.Initialize();
 
         // initializes ScheduledTask plug-ins
         JobScheduler scheduler = JobScheduler.getInstance();
 
         LOG.debug("Processing pluggable scheduled tasks...");
 
         try {
             for (CompanyDTO entity : new CompanyDAS().findEntities()) {
                 PluggableTaskManager<IScheduledTask> taskManager =
                         new PluggableTaskManager<IScheduledTask>
                                 (entity.getId(), com.sapienter.jbilling.server.util.Constants.PLUGGABLE_TASK_SCHEDULED);
 
                 LOG.debug(taskManager.getAllTasks().size() + " scheduled tasks for entity " + entity.getId());

                IScheduledTask task = taskManager.getNextClass();
                while (task != null) {
                    try {
                        scheduler.getScheduler().scheduleJob(task.getJobDetail(), task.getTrigger());
                        LOG.debug("scheduled task '" + task.getTaskName() + "', " + task.getClass());
                    } catch (PluggableTaskException e) {
                        LOG.warn("Failed scheduling the pluggable tasks - " + task.getTaskName());
                    } catch (SchedulerException e) {
                        LOG.warn("Failed scheduling the pluggable tasks - " + task.getTaskName());
                    }
                    task = taskManager.getNextClass();
                }
            }
        } catch (PluggableTaskException e) {
            LOG.error("Exception occurred scheduling pluggable tasks.", e);
        }

        // start the scheduler now that all the tasks have been scheduled.
         LOG.debug("Starting up the scheduler...");
         scheduler.start();
 
         // initialize the currencies, which are in application scope
         ServletContext context = event.getServletContext();
         LOG.debug("Loadding application currency symbols");
         try {
             IListSessionBean myRemoteSession = (IListSessionBean) 
                     Context.getBean(Context.Name.LIST_SESSION);
             context.setAttribute(Constants.APP_CURRENCY_SYMBOLS, 
                     new CurrencyArrayWrap(
                             myRemoteSession.getCurrencySymbolsMap()));
         } catch (Exception e) {
             throw new SessionInternalError(e);
         }
         
     }
     
     private void validateJava() {
         Float version = Float.valueOf(System.getProperty("java.version").substring(0, 3));
         if (version < 1.6F) {
             // can't run!
            LOG.fatal("*********************************************************");
            LOG.fatal("You need Java version 1.6 or higher to run jbilling. " +
                 "Your current version is " + version);
            LOG.fatal("*********************************************************"); 
            System.exit(1);
         }
         
         if (!System.getProperty("java.vendor").matches(".*Sun.*")) {
             LOG.warn("Your java vendor is not Sun. Results are unpredicatble");
         }
     }
 
     public void contextDestroyed(ServletContextEvent event) {
         Context.shutdown(); // shutdown Spring container
         JobScheduler.getInstance().shutdown(); // shutdown Quartz scheduler
     }
 }
