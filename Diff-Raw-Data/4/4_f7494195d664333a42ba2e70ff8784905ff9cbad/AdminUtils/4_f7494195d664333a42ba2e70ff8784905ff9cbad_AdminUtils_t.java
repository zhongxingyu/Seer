 package iTests.framework.utils;
 
 import com.gigaspaces.cluster.activeelection.SpaceMode;
 import com.gigaspaces.cluster.replication.async.mirror.MirrorStatistics;
 import com.gigaspaces.grid.gsa.GSProcessOptions;
 import com.gigaspaces.internal.server.space.SpaceImpl;
 import com.gigaspaces.log.LogEntryMatchers;
 import com.gigaspaces.security.directory.CredentialsProvider;
 import com.gigaspaces.security.directory.UserDetails;
 import com.j_spaces.core.client.SpaceURL;
 import com.j_spaces.core.filters.ReplicationStatistics;
 import com.j_spaces.core.filters.ReplicationStatistics.OutgoingChannel;
 import com.j_spaces.core.filters.ReplicationStatistics.OutgoingReplication;
 import com.j_spaces.core.filters.ReplicationStatistics.ReplicationMode;
 import com.j_spaces.kernel.JSpaceUtilities;
 
 import iTests.framework.tools.SGTestHelper;
 import net.jini.core.discovery.LookupLocator;
 import org.openspaces.admin.Admin;
 import org.openspaces.admin.AdminFactory;
 import org.openspaces.admin.esm.ElasticServiceManager;
 import org.openspaces.admin.gsa.*;
 import org.openspaces.admin.gsc.GridServiceContainer;
 import org.openspaces.admin.gsm.GridServiceManager;
 import org.openspaces.admin.internal.InternalAdminFactory;
 import org.openspaces.admin.internal.admin.InternalAdmin;
 import org.openspaces.admin.internal.space.InternalSpaceInstance;
 import org.openspaces.admin.lus.LookupService;
 import org.openspaces.admin.machine.Machine;
 import org.openspaces.admin.pu.ProcessingUnit;
 import org.openspaces.admin.space.Space;
 import org.openspaces.admin.space.SpaceInstance;
 import org.openspaces.admin.space.SpaceInstanceStatistics;
 import org.openspaces.admin.space.Spaces;
 import org.openspaces.security.AdminFilter;
 import org.testng.Assert;
 
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.concurrent.*;
 
 /**
  * Utility methods on top of the Admin API
  * 
  * @author Moran Avigdor
  */
 public class AdminUtils {
 
     private static final List<ExecutorService> scripts = new ArrayList<ExecutorService>();
     private static final long ADMIN_DEFAULT_TIMEOUT_SECONDS = TimeUnit.MINUTES.toSeconds(15); //default - 15 minutes
     private static final long START_GRID_COMPONENT_TIMEOUT_MILLISECONDS = TimeUnit.MINUTES.toMillis(5);
     private static final String WORK_DIR_PROP = "com.gs.work";
     private static final String DEPLOY_DIR_PROP = "com.gs.deploy";
 
     private static final String START_AGENT_COMMAND = "gsa.global.lus=0 gsa.global.gsm=0 gsa.gsc=0";
     private static final String START_AGENT_AND_LUS_COMMAND = "gsa.global.lus=0 gsa.lus=1 gsa.global.gsm=0 gsa.gsc=0";
     private static final String START_AGENT_AND_LUS_AND_GSM_AND_ESM_COMMAND = "gsa.global.lus=0 gsa.lus=1 gsa.global.gsm=0 gsa.gsm=1 gsa.esm=1 gsa.gsc=0";
     private static final String SSH_USERNAME = "tgrid";
     private static final String SSH_PASSWORD = "tgrid";
     private static final long SSH_TIMEOUT = 3 *60 * 1000;
 
     /**
      * @return lookup group environment variable value or null if undefined.
      */
     private static String getGroupsEnvironmentVariable() {
         String groupsProperty = System.getProperty("com.gs.jini_lus.groups");
         if (groupsProperty == null) {
             groupsProperty = System.getenv("LOOKUPGROUPS");
             if (groupsProperty == null)
                 LogUtils.log("Groups are not defined in system and environment.");
             else
                 LogUtils.log("Groups are defined in environment: '" + groupsProperty + "'");
         }
         else
             LogUtils.log("Groups are defined in system: '" + groupsProperty + "'");
         return groupsProperty;
     }
 
     public static String getWorkDirEnvironmentVariable() {
         String workDirProperty = System.getProperty(WORK_DIR_PROP);
         return workDirProperty;
     }
 
     public static String getDeployDirEnvironmentVariable() {
         String deployDirProperty = System.getProperty(DEPLOY_DIR_PROP);
         return deployDirProperty;
     }
 
     public static String getTestGroups()  {
         String groups = getGroupsEnvironmentVariable();
         if (groups == null) {
             try {
                 groups = "sgtest-"+InetAddress.getLocalHost().getHostName();
                 LogUtils.log("Generated test groups: '" + groups + "'");
             } catch (UnknownHostException e) {
                 AssertUtils.assertFail("Failed generating unique group name", e);
             }
         }
         return groups;
     }
 	
     private static Admin createAdmin(AdminFactory adminFactory) {
         if (getGroupsEnvironmentVariable() == null) 
             adminFactory.addGroups(getTestGroups());
          
         Admin admin = adminFactory.createAdmin();
        LogUtils.log("Created admin with groups [" + java.util.Arrays.toString(admin.getGroups()) + "], locators ["
            + java.util.Arrays.toString(admin.getLocators()) + "]");
         return admin;
     }
 	
 	public static Admin createAdmin() {
 		Admin admin = createAdmin(new AdminFactory());
 		admin.setDefaultTimeout(ADMIN_DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS); //default - 15 minutes
 		return admin;
 	}
 
     public static Admin createAdmin( String username, String password, AdminFilter adminFilter ) {
         AdminFactory adminFactory = new AdminFactory();
         if( username != null && password != null ){
             adminFactory.userDetails( username, password );
         }
         if( adminFilter != null ){
             adminFactory.adminFilter( adminFilter );
         }
         Admin admin = createAdmin( adminFactory );
         admin.setDefaultTimeout(ADMIN_DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS); //default - 15 minutes
         return admin;
     }
 
 	public static Admin createAdmin( String username, String password, String locator, AdminFilter adminFilter ) {
 		AdminFactory adminFactory = new AdminFactory();
 		if( username != null && password != null ){
 			adminFactory.userDetails( username, password );
 		}
 		if( adminFilter != null ){
 			adminFactory.adminFilter( adminFilter );
 		}
 		if( locator != null && locator.trim().length() > 0 ){
 			adminFactory.addLocator(locator);
 		}
 		Admin admin = createAdmin( adminFactory );
 		admin.setDefaultTimeout(ADMIN_DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS); //default - 15 minutes
 		return admin;
 	}	
 
 	public static Admin createSingleThreadAdmin() {
 		Admin admin = createAdmin(new InternalAdminFactory().singleThreadedEventListeners());
 		admin.setDefaultTimeout(ADMIN_DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS); //default - 15 minutes
 		return admin;
 	}
 	
     public static Admin createAdminWithLocators(Admin admin) {
         AdminFactory factory = new AdminFactory();
         for (Machine machine : admin.getMachines().getMachines()) {
             factory.addLocator(machine.getHostAddress());
         }
         Admin _admin = createAdmin(factory);
         _admin.setDefaultTimeout(ADMIN_DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS); //default - 15 minutes
         return _admin;
     }
     
     public static Admin createSecuredAdmin(String userName,String password) {
         AdminFactory factory = new AdminFactory().userDetails(userName, password).discoverUnmanagedSpaces();
         Admin _admin = createAdmin(factory);
         _admin.setDefaultTimeout(ADMIN_DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS); //default - 15 minutes
         return _admin;
     }
     
     public static Admin createSecuredAdmin(UserDetails userDetails) {
         AdminFactory factory = new AdminFactory().userDetails(userDetails).discoverUnmanagedSpaces();
         Admin _admin = createAdmin(factory);
         _admin.setDefaultTimeout(ADMIN_DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS); //default - 15 minutes
         return _admin;
     }
     
     public static Admin createSecuredAdmin(CredentialsProvider credentialsProvider) {
         AdminFactory factory = new AdminFactory().credentialsProvider( credentialsProvider ).discoverUnmanagedSpaces();
         Admin _admin = createAdmin(factory);
         _admin.setDefaultTimeout(ADMIN_DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS); //default - 15 minutes
         return _admin;
     }    
 
     public static LookupService loadLUS(GridServiceAgent gsa) {
     	return loadLUS(gsa, null);
     }
     
     /** loads 1 LUS via this GSA, tagged with the list of comma separated zones */
 	public static LookupService loadLUS(GridServiceAgent gsa, String zones) {
 		LookupServiceOptions options = new LookupServiceOptions();
 		if (zones == null) {
             if(getDeployDirEnvironmentVariable() != null)
                 options.vmInputArgument("-Dcom.gs.deploy=" + getDeployDirEnvironmentVariable());
             if(getWorkDirEnvironmentVariable() != null)
                 options.vmInputArgument("-Dcom.gs.work=" +getWorkDirEnvironmentVariable());
 			options.useScript();
 		}
 		if (zones!=null) {
 			options.vmInputArgument("-Dcom.gs.zones="+zones);
 		}
 		return gsa.startGridServiceAndWait(options);
 	}
 	
 	/** loads 1 GSC on this machine */
 	public static GridServiceContainer loadGSC(Machine machine) {
         GridServiceAgent gsa = waitForAgent(machine);
         return loadGSC(gsa);
     }
 	
 	/** loads 1 GSC via this GSA */
 	public static GridServiceContainer loadGSC(GridServiceAgent gsa) {
 		return loadGSC(gsa, null);
 	}
 
 	/** loads 1 GSC via this GSA, tagged with the list of comma separated zones */
 	public static GridServiceContainer loadGSC(GridServiceAgent gsa, String zones) {
 		GridServiceContainerOptions options = new GridServiceContainerOptions();
 		if (zones == null) {
             if(getDeployDirEnvironmentVariable() != null)
                 options.vmInputArgument("-Dcom.gs.deploy=" + getDeployDirEnvironmentVariable());
             if(getWorkDirEnvironmentVariable() != null)
                 options.vmInputArgument("-Dcom.gs.work=" +getWorkDirEnvironmentVariable());
 			options.useScript();
 		}
 		if (zones!=null) {
 			options.vmInputArgument("-Dcom.gs.zones="+zones);
 		}
         return startGridServiceAgentAndWait(gsa, options);
 	}
 	
 	/** loads 1 secured GSC via this GSA, */
 	public static GridServiceContainer loadGSC(GridServiceAgent gsa, boolean secured) {
 		GridServiceContainerOptions options = new GridServiceContainerOptions();
 		if (secured) {
 			options.vmInputArgument("-Dcom.gs.security.enabled="+secured);
 		}
         return startGridServiceAgentAndWait(gsa, options);
 	}
 
     /** loads 1 secured GSC via this GSA, */
     public static GridServiceContainer loadGSCWithCustomSize(GridServiceAgent gsa, int memoryInMbs) {
         GridServiceContainerOptions options = new GridServiceContainerOptions()
                 .vmInputArgument("-Xms" + memoryInMbs + "m")
                 .vmInputArgument("-Xmx" + memoryInMbs + "m");
         return startGridServiceAgentAndWait(gsa, options);
     }
 
 	/** loads n GSCs simultaneously */
 	public static GridServiceContainer[] loadGSCs(Machine machine, int nGSCs) {
 		return loadGSCs(waitForAgent(machine), nGSCs);
 	}
 
     private static GridServiceAgent waitForAgent(Machine machine) {
         GridServiceAgent gsa = machine.getGridServiceAgents().waitForAtLeastOne();
         AssertUtils.assertNotNull(gsa);
         return gsa;
     }
 
 	/** loads n GSCs simultaneously */
 	public static GridServiceContainer[] loadGSCs(Machine machine, int nGSCs, String zones) {
 		return loadGSCs(waitForAgent(machine), nGSCs, zones);
 	}
 	
 	/** loads 1 GSC via this GSA, tagged with the list of comma separated zones */
 	public static GridServiceContainer loadGSC(Machine machine, String zones) {
 		GridServiceAgent gsa = waitForAgent(machine);
 		return gsa.startGridServiceAndWait(new GridServiceContainerOptions().vmInputArgument("-Dcom.gs.zones="+zones));
 	}
 
     /** loads 1 GSC via this GSA, tagged with the list of comma separated system properties */
 	public static GridServiceContainer loadGSCWithSystemProperty(Machine machine, String ... systemProperties) {
 		GridServiceAgent gsa = waitForAgent(machine);
         return loadGSCWithSystemProperty(gsa, true, systemProperties);
 	}
 	
 	/** loads 1 GSC via this GSA, tagged with the list of comma separated system properties */
 	public static GridServiceContainer loadGSCWithSystemProperty(GridServiceAgent gsa, boolean override, String ... systemProperties) {
         GridServiceContainerOptions options = new GridServiceContainerOptions();
         for(String prop : systemProperties){
             options = options.vmInputArgument(prop);    
         }
         if (override) {
         	if(getDeployDirEnvironmentVariable() != null)
     			options.vmInputArgument("-Dcom.gs.deploy=" + getDeployDirEnvironmentVariable());
     		if(getWorkDirEnvironmentVariable() != null)
     			options.vmInputArgument("-Dcom.gs.work=" +getWorkDirEnvironmentVariable());
         	options.overrideVmInputArguments();
         }
 		return startGridServiceAgentAndWait(gsa, options);
 	}
 	
 	private static GridServiceContainer startGridServiceAgentAndWait(GridServiceAgent gsa,
 			GridServiceContainerOptions options) {
         LogUtils.log("Starting GSC " + toString(options));
 		GridServiceContainer container = gsa.startGridServiceAndWait(options);
 		
 		//added validation to detect sporadic NPE
 		if (container.getVirtualMachine() == null) {
 			throw new IllegalStateException("container.getVirtualMachine() is null");
 		}
 		if (container.getVirtualMachine().getDetails() == null) {
 			throw new IllegalStateException("container.getVirtualMachine().getDetails() is null");
 		}
 		if (container.getMachine() == null) {
 			throw new IllegalStateException("container.getMachine() us null");
 		}
         LogUtils.log("GSC [" + container.getVirtualMachine().getDetails().getPid()+"] started on "+ container.getMachine().getHostAddress());
 		return container;
 		
 	}
 	
 	/** loads 1 GSC via this GSA, tagged with the list of comma separated system properties */
 	public static GridServiceContainer loadGSCWithSystemProperty(GridServiceAgent gsa, String ... systemProperties) {
         GridServiceContainerOptions options = new GridServiceContainerOptions();
         for(String prop : systemProperties){
             options = options.vmInputArgument(prop);    
         }
         options.overrideVmInputArguments();
 		return gsa.startGridServiceAndWait(options);
 	}
 	
 	/** loads n GSCs simultaneously and returns them */
 	public static GridServiceContainer[] loadGSCsWithSystemProperty(final GridServiceAgent gsa, final boolean override, int nGSCs, final String ... systemProperties) {
 	
 		ExecutorService threadPool = Executors.newFixedThreadPool(nGSCs);
 		Future<GridServiceContainer>[] futures = new Future[nGSCs];
 		for (int i=0; i<nGSCs; ++i) {
 			futures[i] = threadPool.submit(new Callable<GridServiceContainer>() {
 				public GridServiceContainer call() throws Exception {
 					return loadGSCWithSystemProperty(gsa, override, systemProperties);
 				}
 			});
 		}
 		
 		GridServiceContainer[] gscs = new GridServiceContainer[nGSCs]; 
 		for (int i=0; i<nGSCs; ++i) {
 			try {
 				gscs[i] = futures[i].get();
 			} catch (Exception e) {
 				throw new RuntimeException(e);
 			}
 		}
 		
 		threadPool.shutdown();
 		try {
 			threadPool.awaitTermination(START_GRID_COMPONENT_TIMEOUT_MILLISECONDS, TimeUnit.MILLISECONDS);
 		} catch (InterruptedException e) {
 			throw new RuntimeException(e);
 		}
 		return gscs;
 	}
 	
 	/** loads 1 GSM on this machine */
 	public static GridServiceManager loadGSM(Machine machine) {
         GridServiceAgent gsa = waitForAgent(machine);
         return loadGSM(gsa);
     }
 	
 	/** loads 1 GSM via this GSA */
 	public static GridServiceManager loadGSM(GridServiceAgent gsa) {
         GridServiceManagerOptions options = new GridServiceManagerOptions();
         if(getDeployDirEnvironmentVariable() != null)
             options.vmInputArgument("-Dcom.gs.deploy=" + getDeployDirEnvironmentVariable());
         if(getWorkDirEnvironmentVariable() != null)
             options.vmInputArgument("-Dcom.gs.work=" +getWorkDirEnvironmentVariable());
         return gsa.startGridServiceAndWait(options.useScript());
     }
 
     /** loads 1 GSC via this GSA  in remote Debug mode*/
     public static GridServiceContainer loadGSCWithDebugger(GridServiceAgent gsa, int debugPort, boolean suspend) {
         GridServiceContainerOptions options = new GridServiceContainerOptions();
         if(getDeployDirEnvironmentVariable() != null)
             options.vmInputArgument("-Dcom.gs.deploy=" + getDeployDirEnvironmentVariable());
         if(getWorkDirEnvironmentVariable() != null)
             options.vmInputArgument("-Dcom.gs.work=" +getWorkDirEnvironmentVariable());
         options.environmentVariable("GSC_JAVA_OPTIONS", "-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,address="+debugPort+",suspend="+(suspend?"y":"n"));
         return gsa.startGridServiceAndWait(options.useScript());
     }
 
 	/** loads 1 GSM via this GSA */
     public static GridServiceManager loadGSMWithDebugger(GridServiceAgent gsa, int debugPort, boolean suspend) {
         GridServiceManagerOptions options = new GridServiceManagerOptions();
         if(getDeployDirEnvironmentVariable() != null)
             options.vmInputArgument("-Dcom.gs.deploy=" + getDeployDirEnvironmentVariable());
         if(getWorkDirEnvironmentVariable() != null)
             options.vmInputArgument("-Dcom.gs.work=" +getWorkDirEnvironmentVariable());
         options.environmentVariable("GSC_JAVA_OPTIONS", "-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,address="+debugPort+",suspend="+(suspend?"y":"n"));
         return gsa.startGridServiceAndWait(options.useScript());
     }
 
     /** loads 1 secured GSM via this GSA, */
     public static GridServiceManager loadGSM(GridServiceAgent gsa, boolean secured) {
         GridServiceManagerOptions options = new GridServiceManagerOptions();
         if (secured) {
             options.vmInputArgument("-Dcom.gs.security.enabled="+secured);
         }
         return gsa.startGridServiceAndWait(options);
     }
 	
     public static GridServiceManager loadGSMWithSystemProperty(Machine machine, String ... systemProperties) {
         GridServiceAgent gsa = waitForAgent(machine);
         GridServiceManagerOptions options = new GridServiceManagerOptions();
         for(String prop : systemProperties){
             options = options.vmInputArgument(prop);
         }
         if(getDeployDirEnvironmentVariable() != null)
             options.vmInputArgument("-Dcom.gs.deploy=" + getDeployDirEnvironmentVariable());
         if(getWorkDirEnvironmentVariable() != null)
             options.vmInputArgument("-Dcom.gs.work=" +getWorkDirEnvironmentVariable());
         options.overrideVmInputArguments();
         return gsa.startGridServiceAndWait(options);
     }
 
     public static GridServiceManager loadGSMWithSystemProperty(boolean override, GridServiceAgent gsa, String ... systemProperties) {
         GridServiceManagerOptions options = new GridServiceManagerOptions();
         for(String prop : systemProperties){
             options = options.vmInputArgument(prop);
         }
         if (override) {
             if(getDeployDirEnvironmentVariable() != null)
                 options.vmInputArgument("-Dcom.gs.deploy=" + getDeployDirEnvironmentVariable());
             if(getWorkDirEnvironmentVariable() != null)
                 options.vmInputArgument("-Dcom.gs.work=" +getWorkDirEnvironmentVariable());
             options.overrideVmInputArguments();
         }
         return gsa.startGridServiceAndWait(options);
     }
     
 	/** loads n GSCs simultaneously and returns them */
 	public static GridServiceContainer[] loadGSCs(final GridServiceAgent gsa, int nGSCs) {
 		return loadGSCs(gsa, nGSCs, null);
 	}
 	
 	/** loads n GSCs simultaneously and returns them */
 	public static GridServiceContainer[] loadGSCs(final GridServiceAgent gsa, int nGSCs, final String zones) {
 
         if (zones == null) {
             return loadGSCsWithSystemProperty(gsa, false, nGSCs);
         }
         else {
             String systemProperties = "-Dcom.gs.zones="+zones;
             return loadGSCsWithSystemProperty(gsa, false, nGSCs, systemProperties);
         }
 	}
 	
 	/** loads n GSMs simultaneously and returns them */
 	public static GridServiceManager[] loadGSMs(final Machine m, int nGSMs) {
 		ExecutorService threadPool = Executors.newFixedThreadPool(nGSMs);
 		Future<GridServiceManager>[] futures = new Future[nGSMs];
 		for (int i=0; i<nGSMs; ++i) {
 			futures[i] = threadPool.submit(new Callable<GridServiceManager>() {
 				public GridServiceManager call() throws Exception {
 					return loadGSM(m);
 				}
 			});
 		}
 		
 		GridServiceManager[] gsms = new GridServiceManager[nGSMs]; 
 		for (int i=0; i<nGSMs; ++i) {
 			try {
 				gsms[i] = futures[i].get();
 			} catch (Exception e) {
 				throw new RuntimeException(e);
 			}
 		}
 		
 		threadPool.shutdown();
 		try {
 			threadPool.awaitTermination(START_GRID_COMPONENT_TIMEOUT_MILLISECONDS, TimeUnit.MILLISECONDS);
 		} catch (InterruptedException e) {
 			throw new RuntimeException(e);
 		}
 		return gsms;
 	}
 	
 	/** load nGSMs and nGSCs simultaneously, with no return value  */
 	public static void loadGSMsGSCs(final Machine machine, int nGSMs, int nGSCs) {
 		ExecutorService threadPool = Executors.newFixedThreadPool(nGSMs + nGSCs);
 		
 		for (int i=0; i<nGSMs; ++i) {
 			threadPool.submit(new Runnable() {
 				public void run() {
 					loadGSM(machine);
 				}
 			});
 		}
 		
 		for (int i=0; i<nGSCs; ++i) {
 			threadPool.submit(new Runnable() {
 				public void run() {
 					loadGSC(machine);
 				}
 			});
 		}
 		
 		// Initiates an orderly shutdown in which previously submitted tasks are
 		// executed, but no new tasks will be accepted.
 		threadPool.shutdown();
 		try {
 			threadPool.awaitTermination(START_GRID_COMPONENT_TIMEOUT_MILLISECONDS, TimeUnit.MILLISECONDS);
 		} catch (InterruptedException e) {
 			throw new RuntimeException(e);
 		}
 	}
 	
 	/** Waits for the GSM to print out:  "Registered GSC .... count [2]" */
 	public static boolean waitForRegisteredGSCs(GridServiceManager gsm, int nGSCs) {
 		long interval = 500;
 		long timeout = 60000;
 		while (gsm.logEntries(LogEntryMatchers.regex(".*(Registered GSC).*(count \\["+nGSCs+"\\]).*")).getEntries().size() < nGSCs) {
 			try {
 				Thread.sleep(interval);
 				timeout -= interval;
 				if (timeout <= 0) return false;
 			} catch (InterruptedException e) {
 			}
 		}
 		return true;
 	}
 	
 	public static ElasticServiceManager loadESM(GridServiceAgent gsa) {
         return loadESMWithSystemProperties(gsa);
     }
 
     public static ElasticServiceManager loadESMWithSystemProperties(GridServiceAgent gsa, String... systemProperties) {
         LogUtils.log("Starting esm");
         ElasticServiceManagerOptions options = new ElasticServiceManagerOptions();
         for (String prop : systemProperties) {
             options.vmInputArgument(prop);
         }
         ElasticServiceManager esm = gsa.startGridServiceAndWait(options);
         Assert.assertNotNull(esm);
         LogUtils.log("Started esm " + esm.getVirtualMachine().getDetails().getPid());
         return esm;
     }
 
 	public static ElasticServiceManager loadESMWithDebugger(GridServiceAgent gsa, int debugPort, boolean suspend) {
 		return gsa.startGridServiceAndWait(
 				new ElasticServiceManagerOptions()
 				.vmInputArgument("-Xdebug")
 				.vmInputArgument("-Xnoagent")
 				.vmInputArgument("-Djava.compiler=NONE")
 				.vmInputArgument("-Xrunjdwp:transport=dt_socket,server=y,address=" + debugPort + ",suspend=" + (suspend ? "y" : "n")));
     }
 
     public static void startGSAAndLUSOnHost(final String hostName) {
         startGridServiceOnHost(hostName, AdminUtils.getTestGroups(), START_AGENT_AND_LUS_COMMAND);
     }
 
     public static void startGSAAndLUSAndGSMAndESMOnHost(final String hostName) {
         startGridServiceOnHost(hostName, AdminUtils.getTestGroups(), START_AGENT_AND_LUS_AND_GSM_AND_ESM_COMMAND);
     }
 
     public static void startGSAonHost(final String hostName) {
         startGridServiceOnHost(hostName, AdminUtils.getTestGroups(), START_AGENT_COMMAND);
     }
 
     protected static void startGridServiceOnHost(final String hostName, final String lookupGroup, final String services) {
         if(hostName == null || hostName.isEmpty()){
             LogUtils.log("Not starting GSA And LUS - invalid host name");
             return;
         }
 
         LogUtils.log("Starting " + services + " on machine " + hostName);
         ExecutorService script = Executors.newSingleThreadExecutor();
         script.submit(new Runnable() {
             @SuppressWarnings("deprecation")
             @Override
             public void run() {
                 SSHUtils.runCommand(hostName, SSH_TIMEOUT, buildCommand(services, lookupGroup) , SSH_USERNAME, SSH_PASSWORD);
             }
         });
         scripts.add(script);
     }
 
     private static String buildCommand(String startupCommand, String lookupGroup) {
         LogUtils.log("Using group " + lookupGroup + " to start services");
         String setLookupGroupCommand = "LOOKUPGROUPS=" + lookupGroup;
         String exportLookupGroups = "export LOOKUPGROUPS";
         String buildPath = getBuildPath();
         String pathToAgent = "cd " + buildPath + "/bin";
         String deployAndWorkDir ="";
         String work, deploy;
         if((work = System.getProperty("com.gs.work")) != null && (deploy = System.getProperty("com.gs.deploy")) != null){
             LogUtils.log("Using deploy dir: " + deploy + " and work dir: " + work);
             String setJavaOptionsCommand = "GSA_JAVA_OPTIONS=\"-Dcom.gs.work=" + work +" -Dcom.gs.deploy=" + deploy + "\"";
             String exportJavaOptions = "export GSA_JAVA_OPTIONS";
             deployAndWorkDir = setJavaOptionsCommand + ";" + exportJavaOptions + ";";
         }
         return  deployAndWorkDir + setLookupGroupCommand  + ";" + exportLookupGroups + ";" + pathToAgent + ";  ./gs-agent.sh " + startupCommand;
 
     }
 
     private static String getBuildPath() {
         return SGTestHelper.getBuildDir();
     }
 
     public static Admin cloneAdmin(Admin admin) {
 
         InternalAdminFactory factory = new InternalAdminFactory();
         for (String group : admin.getGroups()) {
             factory.addGroup(group);
         }
 
         for (LookupLocator locator :admin.getLocators()) {
             factory.addLocator(locator.getHost()+":"+locator.getPort());
         }
 
         if (((InternalAdmin)admin).isSingleThreadedEventListeners()) {
             factory.singleThreadedEventListeners();
         }
 
         return factory.createAdmin();
     }
 
     public static SpaceInstance getPrimarySpaceInstance( Space space ){
         SpaceInstance retValue = null;
         for( SpaceInstance spaceInstance : space ){
             SpaceMode mode = spaceInstance.getMode();
             if( mode.equals( SpaceMode.PRIMARY ) ){
                 retValue = spaceInstance;
                 break;
             }
         }
 
         return retValue;
     }
 
     public static String toString(GridServiceContainerOptions options) {
         GSProcessOptions processOptions = options.getOptions();
         return "GSProcessOptions ["
                 + (processOptions.getType() != null ? "type=" + processOptions.getType() + ", " : "")
                 + "useScript="
                 + processOptions.isUseScript()
                 + ", "
                 + (processOptions.getScriptArguments() != null ? "scriptArguments="
                 + Arrays.toString(processOptions.getScriptArguments()) + ", " : "")
                 + (processOptions.getScriptAppendableArguments() != null ? "scriptAppendableArguments="
                 + Arrays.toString(processOptions.getScriptAppendableArguments()) + ", "
                 : "")
                 + (processOptions.getVmArguments() != null ? "vmArguments="
                 + Arrays.toString(processOptions.getVmArguments()) + ", " : "")
                 + (processOptions.getVmAppendableArguments() != null ? "vmAppendableArguments="
                 + Arrays.toString(processOptions.getVmAppendableArguments()) + ", " : "")
                 + (processOptions.getVmInputArguments() != null ? "vmInputArguments="
                 + Arrays.toString(processOptions.getVmInputArguments()) + ", " : "")
                 + (processOptions.getVmAppendableInputArguments() != null ? "vmAppendableInputArguments="
                 + Arrays.toString(processOptions.getVmAppendableInputArguments()) + ", "
                 : "")
                 + (processOptions.getEnvironmentVariables() != null ? "environmentVariables="
                 + processOptions.getEnvironmentVariables() + ", " : "")
                 + (processOptions.getRestartOnExit() != null ? "restartOnExit=" + processOptions.getRestartOnExit()
                 : "") + "]";
     }
 
 
 
 
     public static PuDataReplicationThroughputStatisticsWrapper getTotalDataReplicationThrougputFromAdmin( ProcessingUnit processingUnit ){
 
         Space space = processingUnit.getSpace();
 
         long totalSendPacketsPerSecond = 0;
         long totalSendBytesPerSecond = 0;
 
         LogUtils.log( ">>>>>>>>>>>>>>>>> Calculate Total Data Replication Througput" );
 
         if( space != null ){
             for( SpaceInstance spaceInstance : space ){
                 if( !spaceInstance.getMode().equals( SpaceMode.BACKUP ) ){
                     SpaceInstanceStatistics spaceInstanceStatistics = spaceInstance.getStatistics();
                     if( spaceInstanceStatistics != null && spaceInstanceStatistics.getMirrorStatistics() == null ){
                         ReplicationStatistics replicationStatistics =
                                 spaceInstanceStatistics.getReplicationStatistics();
                         if( replicationStatistics != null ){
                             OutgoingReplication outgoingReplication =
                                     replicationStatistics.getOutgoingReplication();
                             List<OutgoingChannel> channels = outgoingReplication.getChannels();
 
                             for( OutgoingChannel outgoingChannel : channels ){
                                 ReplicationMode replicationMode = outgoingChannel.getReplicationMode();
                                 //for not to mirror replication
                                 if( !replicationMode.equals( ReplicationMode.MIRROR ) ){
                                     long sendBytesPerSecond = outgoingChannel.getSendBytesPerSecond();
                                     int sendPacketsPerSecond = outgoingChannel.getSendPacketsPerSecond();
 
                                     sendBytesPerSecond = sendBytesPerSecond < 0 ? 0 : sendBytesPerSecond;
                                     sendPacketsPerSecond = sendPacketsPerSecond < 0 ? 0 : sendPacketsPerSecond;
 
                                     totalSendBytesPerSecond += sendBytesPerSecond;
                                     totalSendPacketsPerSecond += sendPacketsPerSecond;
 
                                     LogUtils.log( "> totalSendBytesPerSecond=" + totalSendBytesPerSecond +
                                             ", sendBytesPerSecond=" + sendBytesPerSecond +
                                             ", replicationMode=" + replicationMode );
                                 }
                             }
                         }
                     }
                 }
             }
         }
         return new PuDataReplicationThroughputStatisticsWrapper(
                 totalSendPacketsPerSecond, totalSendBytesPerSecond );
     }
 
 
     public static PuMirrorThroughputStatisticsWrapper getTotalMirrorThrougputFromAdmin(
             Admin admin ){
 
         List<SpaceInstance> spaceInstances = new ArrayList<SpaceInstance>();
         Spaces spaces = admin.getSpaces();
         for( Space space : spaces ){
             SpaceInstance[] instances = space.getInstances();
             spaceInstances.addAll( Arrays.asList( instances ) );
         }
 
         long totalSendPacketsPerSecond = 0;
         long totalSendBytesPerSecond = 0;
         double totalSuccessfulOperationsPerSecond = 0;
 
         for( SpaceInstance spaceInstance : spaceInstances ) {
             SpaceInstanceStatistics statistics = spaceInstance.getStatistics();
             //mirror will not be taken in calculation
             if( statistics != null ){
                 MirrorStatistics mirrorOperationStatistics = statistics.getMirrorStatistics();
                 //not mirror
                 if( mirrorOperationStatistics == null ){
 
                     ReplicationStatistics replicationStatistics =
                             statistics.getReplicationStatistics();
                     if( replicationStatistics != null ){
                         OutgoingReplication outgoingReplication =
                                 replicationStatistics.getOutgoingReplication();
                         List<OutgoingChannel> channels = outgoingReplication.getChannels();
 
                         for( OutgoingChannel outgoingChannel : channels ){
                             //only for mirror replication
                             if( outgoingChannel.getReplicationMode().equals( ReplicationMode.MIRROR ) ){
                                 long sendBytesPerSecond = outgoingChannel.getSendBytesPerSecond();
                                 long sendPacketsPerSecond = outgoingChannel.getSendPacketsPerSecond();
 
                                 sendBytesPerSecond = sendBytesPerSecond < 0 ? 0 : sendBytesPerSecond;
                                 sendPacketsPerSecond = sendPacketsPerSecond < 0 ? 0 : sendPacketsPerSecond;
 
                                 totalSendBytesPerSecond += sendBytesPerSecond;
                                 totalSendPacketsPerSecond += sendPacketsPerSecond;
                             }
                         }
                     }
                 }
                 //mirror
                 else{
 
                     double successfulOperationsThroughput = 0;
                     SpaceInstanceStatistics previousStats = statistics.getPrevious();
                     MirrorStatistics prevMirrorStatistics = previousStats == null ?
                             null : previousStats.getMirrorStatistics();
 
                     if( prevMirrorStatistics != null ){
 
                         long currentTimestamp = statistics.getAdminTimestamp();
                         long previousTimestamp = previousStats.getAdminTimestamp();
 
                         long previousCount =
                                 prevMirrorStatistics.getSuccessfulOperationCount();
                         long successfulOperationsCount =
                                 mirrorOperationStatistics.getSuccessfulOperationCount();
 
                         //calculate successful operations throughput
                         successfulOperationsThroughput =
                                 org.openspaces.admin.support.StatisticsUtils.computePerSecond(
                                         successfulOperationsCount, previousCount,
                                         currentTimestamp, previousTimestamp );
 
                         LogUtils.log(
                                 "!!! Calculated mirror successfulOperationsTP: " +
                                         successfulOperationsThroughput +
                                         ", from: successfulOperationsCount=" + successfulOperationsCount +
                                         ", previousCount=" + previousCount +
                                         ", currentTimestamp=" + currentTimestamp +
                                         ", previousTimestamp=" + previousTimestamp );
                     }
 
                     totalSuccessfulOperationsPerSecond += successfulOperationsThroughput;
                 }
             }
         }
 
         return new PuMirrorThroughputStatisticsWrapper(
                 totalSendPacketsPerSecond, totalSendBytesPerSecond,
                 totalSuccessfulOperationsPerSecond );
     }
 
 
     public static class PuDataReplicationThroughputStatisticsWrapper{
 
         private final long totalSendPacketsPerSecond;
         private final long totalSendBytesPerSecond;
 
         public PuDataReplicationThroughputStatisticsWrapper( long totalSendPacketsPerSecond,
                                                              long totalSendBytesPerSecond ){
 
             this.totalSendPacketsPerSecond = totalSendPacketsPerSecond;
             this.totalSendBytesPerSecond = totalSendBytesPerSecond;
         }
 
         public long getTotalSendPacketsPerSecond() {
             return totalSendPacketsPerSecond;
         }
 
         public long getTotalSendBytesPerSecond() {
             return totalSendBytesPerSecond;
         }
     }
 
     public static class PuMirrorThroughputStatisticsWrapper extends
             PuDataReplicationThroughputStatisticsWrapper{
 
         private final double totalSuccessfulOperationsPerSecond;
 
         public PuMirrorThroughputStatisticsWrapper( long totalSendPacketsPerSecond,
                                                     long totalSendBytesPerSecond,
                                                     double totalSuccessfulOperationsPerSecond ){
 
             super( totalSendPacketsPerSecond, totalSendBytesPerSecond );
 
             this.totalSuccessfulOperationsPerSecond = totalSuccessfulOperationsPerSecond;;
         }
 
         public double getTotalSuccessfulOperationsPerSecond() {
             return totalSuccessfulOperationsPerSecond;
         }
     }
     
 	public static String createSpaceInstanceName( SpaceInstance spaceInstance ){
 		
 		String className = SpaceImpl.class.getName();
 		StringBuilder strBuilder = new StringBuilder( className );
 		strBuilder.append( "(" );
 		
 		SpaceURL spaceUrl = spaceInstance.getSpaceUrl();
 		String spaceContainerName = spaceUrl.getContainerName();
 		String spaceName = ( ( InternalSpaceInstance )spaceInstance ).getSpaceName();
 		String fullSpaceName = JSpaceUtilities.createFullSpaceName( spaceContainerName, spaceName );
 		
 		strBuilder.append( fullSpaceName );
 		strBuilder.append( ")" );
 
 		return strBuilder.toString();        
 	}    
 }
