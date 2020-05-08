 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  *  or more contributor license agreements.  See the NOTICE file
  *  distributed with this work for additional information
  *  regarding copyright ownership.  The ASF licenses this file
  *  to you under the Apache License, Version 2.0 (the
  *  "License"); you may not use this file except in compliance
  *  with the License.  You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 
 package org.apache.hadoop.hoya.yarn.appmaster;
 
 import com.google.protobuf.BlockingService;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.hdfs.DFSConfigKeys;
 import org.apache.hadoop.hoya.HoyaExitCodes;
 import org.apache.hadoop.hoya.HoyaKeys;
 import org.apache.hadoop.hoya.api.ClusterDescription;
 import org.apache.hadoop.hoya.api.HoyaClusterProtocol;
 import org.apache.hadoop.hoya.api.RoleKeys;
 import org.apache.hadoop.hoya.api.proto.HoyaClusterAPI;
 import org.apache.hadoop.hoya.api.proto.Messages;
 import org.apache.hadoop.hoya.exceptions.BadCommandArgumentsException;
 import org.apache.hadoop.hoya.exceptions.HoyaException;
 import org.apache.hadoop.hoya.exceptions.HoyaInternalStateException;
 import org.apache.hadoop.hoya.providers.HoyaProviderFactory;
 import org.apache.hadoop.hoya.providers.ProviderRole;
 import org.apache.hadoop.hoya.providers.ProviderService;
 import org.apache.hadoop.hoya.providers.hbase.HBaseConfigFileOptions;
 import org.apache.hadoop.hoya.tools.ConfigHelper;
 import org.apache.hadoop.hoya.tools.HoyaUtils;
 import org.apache.hadoop.hoya.yarn.HoyaActions;
 import org.apache.hadoop.hoya.yarn.appmaster.rpc.HoyaClusterProtocolPBImpl;
 import org.apache.hadoop.hoya.yarn.appmaster.rpc.RpcBinder;
 import org.apache.hadoop.hoya.yarn.appmaster.state.AppState;
 import org.apache.hadoop.hoya.yarn.appmaster.state.RoleInstance;
 import org.apache.hadoop.hoya.yarn.appmaster.state.RoleStatus;
 import org.apache.hadoop.hoya.yarn.service.EventCallback;
 import org.apache.hadoop.io.DataOutputBuffer;
 import org.apache.hadoop.ipc.ProtocolSignature;
 import org.apache.hadoop.ipc.Server;
 import org.apache.hadoop.net.NetUtils;
 import org.apache.hadoop.security.Credentials;
 import org.apache.hadoop.security.UserGroupInformation;
 import org.apache.hadoop.security.token.Token;
 import org.apache.hadoop.service.CompositeService;
 import org.apache.hadoop.service.Service;
 import org.apache.hadoop.service.ServiceStateChangeListener;
 import org.apache.hadoop.yarn.api.ApplicationConstants;
 import org.apache.hadoop.yarn.api.protocolrecords.RegisterApplicationMasterResponse;
 import org.apache.hadoop.yarn.api.records.ApplicationAccessType;
 import org.apache.hadoop.yarn.api.records.ApplicationAttemptId;
 import org.apache.hadoop.yarn.api.records.ApplicationId;
 import org.apache.hadoop.yarn.api.records.Container;
 import org.apache.hadoop.yarn.api.records.ContainerId;
 import org.apache.hadoop.yarn.api.records.ContainerLaunchContext;
 import org.apache.hadoop.yarn.api.records.ContainerState;
 import org.apache.hadoop.yarn.api.records.ContainerStatus;
 import org.apache.hadoop.yarn.api.records.FinalApplicationStatus;
 import org.apache.hadoop.yarn.api.records.NodeReport;
 import org.apache.hadoop.yarn.api.records.Resource;
 import org.apache.hadoop.yarn.client.api.AMRMClient;
 import org.apache.hadoop.yarn.client.api.async.AMRMClientAsync;
 import org.apache.hadoop.yarn.client.api.async.NMClientAsync;
 import org.apache.hadoop.yarn.client.api.async.impl.NMClientAsyncImpl;
 import org.apache.hadoop.yarn.conf.YarnConfiguration;
 import org.apache.hadoop.yarn.exceptions.YarnException;
 import org.apache.hadoop.yarn.exceptions.YarnRuntimeException;
 import org.apache.hadoop.yarn.ipc.YarnRPC;
 import org.apache.hadoop.yarn.security.AMRMTokenIdentifier;
 import org.apache.hadoop.yarn.service.launcher.RunService;
 import org.apache.hadoop.yarn.util.ConverterUtils;
 import org.apache.hadoop.yarn.util.Records;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.net.URI;
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.concurrent.locks.Condition;
 import java.util.concurrent.locks.ReentrantLock;
 
 /**
  * This is the AM, which directly implements the callbacks from the AM and NM
  */
 public class HoyaAppMaster extends CompositeService
   implements AMRMClientAsync.CallbackHandler,
              NMClientAsync.CallbackHandler,
              RunService,
              HoyaExitCodes,
              HoyaKeys,
              HoyaClusterProtocol,
              ServiceStateChangeListener,
              RoleKeys,
              EventCallback {
   protected static final Logger log =
     LoggerFactory.getLogger(HoyaAppMaster.class);
 
   /**
    * log for YARN events
    */
   protected static final Logger LOG_YARN =
     LoggerFactory.getLogger(
       "org.apache.hadoop.hoya.yarn.appmaster.HoyaAppMaster.yarn");
 
   /**
    * How long to expect launcher threads to shut down on AM termination:
    * {@value}
    */
   public static final int LAUNCHER_THREAD_SHUTDOWN_TIME = 10000;
 
   /**
    * time to wait from shutdown signal being rx'd to telling
    * the AM: {@value}
    */
   public static final int TERMINATION_SIGNAL_PROPAGATION_DELAY = 1000;
 
   public static final int HEARTBEAT_INTERVAL = 1000;
   public static final int NUM_RPC_HANDLERS = 5;
 
   /** YARN RPC to communicate with the Resource Manager or Node Manager */
   private YarnRPC rpc;
 
   /** Handle to communicate with the Resource Manager*/
   private AMRMClientAsync asyncRMClient;
 
   /** Handle to communicate with the Node Manager*/
   public NMClientAsync nmClientAsync;
   
   /**
    * token blob
    */
   private ByteBuffer allTokens;
   
   /** RPC server*/
   private Server server;
   /** Hostname of the container*/
   private String appMasterHostname = "";
   /* Port on which the app master listens for status updates from clients*/
   private int appMasterRpcPort = 0;
   /** Tracking url to which app master publishes info for clients to monitor*/
   private String appMasterTrackingUrl = "";
 
   /** Application Attempt Id ( combination of attemptId and fail count )*/
   private ApplicationAttemptId appAttemptID;
 
   /**
    * Security info client to AM key returned after registration
    */
   private ByteBuffer clientToAMKey;
 
   /**
    * App ACLs
    */
   protected Map<ApplicationAccessType, String> applicationACLs;
 
   /**
    * Ongoing state of the cluster: containers, nodes they
    * live on, etc.
    */
   private final AppState appState = new AppState();
 
 
   /**
    * Map of launched threads.
    * These are retained so that at shutdown time the AM can signal
    * all threads to stop.
    * 
    * However, we don't want to run out of memory even if many containers
    * get launched over time, so the AM tries to purge this
    * of the latest launched thread when the RoleLauncher signals
    * the AM that it has finished
    */
   private final Map<RoleLauncher, Thread> launchThreads =
     new HashMap<RoleLauncher, Thread>();
 
   /**
    * Thread group for the launchers; gives them all a useful name
    * in stack dumps
    */
   private final ThreadGroup launcherThreadGroup = new ThreadGroup("launcher");
 
   /**
    * model the state using locks and conditions
    */
   private final ReentrantLock AMExecutionStateLock = new ReentrantLock();
   private final Condition isAMCompleted = AMExecutionStateLock.newCondition();
 
   /**
    * Flag set if the AM is to be shutdown
    */
   private final AtomicBoolean amCompletionFlag = new AtomicBoolean(false);
   private volatile boolean success = true;
 
   /**
    * Exit code set when the spawned process exits
    */
   private volatile int spawnedProcessExitCode;
   private volatile int mappedProcessExitCode;
   /**
    * Flag to set if the process exit code was set before shutdown started
    */
   private boolean spawnedProcessExitedBeforeShutdownTriggered;
 
 
   /** Arguments passed in : raw*/
   private HoyaMasterServiceArgs serviceArgs;
 
   /** Arguments passed in : parsed*/
   private String[] argv;
 
   /**
    * ID of the AM container
    */
   private ContainerId appMasterContainerID;
 
   /**
    * Provider of this cluster
    */
   private ProviderService provider;
 
   /**
    * Record of the max no. of cores allowed in this cluster
    */
   private int containerMaxCores;
 
 
   /**
    * limit container memory
    */
   private int containerMaxMemory;
   private String amCompletionReason;
 
   /**
    * Service Constructor
    */
   public HoyaAppMaster() {
     super("HoyaMasterService");
   }
 
 /* =================================================================== */
 /* Object methods */
 /* =================================================================== */
 
   @Override
   public String toString() {
     return super.toString();
   }
 
   /* =================================================================== */
 /* service lifecycle methods */
 /* =================================================================== */
 
   @Override //AbstractService
   public synchronized void serviceInit(Configuration conf) throws Exception {
     //sort out the location of the AM
     serviceArgs.applyDefinitions(conf);
     serviceArgs.applyFileSystemURL(conf);
 
     String rmAddress = serviceArgs.rmAddress;
     if (rmAddress != null) {
       log.debug("Setting rm address from the command line: {}", rmAddress);
       HoyaUtils.setRmSchedulerAddress(conf, rmAddress);
     }
     serviceArgs.applyDefinitions(conf);
     serviceArgs.applyFileSystemURL(conf);
     //init security with our conf
     if (serviceArgs.secure) {
       log.info("Secure mode with kerberos realm {}",
                HoyaUtils.getKerberosRealm());
       UserGroupInformation.setConfiguration(conf);
       UserGroupInformation ugi = UserGroupInformation.getCurrentUser();
       log.debug("Authenticating as " + ugi.toString());
       log.debug("Login user is {}", UserGroupInformation.getLoginUser());
       HoyaUtils.verifyPrincipalSet(conf,
                                    DFSConfigKeys.DFS_NAMENODE_USER_NAME_KEY);
     }
 
     //look at settings of Hadoop Auth, to pick up a problem seen once
     checkAndWarnForAuthTokenProblems();
 
     super.serviceInit(conf);
   }
   
 /* =================================================================== */
 /* RunService methods called from ServiceLauncher */
 /* =================================================================== */
 
   /**
    * pick up the args from the service launcher
    * @param config
    * @param args argument list
    */
   @Override // RunService
   public Configuration bindArgs(Configuration config, String... args) throws
                                                                       Exception {
     this.argv = args;
     serviceArgs = new HoyaMasterServiceArgs(argv);
     serviceArgs.parse();
     serviceArgs.postProcess();
     return HoyaUtils.patchConfiguration(config);
   }
 
 
   /**
    * this is called by service launcher; when it returns the application finishes
    * @return the exit code to return by the app
    * @throws Throwable
    */
   @Override
   public int runService() throws Throwable {
 
     //choose the action
     String action = serviceArgs.action;
     List<String> actionArgs = serviceArgs.actionArgs;
     int exitCode = EXIT_SUCCESS;
     if (action.equals(HoyaActions.ACTION_HELP)) {
       log.info(getName() + serviceArgs.usage());
       exitCode = HoyaExitCodes.EXIT_USAGE;
     } else if (action.equals(HoyaActions.ACTION_CREATE)) {
       exitCode = createAndRunCluster(actionArgs.get(0));
     } else {
       throw new HoyaException("Unimplemented: " + action);
     }
     log.info("Exiting HoyaAM; final exit code = {}", exitCode);
     return exitCode;
   }
 
 /* =================================================================== */
 
   /**
    * Create and run the cluster.
    * @return exit code
    * @throws Throwable on a failure
    */
   private int createAndRunCluster(String clustername) throws Throwable {
 
     //load the cluster description from the cd argument
     String hoyaClusterDir = serviceArgs.hoyaClusterURI;
     URI hoyaClusterURI = new URI(hoyaClusterDir);
     Path clusterDirPath = new Path(hoyaClusterURI);
     Path clusterSpecPath =
       new Path(clusterDirPath, HoyaKeys.CLUSTER_SPECIFICATION_FILE);
     FileSystem fs = getClusterFS();
     ClusterDescription.verifyClusterSpecExists(clustername, fs,
                                                clusterSpecPath);
 
     ClusterDescription clusterSpec = ClusterDescription.load(fs, clusterSpecPath);
 
     File confDir = getLocalConfDir();
     if (!confDir.exists() || !confDir.isDirectory()) {
       throw new BadCommandArgumentsException(
         "Configuration directory %s doesn't exist", confDir);
     }
 
     //get our provider
     String providerType = clusterSpec.type;
     log.info("Cluster provider type is {}", providerType);
     HoyaProviderFactory factory =
       HoyaProviderFactory.createHoyaProviderFactory(
         providerType);
     provider = factory.createServerProvider();
     provider.init(getConfig());
     provider.start();
     addService(provider);
     //verify that the cluster specification is now valid
     provider.validateClusterSpec(clusterSpec);
 
 
     YarnConfiguration conf = new YarnConfiguration(getConfig());
     InetSocketAddress address = HoyaUtils.getRmSchedulerAddress(conf);
     log.info("RM is at {}", address);
     rpc = YarnRPC.create(conf);
 
     appMasterContainerID = ConverterUtils.toContainerId(
       HoyaUtils.mandatoryEnvVariable(
         ApplicationConstants.Environment.CONTAINER_ID.name()));
     appAttemptID = appMasterContainerID.getApplicationAttemptId();
 
     ApplicationId appid = appAttemptID.getApplicationId();
     log.info("Hoya AM for ID {}", appid.getId());
 
<<<<<<< HEAD
     Credentials credentials =
       UserGroupInformation.getCurrentUser().getCredentials();
     DataOutputBuffer dob = new DataOutputBuffer();
     credentials.writeTokenStorageToStream(dob);
     // Now remove the AM->RM token so that containers cannot access it.
     Iterator<Token<?>> iter = credentials.getAllTokens().iterator();
     while (iter.hasNext()) {
       Token<?> token = iter.next();
       if (token.getKind().equals(AMRMTokenIdentifier.KIND_NAME)) {
         iter.remove();
       }
     }
     allTokens = ByteBuffer.wrap(dob.getData(), 0, dob.getLength());
=======
    int heartbeatInterval = HEARTBEAT_INTERVAL;
 
>>>>>>> hortonworks/develop
 
     //add the RM client -this brings the callbacks in
     asyncRMClient = AMRMClientAsync.createAMRMClientAsync(HEARTBEAT_INTERVAL,
                                                           this);
     addService(asyncRMClient);
     //now bring it up
     asyncRMClient.init(conf);
     asyncRMClient.start();
 
 
     //nmclient relays callbacks back to this class
     nmClientAsync = new NMClientAsyncImpl("hoya", this);
     addService(nmClientAsync);
     nmClientAsync.init(conf);
     nmClientAsync.start();
 
     //bring up the Hoya RPC service
     startHoyaRPCServer();
 
     String hostname = NetUtils.getConnectAddress(server).getHostName();
     appMasterHostname = hostname;
     appMasterRpcPort = server.getPort();
     appMasterTrackingUrl = null;
     log.info("HoyaAM Server is listening at {}:{}", appMasterHostname,
              appMasterRpcPort);
 
     //build the role map
     List<ProviderRole> providerRoles = provider.getRoles();
 
 
     // work out a port for the AM
     int infoport = clusterSpec.getRoleOptInt(ROLE_MASTER,
                                                   RoleKeys.APP_INFOPORT,
                                                   0);
     if (0 == infoport) {
       infoport =
         HoyaUtils.findFreePort(provider.getDefaultMasterInfoPort(), 128);
       //need to get this to the app
 
       clusterSpec.setRoleOpt(ROLE_MASTER,
                                   RoleKeys.APP_INFOPORT,
                                   infoport);
     }
     appMasterTrackingUrl =
       "http://" + appMasterHostname + ":" + infoport;
 
 
     // Register self with ResourceManager
     // This will start heartbeating to the RM
     address = HoyaUtils.getRmSchedulerAddress(asyncRMClient.getConfig());
     log.info("Connecting to RM at {},address tracking URL={}",
              appMasterRpcPort, appMasterTrackingUrl);
     RegisterApplicationMasterResponse response = asyncRMClient
       .registerApplicationMaster(appMasterHostname,
                                  appMasterRpcPort,
                                  appMasterTrackingUrl);
     Resource maxResources =
       response.getMaximumResourceCapability();
     containerMaxMemory = maxResources.getMemory();
     containerMaxCores = maxResources.getVirtualCores();
     if (UserGroupInformation.isSecurityEnabled()) {
       //TODO make use of this
       clientToAMKey = response.getClientToAMTokenMasterKey();
       applicationACLs = response.getApplicationACLs();
     }
 
 
 
 
     //before bothering to start the containers, bring up the
     //master.
     //This ensures that if the master doesn't come up, less
     //cluster resources get wasted
 

     //now validate the dir by loading in a hadoop-site.xml file from it
     String siteXMLFilename = provider.getSiteXMLFilename();
     File siteXML = new File(confDir, siteXMLFilename);
     if (!siteXML.exists()) {
       throw new BadCommandArgumentsException(
         "Configuration directory %s doesn't contain %s - listing is %s",
         confDir, siteXMLFilename, HoyaUtils.listDir(confDir));
     }
 
     //now read it in
     Configuration siteConf = ConfigHelper.loadConfFromFile(siteXML);
     //update the values
     log.debug(" Contents of {}", siteXML);
 
     /*
     clusterSpec.zkHosts = siteConf.get(HBaseConfigFileOptions.KEY_ZOOKEEPER_QUORUM);
     clusterSpec.zkPort =
       siteConf.getInt(HBaseConfigFileOptions.KEY_ZOOKEEPER_PORT, 0);
     clusterSpec.zkPath = siteConf.get(HBaseConfigFileOptions.KEY_ZNODE_PARENT);
 */
 
     if (clusterSpec.zkPort == 0) {
       throw new BadCommandArgumentsException(
         "ZK port property not provided at %s in configuration file %s",
         HBaseConfigFileOptions.KEY_ZOOKEEPER_PORT,
         siteXML);
     }
 
     //build the instance
     appState.buildInstance(clusterSpec, siteConf, providerRoles);
 
     appState.buildMasterNode(appMasterContainerID);
 
 
     boolean noMaster = clusterSpec.getDesiredInstanceCount(ROLE_MASTER, 1) <= 0;
     if (noMaster) {
       log.info("skipping master launch");
       eventCallbackEvent();
     } else {
       appState.noteMasterNodeLaunched();
       //launch the provider; this is expected to trigger a callback that
       //brings up the service
       launchProviderService(clusterSpec, confDir);
     }
 
     try {
       //now block waiting to be told to exit the process
       waitForAMCompletionSignal();
       //shutdown time
     } finally {
       finish();
     }
 
     return buildExitCode();
   }
 
   private void checkAndWarnForAuthTokenProblems() {
     String fileLocation =
       System.getenv(UserGroupInformation.HADOOP_TOKEN_FILE_LOCATION);
     if (fileLocation != null) {
       File tokenFile = new File(fileLocation);
       if (!tokenFile.exists()) {
         log.warn("Token file {} specified in {} not found", tokenFile,
                  UserGroupInformation.HADOOP_TOKEN_FILE_LOCATION);
       }
     }
   }
 
   /**
    * Build a service exit code
    * @return
    */
   private int buildExitCode() {
     if (spawnedProcessExitedBeforeShutdownTriggered) {
       return mappedProcessExitCode;
     }
     return success ? EXIT_SUCCESS
                    : EXIT_TASK_LAUNCH_FAILURE;
   }
 
   /**
    * Build the configuration directory passed in or of the target FS
    * @return the file
    */
   public File getLocalConfDir() {
     File confdir =
       new File(HoyaKeys.PROPAGATED_CONF_DIR_NAME).getAbsoluteFile();
     return confdir;
   }
 
   public String getDFSConfDir() {
     return getClusterSpec().generatedConfigurationPath;
   }
 
   /**
    * Get the filesystem of this cluster
    * @return the FS of the config
    */
   public FileSystem getClusterFS() throws IOException {
     return FileSystem.get(getConfig());
   }
 
   /**
    * Block until it is signalled that the AM is done
    */
   private void waitForAMCompletionSignal() {
     AMExecutionStateLock.lock();
     try {
       if (!amCompletionFlag.get()) {
         log.debug("blocking until signalled to terminate");
         isAMCompleted.awaitUninterruptibly();
       }
     } finally {
       AMExecutionStateLock.unlock();
     }
     //add a sleep here for about a second. Why? it
     //stops RPC calls breaking so dramatically when the cluster
     //is torn down mid-RPC
     try {
       Thread.sleep(TERMINATION_SIGNAL_PROPAGATION_DELAY);
     } catch (InterruptedException ignored) {
       //ignored
     }
   }
 
   /**
    * Declare that the AM is complete
    */
   public void signalAMComplete(String reason) {
     amCompletionReason = reason;
     log.info("Triggering shutdown of the AM: {}", amCompletionReason);
     AMExecutionStateLock.lock();
     try {
       amCompletionFlag.set(true);
       isAMCompleted.signal();
     } finally {
       AMExecutionStateLock.unlock();
     }
   }
 
   /**
    * shut down the cluster 
    */
   private synchronized void finish() {
     FinalApplicationStatus appStatus;
     String appMessage = "Completed";
     appStatus = FinalApplicationStatus.SUCCEEDED;
     //stop the daemon & grab its exit code
     Integer exitCode = null;
     if (spawnedProcessExitedBeforeShutdownTriggered) {
       exitCode = mappedProcessExitCode;
       success = false;
       appStatus = FinalApplicationStatus.FAILED;
       appMessage =
         String.format("Forked process failed, mapped exit code=%s raw=%s",
                       exitCode,
                       spawnedProcessExitCode);
 
     } else {
       //stopped the forked process but don't worry about its exit code
       exitCode = stopForkedProcess();
       log.debug("Stopping forked process: exit code={}", exitCode);
     }
     joinAllLaunchedThreads();
 
 
     log.info("Releasing all containers");
     //now release all containers
     releaseAllContainers();
 
     // When the application completes, it should send a finish application
     // signal to the RM
     log.info("Application completed. Signalling finish to RM");
 
     ;
 
     String exitCodeString = exitCode != null ? exitCode.toString() : "n/a";
     //if there were failed containers and the app isn't already down as failing, it is now
     int failedContainerCount = appState.getFailedCountainerCount();
     if (failedContainerCount != 0 &&
         appStatus == FinalApplicationStatus.SUCCEEDED) {
       appStatus = FinalApplicationStatus.FAILED;
       appMessage =
         "Completed with " + failedContainerCount + " failed containers: "
         + " Local daemon exit code =  " +
         exitCodeString + " - " + getContainerDiagnosticInfo();
       success = false;
     }
     try {
       log.info("Unregistering AM status={} message={}", appStatus, appMessage);
       asyncRMClient.unregisterApplicationMaster(appStatus, appMessage, null);
     } catch (YarnException e) {
       log.info("Failed to unregister application: " + e, e);
     } catch (IOException e) {
       log.info("Failed to unregister application: " + e, e);
     }
     if (server != null) {
       server.stop();
     }
   }
 
   /**
    * Get diagnostics info about containers
    */
   private String getContainerDiagnosticInfo() {
     StringBuilder builder = new StringBuilder();
     for (RoleStatus roleStatus : getRoleStatusMap().values()) {
       builder.append(roleStatus).append('\n');
     }
     return builder.toString();
   }
 
   public Object getProxy(Class protocol, InetSocketAddress addr) {
     return rpc.getProxy(protocol, addr, getConfig());
   }
 
   /**
    * Register self as a server
    * @return the new server
    */
   private Server startHoyaRPCServer() throws IOException {
     HoyaClusterProtocolPBImpl protobufRelay = new HoyaClusterProtocolPBImpl(this);
     BlockingService blockingService = HoyaClusterAPI.HoyaClusterProtocolPB
                                                     .newReflectiveBlockingService(
                                                       protobufRelay);
 
     server = RpcBinder.createProtobufServer(
       new InetSocketAddress("0.0.0.0", 0),
       getConfig(),
       null,
       NUM_RPC_HANDLERS,
       blockingService,
       null);
     server.start();
 
 
 
     return server;
   }
 
   /**
    * Setup the request that will be sent to the RM for the container ask.
    *
    * much of the container ask will be built up in AppState
    * @param role @return the setup ResourceRequest to be sent to RM
    */
   private AMRMClient.ContainerRequest buildContainerRequest(RoleStatus role) {
 
     // Set up resource type requirements
     Resource capability = buildResourceRequirementsForRole(role);
     AMRMClient.ContainerRequest request =
       appState.createContainerRequest(role, capability);
 
     return request;
   }
 
 
   /**
    * Create the resource requirements for an instance of this role 
    * -done by looking up the cluster spec
    * @param role
    * @return
    */
   private Resource buildResourceRequirementsForRole(RoleStatus role) {
     Resource capability;
     capability = Records.newRecord(Resource.class);
     // Set up resource requirements from role valuesx
     String name = role.getName();
     capability.setVirtualCores(getClusterSpec().getRoleOptInt(name,
                                                               YARN_CORES,
                                                               DEF_YARN_CORES));
     capability.setMemory(getClusterSpec().getRoleOptInt(name,
                                                         YARN_MEMORY,
                                                         DEF_YARN_MEMORY));
     return capability;
   }
 
 
   private void launchThread(RoleLauncher launcher, String name) {
     Thread launchThread = new Thread(launcherThreadGroup,
                                      launcher,
                                      name);
 
     // launch and start the container on a separate thread to keep
     // the main thread unblocked
     // as all containers may not be allocated at one go.
     synchronized (launchThreads) {
       launchThreads.put(launcher, launchThread);
     }
     launchThread.start();
   }
 
   /**
    * Method called by a launcher thread when it has completed; 
    * this removes the launcher of the map of active
    * launching threads.
    * @param launcher
    */
   public void launchedThreadCompleted(RoleLauncher launcher) {
     synchronized (launchThreads) {
       launchThreads.remove(launcher);
     }
   }
 
   /**
    Join all launched threads
    needed for when we time out
    and we need to release containers
    */
   private void joinAllLaunchedThreads() {
 
 
     //first: take a snapshot of the thread list
     List<Thread> liveThreads;
     synchronized (launchThreads) {
       liveThreads = new ArrayList<Thread>(launchThreads.values());
     }
     int size = liveThreads.size();
     if (size > 0) {
       log.info("Waiting for the completion of {} threads", size);
       for (Thread launchThread : liveThreads) {
         try {
           launchThread.join(LAUNCHER_THREAD_SHUTDOWN_TIME);
         } catch (InterruptedException e) {
           log.info("Exception thrown in thread join: " + e, e);
         }
       }
     }
   }
 
 
   /**
    * Look up a role from its key -or fail 
    *
    * @param c container in a role
    * @return the status
    * @throws YarnRuntimeException on no match
    */
   public RoleStatus lookupRoleStatus(Container c) {
     return appState.lookupRoleStatus(c);
   }
   
 /* =================================================================== */
 /* AMRMClientAsync callbacks */
 /* =================================================================== */
 
   /**
    * Callback event when a container is allocated
    * @param allocatedContainers list of containers that are now ready to be
    * given work
    */
   @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
   @Override //AMRMClientAsync
   public void onContainersAllocated(List<Container> allocatedContainers) {
     LOG_YARN.info("onContainersAllocated({})", allocatedContainers.size());
     List<Container> surplus = new ArrayList<Container>();
     for (Container container : allocatedContainers) {
       String containerHostInfo = container.getNodeId().getHost()
                                  + ":" +
                                  container.getNodeId().getPort();
       int allocated;
       int desired;
       //get the role
       RoleStatus role;
       role = lookupRoleStatus(container);
       synchronized (role) {
         //sync on all container details. Even though these are atomic,
         //we don't really want multiple updates happening simultaneously
         log.info(getContainerDiagnosticInfo());
         //dec requested count
         role.decRequested();
         //inc allocated count
         allocated = role.incActual();
 
         //look for (race condition) where we get more back than we asked
         desired = role.getDesired();
       }
       if (allocated > desired) {
         log.info("Discarding surplus container {} on {}", container.getId(),
                  containerHostInfo);
         surplus.add(container);
       } else {
 
         log.info("Launching shell command on a new container.," +
                  " containerId={}," +
                  " containerNode={}:{}," +
                  " containerNodeURI={}," +
                  " containerResource={}",
                  container.getId(),
                  container.getNodeId().getHost(),
                  container.getNodeId().getPort(),
                  container.getNodeHttpAddress(),
                  container.getResource());
 
         String roleName = role.getName();
         RoleLauncher launcher =
           new RoleLauncher(this,
                            container,
                            role.getProviderRole(),
                            provider,
                            getClusterSpec(),
                            getClusterSpec().getOrAddRole(
                              roleName));
         launchThread(launcher, "container-" +
                                containerHostInfo);
       }
     }
     //now discard those surplus containers
     for (Container container : surplus) {
       ContainerId id = container.getId();
       log.info("Releasing surplus container {} on {}:{}",
                id.getApplicationAttemptId(),
                container.getNodeId().getHost(),
                container.getNodeId().getPort());
       RoleStatus role = lookupRoleStatus(container);
       synchronized (role) {
         role.incReleasing();
       }
       asyncRMClient.releaseAssignedContainer(id);
     }
     log.info("Diagnostics: " + getContainerDiagnosticInfo());
   }
 
   @Override //AMRMClientAsync
   public synchronized void onContainersCompleted(List<ContainerStatus> completedContainers) {
     LOG_YARN.info("onContainersCompleted([{}]", completedContainers.size());
     for (ContainerStatus status : completedContainers) {
       ContainerId containerId = status.getContainerId();
       LOG_YARN.info("Container Completion for" +
                     " containerID={}," +
                     " state={}," +
                     " exitStatus={}," +
                     " diagnostics={}",
                     containerId, status.getState(),
                     status.getExitStatus(),
                     status.getDiagnostics());
 
       // non complete containers should not be here
       assert (status.getState() == ContainerState.COMPLETE);
       appState.onCompletedNode(status);
     }
 
     // ask for more containers if any failed
     // In the case of Hoya, we don't expect containers to complete since
     // Hoya is a long running application. Keep track of how many containers
     // are completing. If too many complete, abort the application
     // TODO: this needs to be better thought about (and maybe something to
     // better handle in Yarn for long running apps)
 
     try {
       reviewRequestAndReleaseNodes();
     } catch (HoyaInternalStateException e) {
       log.warn("Exception while flexing nodes", e);
     }
 
   }
 
   /**
    * Implementation of cluster flexing.
    * This is synchronized so that it doesn't get confused by other requests coming
    * in.
    * It should be the only way that anything -even the AM itself on startup-
    * asks for nodes. 
    * @param workers #of workers to add
    * @param masters #of masters to request (if supported)
    * @return true if the number of workers changed
    * @throws IOException
    */
   private boolean flexCluster(ClusterDescription updated) throws
                                                                IOException,
                                                                HoyaInternalStateException {
 
     
     //validation
     try {
       provider.validateClusterSpec(updated);
     } catch (HoyaException e) {
       throw new IOException("Invalid cluster specification " + e, e);
     }
     appState.updateClusterSpec(updated);
 
     // ask for more containers if needed
     return reviewRequestAndReleaseNodes();
   }
 
   /**
    * Look at where the current node state is -and whether it should be changed
    */
   private synchronized boolean reviewRequestAndReleaseNodes() throws
                                                               HoyaInternalStateException {
     log.debug("in reviewRequestAndReleaseNodes()");
     if (amCompletionFlag.get()) {
       log.info("Ignoring node review operation: shutdown in progress");
       return false;
     }
 
     boolean updatedNodeCount = false;
 
     for (RoleStatus roleStatus : getRoleStatusMap().values()) {
       if (!roleStatus.getExcludeFromFlexing()) {
         updatedNodeCount |= reviewOneRole(roleStatus);
       }
     }
     return updatedNodeCount;
   }
 
   
   private boolean reviewOneRole(RoleStatus role) throws
                                                  HoyaInternalStateException {
     int delta;
     String details;
     int expected;
     synchronized (role) {
       delta = role.getDelta();
       details = role.toString();
       expected = role.getDesired();
     }
 
     log.info(details);
     boolean updated = false;
     if (delta > 0) {
       log.info("Asking for {} more worker(s) for a total of {} ",
                delta, expected);
       //more workers needed than we have -ask for more
       for (int i = 0; i < delta; i++) {
         AMRMClient.ContainerRequest containerAsk =
           buildContainerRequest(role);
         log.info("Container ask is {}", containerAsk);
         if (containerAsk.getCapability().getMemory() > this.containerMaxMemory) {
           log.warn("Memory requested: " + containerAsk.getCapability().getMemory() + " > " +
               this.containerMaxMemory);
         } else {
           asyncRMClient.addContainerRequest(containerAsk);
           updated = true;
         }
       }
     } else if (delta < 0) {
 
       //special case: there are no more containers
 /*
       if (total == 0 && !noMaster) {
         //just exit the entire application here, rather than a node at a time.
         signalAMComplete("#of workers is set to zero: exiting");
         return;
       }
 */
 
       log.info("Asking for {} fewer worker(s) for a total of {}", -delta,
                expected);
       //reduce the number expected (i.e. subtract the delta)
 
       //then pick some containers to kill
       int excess = -delta;
       List<RoleInstance> targets = appState.cloneActiveContainerList();
       for (RoleInstance instance : targets) {
         if (excess > 0) {
           Container possible = instance.container;
           if (!instance.released) {
             ContainerId id = possible.getId();
             log.info("Requesting release of container {}", id);
             appState.containerReleaseSubmitted(id);
             asyncRMClient.releaseAssignedContainer(id);
             excess--;
           }
         }
       }
       //here everything should be freed up, though there may be an excess due 
       //to race conditions with requests coming in
       if (excess > 0) {
         log.warn(
           "After releasing all worker nodes that could be free, there was an excess of {} nodes",
           excess);
       }
       updated = true;
 
     }
     return updated;
   }
 
   /**
    * Shutdown operation: release all containers
    */
   void releaseAllContainers() {
     Collection<RoleInstance> targets = appState.cloneActiveContainerList();
     for (RoleInstance instance : targets) {
       Container possible = instance.container;
       ContainerId id = possible.getId();
       if (!instance.released) {
         try {
           appState.containerReleaseSubmitted(id);
         } catch (HoyaInternalStateException e) {
           log.warn("when releasing container {} :", possible, e);
         }
         asyncRMClient.releaseAssignedContainer(id);
       }
     }
   }
 
   /**
    * RM wants to shut down the AM
    */
   @Override //AMRMClientAsync
   public void onShutdownRequest() {
     LOG_YARN.info("Shutdown Request received");
     signalAMComplete("Shutdown requested from RM");
   }
 
   /**
    * Monitored nodes have been changed
    * @param updatedNodes list of updated notes
    */
   @Override //AMRMClientAsync
   public void onNodesUpdated(List<NodeReport> updatedNodes) {
     LOG_YARN.info("Nodes updated");
   }
 
   /**
    * heartbeat operation; return the ratio of requested
    * to actual
    * @return progress
    */
   @Override //AMRMClientAsync
   public float getProgress() {
     return appState.getApplicationProgressPercentage();
   }
 
   @Override //AMRMClientAsync
   public void onError(Throwable e) {
     //callback says it's time to finish
     LOG_YARN.error("AMRMClientAsync.onError() received " + e, e);
     signalAMComplete("AMRMClientAsync.onError() received " + e);
   }
   
 /* =================================================================== */
 /* HoyaClusterProtocol */
 /* =================================================================== */
 
   @Override   //HoyaClusterProtocol
   public ProtocolSignature getProtocolSignature(String protocol,
                                                 long clientVersion,
                                                 int clientMethodsHash) throws
                                                                        IOException {
     return ProtocolSignature.getProtocolSignature(
       this, protocol, clientVersion, clientMethodsHash);
   }
 
 
 
   @Override   //HoyaClusterProtocol
   public long getProtocolVersion(String protocol, long clientVersion) throws
                                                                       IOException {
     return HoyaClusterProtocol.versionID;
   }
 
   
 /* =================================================================== */
 /* HoyaClusterProtocol */
 /* =================================================================== */
 
   @Override //HoyaClusterProtocol
   public Messages.StopClusterResponseProto stopCluster(Messages.StopClusterRequestProto request) throws
                                                                                                  IOException,
                                                                                                  YarnException {
     HoyaUtils.getCurrentUser();
     String message = request.getMessage();
     log.info("HoyaAppMasterApi.stopCluster: {}",message);
     signalAMComplete("stopCluster: " + message);
     return Messages.StopClusterResponseProto.getDefaultInstance();
   }
 
   @Override //HoyaClusterProtocol
   public Messages.FlexClusterResponseProto flexCluster(Messages.FlexClusterRequestProto request) throws
                                                                                                  IOException,
                                                                                                  YarnException {
     HoyaUtils.getCurrentUser();
 
     ClusterDescription updated =
       ClusterDescription.fromJson(request.getClusterSpec());
     //verify that the cluster specification is now valid
     provider.validateClusterSpec(updated);
 
     boolean flexed = flexCluster(updated);
     return Messages.FlexClusterResponseProto.newBuilder().setResponse(flexed).build();
   }
 
   @Override //HoyaClusterProtocol
   public Messages.GetJSONClusterStatusResponseProto getJSONClusterStatus(
     Messages.GetJSONClusterStatusRequestProto request) throws
                                                        IOException,
                                                        YarnException {
     HoyaUtils.getCurrentUser();
     String result;
     //quick update
     //query and json-ify
     synchronized (this) {
       updateClusterStatus();
       result = getClusterStatus().toJsonString();
     }
     String stat = result;
     return Messages.GetJSONClusterStatusResponseProto.newBuilder()
       .setClusterSpec(stat)
       .build();
   }
 
   @Override //HoyaClusterProtocol
   public Messages.ListNodeUUIDsByRoleResponseProto listNodeUUIDsByRole(Messages.ListNodeUUIDsByRoleRequestProto request) throws
                                                                                                                          IOException,
                                                                                                                          YarnException {
     HoyaUtils.getCurrentUser();
     String role = request.getRole();
     Messages.ListNodeUUIDsByRoleResponseProto.Builder builder =
       Messages.ListNodeUUIDsByRoleResponseProto.newBuilder();
     List<RoleInstance> nodes = appState.enumLiveNodesInRole(role);
     for (RoleInstance node : nodes) {
       builder.addUuid(node.uuid);
     }
     return builder.build();
   }
 
   @Override //HoyaClusterProtocol
   public Messages.GetNodeResponseProto getNode(Messages.GetNodeRequestProto request) throws
                                                                                      IOException,
                                                                                      YarnException {
     HoyaUtils.getCurrentUser();
     RoleInstance instance = appState.getLiveInstanceByUUID(request.getUuid());
     return Messages.GetNodeResponseProto.newBuilder()
                    .setClusterNode(instance.toProtobuf())
                    .build();
   }
 
   @Override //HoyaClusterProtocol
   public Messages.GetClusterNodesResponseProto getClusterNodes(Messages.GetClusterNodesRequestProto request) throws
                                                                                                              IOException,
                                                                                                              YarnException {
     HoyaUtils.getCurrentUser();
     List<RoleInstance>
       clusterNodes = appState.getLiveContainerInfosByUUID(request.getUuidList());
 
     Messages.GetClusterNodesResponseProto.Builder builder =
       Messages.GetClusterNodesResponseProto.newBuilder();
     for (RoleInstance node : clusterNodes) {
       builder.addClusterNode(node.toProtobuf());
     }
     //at this point: a possibly empty list of nodes
     return builder.build();
   }
 
   
 /* =================================================================== */
 /* END */
 /* =================================================================== */
 
   /**
    * Update the cluster description with anything interesting
    */
   private void updateClusterStatus() {
 
     long t = System.currentTimeMillis();
     RoleInstance masterNode = appState.getMasterNode();
     if (masterNode != null) {
       provider.buildStatusReport(masterNode.toClusterNodeFormat());
     }
     appState.refreshClusterStatus();
   }
 
   /**
    * Launch the provider service
    *
    * @param cd
    * @param confDir
    * @throws IOException
    * @throws HoyaException
    */
   protected synchronized void launchProviderService(ClusterDescription cd,
                                                     File confDir)
     throws IOException, HoyaException {
     Map<String, String> env = new HashMap<String, String>();
     provider.exec(cd, confDir, env, this);
 
     provider.registerServiceListener(this);
     provider.start();
   }
 
   /* =================================================================== */
   /* EventCallback  from the child */
   /* =================================================================== */
 
   @Override // EventCallback
   public void eventCallbackEvent() {
     //signalled that the child process is up.
     appState.noteMasterNodeLive();
     //now ask for the cluster nodes
     try {
       flexCluster(getClusterSpec());
     } catch (Exception e) {
       //this happens in a separate thread, so the ability to act is limited
       log.error("Failed to flex cluster nodes", e);
       //declare a failure
       finish();
     }
   }
 
   /* =================================================================== */
   /* ServiceStateChangeListener */
   /* =================================================================== */
 
   /**
    * Received on listening service termination.
    * @param service the service that has changed.
    */
   @Override //ServiceStateChangeListener
   public void stateChanged(Service service) {
     if (service == provider) {
       //its the current master process in play
       int exitCode = provider.getExitCode();
       spawnedProcessExitCode = exitCode;
       mappedProcessExitCode =
         AMUtils.mapProcessExitCodeToYarnExitCode(exitCode);
       if (!amCompletionFlag.get()) {
         //this wasn't expected: the process finished early
         spawnedProcessExitedBeforeShutdownTriggered = true;
         log.info(
           "Process has exited with exit code {} mapped to {} -triggering termination",
           exitCode,
           mappedProcessExitCode);
 
         //tell the AM the cluster is complete 
         signalAMComplete(
           "Spawned master exited with raw " + exitCode + " mapped to " +
           mappedProcessExitCode);
       } else {
         //we don't care
         log.info(
           "Process has exited with exit code {} mapped to {} -ignoring as app has finished",
           exitCode,
           mappedProcessExitCode);
       }
     }
   }
 
   /**
    * stop forked process if it the running process var is not null
    * @return the process exit code
    */
   protected synchronized Integer stopForkedProcess() {
     provider.stop();
     return provider.getExitCode();
   }
 
   /**
    * Package scoped operation to add a node to the list of starting
    * nodes then trigger the NM start operation with the given
    * launch context
    * @param container container
    * @param ctx context
    * @param instance node details
    */
   void startContainer(Container container,
                              ContainerLaunchContext ctx,
                              RoleInstance instance) {
     // Set up tokens for the container too. Today, for normal shell commands,
     // the container in distribute-shell doesn't need any tokens. We are
     // populating them mainly for NodeManagers to be able to download any
     // files in the distributed file-system. The tokens are otherwise also
     // useful in cases, for e.g., when one is running a "hadoop dfs" command
     // inside the distributed shell.
     ctx.setTokens(allTokens.duplicate());
     appState.containerStartSubmitted(container, instance);
     nmClientAsync.startContainerAsync(container, ctx);
   }
 
   @Override //  NMClientAsync.CallbackHandler 
   public void onContainerStopped(ContainerId containerId) {
     // do nothing but log: container events from the AM
     // are the source of container halt details to react to
     log.info("onContainerStopped {} ", containerId);
   }
 
   @Override //  NMClientAsync.CallbackHandler 
   public void onContainerStarted(ContainerId containerId,
                                  Map<String, ByteBuffer> allServiceResponse) {
     LOG_YARN.info("Started Container {} ", containerId);
     RoleInstance cinfo = appState.onNodeManagerContainerStarted(containerId);
     if (cinfo != null) {
       //trigger an async container status
       nmClientAsync.getContainerStatusAsync(containerId,
                                             cinfo.container.getNodeId());
     } else {
       //this is a hypothetical path not seen. We react by warning
       log.error("Notified of started container that isn't pending {} - releasing",
                 containerId);
       //then release it
       asyncRMClient.releaseAssignedContainer(containerId);
     }
   }
 
   @Override //  NMClientAsync.CallbackHandler 
   public void onStartContainerError(ContainerId containerId, Throwable t) {
     LOG_YARN.error("Failed to start Container " + containerId, t);
     appState.onNodeManagerContainerStartFailed(containerId, t);
   }
 
   @Override //  NMClientAsync.CallbackHandler 
   public void onContainerStatusReceived(ContainerId containerId,
                                         ContainerStatus containerStatus) {
     LOG_YARN.debug("Container Status: id={}, status={}", containerId,
                    containerStatus);
   }
 
   @Override //  NMClientAsync.CallbackHandler 
   public void onGetContainerStatusError(
     ContainerId containerId, Throwable t) {
     LOG_YARN.error("Failed to query the status of Container {}", containerId);
   }
 
 
   @Override //  NMClientAsync.CallbackHandler 
   public void onStopContainerError(ContainerId containerId, Throwable t) {
     LOG_YARN.warn("Failed to stop Container {}", containerId);
   }
 
   public Map<Integer, RoleStatus> getRoleStatusMap() {
     return appState.getRoleStatusMap();
   }
 
 
   /**
    The cluster description published to callers
    This is used as a synchronization point on activities that update
    the CD, and also to update some of the structures that
    feed in to the CD
    */
   public ClusterDescription getClusterSpec() {
     return appState.getClusterSpec();
   }
 
   /**
    * This is the status, the live model
    */
   public ClusterDescription getClusterStatus() {
     return appState.getClusterStatus();
   }
 
 }
