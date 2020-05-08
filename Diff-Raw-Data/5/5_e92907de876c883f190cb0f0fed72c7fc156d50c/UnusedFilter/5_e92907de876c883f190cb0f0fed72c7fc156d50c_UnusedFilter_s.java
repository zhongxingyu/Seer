 /**
  * Copyright 2010 CosmoCode GmbH
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package de.cosmocode.palava.util.qa;
 
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ConcurrentMap;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.base.Joiner;
 import com.google.common.base.Preconditions;
 import com.google.common.collect.Maps;
 import com.google.inject.Inject;
 import com.google.inject.Singleton;
 import com.google.inject.name.Named;
 
 import de.cosmocode.commons.reflect.Classpath;
 import de.cosmocode.commons.reflect.Packages;
 import de.cosmocode.commons.reflect.Reflection;
import de.cosmocode.palava.concurrent.BackgroundScheduler;
 import de.cosmocode.palava.core.lifecycle.Initializable;
 import de.cosmocode.palava.core.lifecycle.LifecycleException;
 import de.cosmocode.palava.ipc.IpcCall;
 import de.cosmocode.palava.ipc.IpcCallFilter;
 import de.cosmocode.palava.ipc.IpcCallFilterChain;
 import de.cosmocode.palava.ipc.IpcCommand;
 import de.cosmocode.palava.ipc.IpcCommandExecutionException;
 
 /**
  * A filter which keeps track of all unused commands.
  *
  * @since 1.2
  * @author Willi Schoenborn
  */
 @Singleton
 final class UnusedFilter implements IpcCallFilter, Initializable, Runnable {
 
     private static final Logger LOG = LoggerFactory.getLogger(UnusedFilter.class);
 
     private final Joiner joiner = Joiner.on('\n');
     private final Set<Class<? extends IpcCommand>> commands;
     
     private final ScheduledExecutorService scheduler;
     
     private long period = 1L;
     private TimeUnit periodUnit = TimeUnit.MINUTES;
     
     @Inject
     public UnusedFilter(@Named(UnusedConfig.PACKAGES) List<String> packageNames, 
            @BackgroundScheduler ScheduledExecutorService scheduler) {
         
         final Classpath classpath = Reflection.defaultClasspath();
         final Packages packages = classpath.restrictTo(packageNames);
         final Iterable<Class<? extends IpcCommand>> all = packages.subclassesOf(IpcCommand.class);
         final ConcurrentMap<Class<? extends IpcCommand>, Boolean> map = Maps.newConcurrentMap();
         
         for (Class<? extends IpcCommand> type : all) {
             map.put(type, Boolean.TRUE);
         }
         
         this.commands = Collections.newSetFromMap(map);
         this.scheduler = Preconditions.checkNotNull(scheduler, "Scheduler");
     }
     
     @Override
     public void initialize() throws LifecycleException {
         scheduler.scheduleAtFixedRate(this, period, period, periodUnit);
     }
     
     @Override
     public void run() {
         if (commands.isEmpty()) {
             LOG.info("Congratulations, all commands are currently in use.");
         } else if (LOG.isWarnEnabled()) {
             LOG.warn("Unused commands:\n{}", joiner.join(commands));
         }
     }
     
     @Override
     public Map<String, Object> filter(IpcCall call, IpcCommand command, IpcCallFilterChain chain)
         throws IpcCommandExecutionException {
         
         final Class<? extends IpcCommand> type = command.getClass();
         
         if (commands.remove(type)) {
             LOG.trace("{} has been used, removing from set of unused commands", type);
         }
         
         return chain.filter(call, command);
     }
 
 }
