 /**
  *    Copyright 2012 meltmedia
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 package com.meltmedia.cadmium.core.scheduler;
 
 import java.lang.reflect.Method;
 import java.util.HashSet;
 import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
 
 import javax.inject.Inject;
 import javax.inject.Singleton;
 
 import org.reflections.Reflections;
 
 import com.google.inject.Binder;
 import com.google.inject.TypeLiteral;
 import com.meltmedia.cadmium.core.Scheduled;
 
 /**
  * This service schedules all @Scheduled classes and methods to run as they are Annotated.
  * 
  * @author John McEntire
  *
  */
 @Singleton
 public class SchedulerService {
   
   /**
    * The Executor that will run all {@link SchedulerTask} instances.
    */
   @Inject
  protected ScheduledExecutorService executor;
   
   /**
    * The tasks to schedule.
    */
   @Inject
   protected Set<SchedulerTask> tasks;
   
   /**
    * Schedules all tasks injected in by guice.
    * 
    * @param tasks
    */
   public void setupScheduler() {
     for(SchedulerTask task : tasks) {
       if(task.getDelay() > 0 && task.getInterval() > 0) {
         executor.scheduleWithFixedDelay(task.getTask(), task.getDelay(), task.getInterval(), task.getTimeUnit());
       } else if(task.isImmediate() && task.getInterval() > 0) {
         executor.scheduleWithFixedDelay(task.getTask(), 0l, task.getInterval(), task.getTimeUnit());
       } else if(task.getDelay() > 0) {
         executor.schedule(task.getTask(), task.getDelay(), task.getTimeUnit());
       } else {
         executor.execute(task.getTask());
       }
     }
   }
    
   /**
    * Binds all Classes and methods Annotated with @Scheduled into a Guice Binder.
    * 
    * @param binder
    * @param reflections
    */
   public static void bindScheduled(Binder binder, Reflections reflections) {
     Set<Class<?>> classes = reflections.getTypesAnnotatedWith(Scheduled.class, true);
     Set<Method> methods = reflections.getMethodsAnnotatedWith(Scheduled.class);
     Set<SchedulerTask> tasks = new HashSet<SchedulerTask>();
     for(Class<?> clazz : classes) {
       if(Runnable.class.isAssignableFrom(clazz)){
         tasks.add(new SchedulerTask(clazz));
       }
     }
     for(Method method : methods) {
       tasks.add(new SchedulerTask(method.getDeclaringClass(), method));
     }
     for(SchedulerTask task : tasks) {
       binder.requestInjection(task);
     }
     
     binder.bind(new TypeLiteral<Set<SchedulerTask>>(){}).toInstance(tasks);
   }
   
   
 }
