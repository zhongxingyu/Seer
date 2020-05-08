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
 
 package de.cosmocode.palava.ipc.execvm;
 
 import java.util.Map;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.base.Preconditions;
 import com.google.common.collect.Maps;
 import com.google.inject.Inject;
 import com.google.inject.Injector;
 
import de.cosmocode.commons.reflect.Reflection;
 import de.cosmocode.palava.ipc.IpcCall;
 import de.cosmocode.palava.ipc.IpcCallFilterChain;
 import de.cosmocode.palava.ipc.IpcCallFilterChainFactory;
 import de.cosmocode.palava.ipc.IpcCommand;
 import de.cosmocode.palava.ipc.IpcCommandExecutionException;
 import de.cosmocode.palava.ipc.IpcCommandExecutor;
 import de.cosmocode.palava.ipc.IpcCommandNotAvailableException;
 
 /**
  * Implements the {@link IpcCommandExecutor} with local vm class lookups.
  *
  * @author Tobias Sarnowski
  * @author Willi Schoenborn
  */
final class LocalExecutor implements IpcCommandExecutor {
 
     private static final Logger LOG = LoggerFactory.getLogger(LocalExecutor.class);
     
     private final Injector injector;
 
     private final IpcCallFilterChainFactory chainFactory;
 
     @Inject
     protected LocalExecutor(Injector injector, IpcCallFilterChainFactory chainFactory) {
         this.injector = Preconditions.checkNotNull(injector, "Injector");
         this.chainFactory = Preconditions.checkNotNull(chainFactory, "ChainFactory");
     }
 
     @Override
     public Map<String, Object> execute(final String name, final IpcCall call) throws IpcCommandExecutionException {
         Preconditions.checkNotNull(call, "Call");
 
         final Class<? extends IpcCommand> commandClass;
 
         try {
            commandClass = Reflection.forName(name).asSubclass(IpcCommand.class);
         } catch (ClassNotFoundException e) {
             LOG.error("Unable to load command class: {}", e);
             throw new IpcCommandNotAvailableException(name);
         } catch (ClassCastException e) {
             LOG.error("Unable to cast as command class: {}", e);
             // caller should not know the difference between "class not found" and "class is no command"
             throw new IpcCommandNotAvailableException(name);
         }
 
         final IpcCommand command = injector.getInstance(commandClass);
         final IpcCallFilterChain chain = chainFactory.create(ExecutingFilterChain.INSTANCE);
         return chain.filter(call, command);
     }
     
     /**
      * A {@link IpcCallFilterChain} implementation that executes the given command.
      *
      * @author Willi Schoenborn
      */
     private enum ExecutingFilterChain implements IpcCallFilterChain {
         
         INSTANCE;
         
         @Override
         public Map<String, Object> filter(IpcCall call, IpcCommand command) throws IpcCommandExecutionException {
             final Map<String, Object> result = Maps.newLinkedHashMap();
             LOG.debug("Executing {}", command);
             
             try {
                 command.execute(call, result);
             } catch (IpcCommandExecutionException e) {
                 LOG.debug("An expected exception was thrown while executing " + command, e);
                 throw e;
                 /* CHECKSTYLE:OFF */
             } catch (RuntimeException e) {
                 /* CHECKSTYLE:ON */
                 LOG.error("An unexpected exception was thrown while executing " + command, e);
                 throw e;
             }
             
             return result;
         }
         
     }
 
 }
