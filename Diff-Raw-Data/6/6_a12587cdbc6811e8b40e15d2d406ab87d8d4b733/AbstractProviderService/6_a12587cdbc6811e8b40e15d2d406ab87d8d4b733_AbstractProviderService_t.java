 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.apache.hadoop.hoya.providers;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.hoya.HoyaKeys;
 import org.apache.hadoop.hoya.api.ClusterDescription;
 import org.apache.hadoop.hoya.api.ClusterNode;
 import org.apache.hadoop.hoya.exceptions.HoyaException;
 import org.apache.hadoop.hoya.tools.HoyaUtils;
 import org.apache.hadoop.hoya.yarn.service.ForkedProcessService;
 import org.apache.hadoop.hoya.yarn.service.Parent;
 import org.apache.hadoop.hoya.yarn.service.SequenceService;
 import org.apache.hadoop.service.Service;
 import org.apache.hadoop.yarn.service.launcher.ExitCodeProvider;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 /**
  * The base class for provider services. It lets the implementations
  * add sequences of operations, and propagates service failures
  * upstream
  */
 public abstract class AbstractProviderService
                           extends SequenceService
                           implements
                             ProviderCore,
                             HoyaKeys,
                             ProviderService {
   private static final Logger log =
     LoggerFactory.getLogger(AbstractProviderService.class);
   
   public AbstractProviderService(String name) {
     super(name);
   }
 
   /**
    * Build a string command from a list of objects
    * @param args arguments
    * @return a space separated string of all the arguments' string values
    */
   protected String cmd(Object... args) {
     List<String> list = new ArrayList<String>(args.length);
     for (Object arg : args) {
       list.add(arg.toString());
     }
     return HoyaUtils.join(list, " ");
   }
 
   @Override
   public Configuration getConf() {
     return getConfig();
   }
 
   @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
   @Override // ExitCodeProvider
   public int getExitCode() {
     Throwable cause = getFailureCause();
     if (cause != null) {
       //failed for some reason
       if (cause instanceof ExitCodeProvider) {
         return ((ExitCodeProvider) cause).getExitCode();
       }
     }
     ForkedProcessService lastProc = latestProcess();
    if (lastProc == null) {
      return 0;
    } else {
      return lastProc.getExitCode();
    }
   }
 
   /**
    * Return the latest forked process service that ran
    * @return the forkes service
    */
   protected ForkedProcessService latestProcess() {
     Service current = getCurrentService();
     Service prev = getPreviousService();
 
     Service latest = current != null ? current : prev;
     if (latest instanceof ForkedProcessService) {
       return (ForkedProcessService) latest;
     } else {
       //its a composite object, so look inside it for a process
       if (latest instanceof Parent) {
         return getFPSFromParentService((Parent) latest);
       } else {
         //no match
         return null;
       }
     }
   }
 
 
   /**
    * Given a parent service, find the one that is a forked process
    * @param parent parent
    * @return the forked process service or null if there is none
    */
   protected ForkedProcessService getFPSFromParentService(Parent parent) {
     List<Service> services = parent.getServices();
     for (Service s : services) {
       if (s instanceof ForkedProcessService) {
         return (ForkedProcessService) s;
       }
     }
     return null;
   }
 
   /**
    * Build a status report 
    * @param masterNode node to fill in
    * @return true iff there was a process to build a status
    * report from
    */
   @Override
   public boolean buildStatusReport(ClusterNode masterNode) {
     ForkedProcessService masterProcess = latestProcess();
     if (masterProcess != null) {
       masterNode.command = masterProcess.getCommandLine();
       masterNode.state = masterProcess.isProcessStarted() ?
                          ClusterDescription.STATE_LIVE :
                          ClusterDescription.STATE_STOPPED;
 
       masterNode.diagnostics = "Exit code = " + masterProcess.getExitCode();
       //pull in recent lines of output
       List<String> output = masterProcess.getRecentOutput();
       masterNode.output = output.toArray(new String[output.size()]);
       return true;
     } else {
       masterNode.state = ClusterDescription.STATE_CREATED;
       masterNode.output = new String[] {"Master process not running"};
       return false;
     }
 
   }
 
   /**
    * if we are already running, start this service
    */
   protected void maybeStartCommandSequence() {
     if (isInState(STATE.STARTED)) {
       startNextService();
     }
   }
 
   /**
    * Create a new forked process service with the given
    * name, environment and command list -then add it as a child
    * for execution in the sequence.
    *
    * @param name command name
    * @param env environment
    * @param commands command line
    * @throws IOException
    * @throws HoyaException
    */
   protected ForkedProcessService queueCommand(String name,
                               Map<String, String> env,
                               List<String> commands) throws
                                                      IOException,
                                                      HoyaException {
     ForkedProcessService process = buildProcess(name, env, commands);
     //register the service for lifecycle management; when this service
     //is terminated, so is the master process
     addService(process);
     return process;
   }
 
   public ForkedProcessService buildProcess(String name,
                                            Map<String, String> env,
                                            List<String> commands) throws
                                                                   IOException,
                                                                   HoyaException {
     ForkedProcessService process;
     process = new ForkedProcessService(name);
     process.init(getConfig());
     process.build(env, commands);
     return process;
   }
 }
