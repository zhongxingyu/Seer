 package com.conx.logistics.kernel.bpm.impl.jbpm;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.PrintWriter;
 import java.io.Reader;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.lang.reflect.InvocationTargetException;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 
 import javax.naming.Context;
 import javax.naming.NamingException;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.Query;
 import javax.transaction.HeuristicMixedException;
 import javax.transaction.HeuristicRollbackException;
 import javax.transaction.NotSupportedException;
 import javax.transaction.RollbackException;
 import javax.transaction.SystemException;
 import javax.transaction.TransactionManager;
 import javax.transaction.UserTransaction;
 
 import org.drools.KnowledgeBase;
 import org.drools.KnowledgeBaseFactory;
 import org.drools.WorkingMemory;
 import org.drools.agent.KnowledgeAgent;
 import org.drools.agent.KnowledgeAgentConfiguration;
 import org.drools.agent.KnowledgeAgentFactory;
 import org.drools.command.impl.CommandBasedStatefulKnowledgeSession;
 import org.drools.command.impl.KnowledgeCommandContext;
 import org.drools.core.util.StringUtils;
 import org.drools.definition.KnowledgePackage;
 import org.drools.definition.process.Node;
 import org.drools.definition.process.Process;
 import org.drools.event.ActivationCancelledEvent;
 import org.drools.event.ActivationCreatedEvent;
 import org.drools.event.AfterActivationFiredEvent;
 import org.drools.event.AgendaGroupPoppedEvent;
 import org.drools.event.AgendaGroupPushedEvent;
 import org.drools.event.BeforeActivationFiredEvent;
 import org.drools.event.RuleFlowGroupActivatedEvent;
 import org.drools.event.RuleFlowGroupDeactivatedEvent;
 import org.drools.impl.EnvironmentFactory;
 import org.drools.impl.StatefulKnowledgeSessionImpl;
 import org.drools.io.ResourceChangeScannerConfiguration;
 import org.drools.io.ResourceFactory;
 import org.drools.marshalling.ObjectMarshallingStrategy;
 import org.drools.marshalling.impl.ClassObjectMarshallingStrategyAcceptor;
 import org.drools.marshalling.impl.SerializablePlaceholderResolverStrategy;
 import org.drools.persistence.jpa.JPAKnowledgeService;
 import org.drools.persistence.jpa.marshaller.JPAPlaceholderResolverStrategy;
 import org.drools.process.instance.WorkItemHandler;
 import org.drools.runtime.Environment;
 import org.drools.runtime.EnvironmentName;
 import org.drools.runtime.KnowledgeSessionConfiguration;
 import org.drools.runtime.StatefulKnowledgeSession;
 import org.jboss.bpm.console.client.model.ProcessDefinitionRef;
 import org.jboss.bpm.console.client.model.ProcessInstanceRef;
 import org.jboss.bpm.console.client.model.ProcessInstanceRef.RESULT;
 import org.jboss.bpm.console.client.model.ProcessInstanceRef.STATE;
 import org.jboss.bpm.console.client.model.TaskRef;
 import org.jbpm.process.audit.JPAWorkingMemoryDbLogger;
 import org.jbpm.process.audit.VariableInstanceLog;
 import org.jbpm.process.workitem.wsht.CommandBasedWSHumanTaskHandler;
 import org.jbpm.process.workitem.wsht.GenericHTWorkItemHandler;
 import org.jbpm.process.workitem.wsht.SyncWSHumanTaskHandler;
 import org.jbpm.task.Content;
 import org.jbpm.task.Group;
 import org.jbpm.task.Task;
 import org.jbpm.task.User;
 import org.jbpm.task.query.TaskSummary;
 import org.jbpm.task.service.ContentData;
 import org.jbpm.task.service.TaskClient;
 import org.jbpm.task.service.TaskService;
 import org.jbpm.task.service.TaskServiceSession;
 import org.jbpm.task.service.local.LocalTaskService;
 import org.jbpm.task.service.mina.MinaTaskServer;
 import org.jbpm.task.utils.ContentMarshallerContext;
 import org.jbpm.task.utils.ContentMarshallerHelper;
 import org.jbpm.workflow.core.node.HumanTaskNode;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.jndi.JndiTemplate;
 import org.springframework.transaction.PlatformTransactionManager;
 
 import com.conx.logistics.kernel.bpm.impl.jbpm.bam.CustomJPAProcessInstanceDbLog;
 import com.conx.logistics.kernel.bpm.impl.jbpm.core.HumanTaskService;
 import com.conx.logistics.kernel.bpm.impl.jbpm.core.ManagementFactory;
 import com.conx.logistics.kernel.bpm.impl.jbpm.core.ProcessGraphManagement;
 import com.conx.logistics.kernel.bpm.impl.jbpm.core.ProcessManagement;
 import com.conx.logistics.kernel.bpm.impl.jbpm.core.TaskClientFactory;
 import com.conx.logistics.kernel.bpm.impl.jbpm.core.TaskManagement;
 import com.conx.logistics.kernel.bpm.impl.jbpm.shared.utils.GuvnorConnectionUtils;
 import com.conx.logistics.kernel.bpm.services.IBPMService;
 import com.conx.logistics.kernel.bpm.services.IBPMTaskService;
 
 import bitronix.tm.jndi.BitronixInitialContextFactory;
 
 public class BPMServerImpl implements IBPMService {
 	private static final Logger logger = LoggerFactory
 			.getLogger(BPMServerImpl.class);
 
 	private KnowledgeAgent kagent;
 	private Properties jbpmProperties = new Properties();
 	private int ksessionId = 0;
     private Set<String> knownPackages;
 	private StatefulKnowledgeSession ksession;
 
 	private EntityManagerFactory jbpmEMF;
 	private EntityManagerFactory jbpmTaskEMF;
 	private EntityManagerFactory conxlogisticsEMF;
 	
 	private TransactionManager globalTransactionManager;
 	private UserTransaction globalUserTransaction;
 	private PlatformTransactionManager globalJPATransManager;
 	private JndiTemplate jndiTemplate;
 
 	private ManagementFactory managementFactory;
 
 	private ProcessManagement processManager;
 	private ProcessGraphManagement processGraphManager;
 	
 	private CustomJPAProcessInstanceDbLog jpaProcessInstanceDbLog;
 	
 	private final Map<String, Map<String,WorkItemHandler>> wihCache = Collections
 			.synchronizedMap(new HashMap<String, Map<String,WorkItemHandler>>());
 
 	private TaskManagement taskManager;
 
 	private HumanTaskService humanTaskManager;	
 	
 	private IBPMTaskService localHumanTaskServer;
 
 	//private SyncWSHumanTaskHandler handler;
 
 	private GenericHTWorkItemHandler hthandler;
 	
 	public IBPMTaskService getLocalHumanTaskServer() {
 		return localHumanTaskServer;
 	}
 
 	public void setLocalHumanTaskServer(IBPMTaskService localHumanTaskServer) {
 		this.localHumanTaskServer = localHumanTaskServer;
 	}
 
 	public void setJbpmEMF(EntityManagerFactory jbpmEMF) {
 		this.jbpmEMF = jbpmEMF;
 	}
 	
 	public void setJbpmTaskEMF(EntityManagerFactory jbpmTaskEMF) {
 		this.jbpmTaskEMF = jbpmTaskEMF;
 	}
 	
 	public EntityManagerFactory getJbpmTaskEMF() {
 		return this.jbpmTaskEMF;
 	}
 	
 
 	public EntityManagerFactory getConxlogisticsEMF() {
 		return conxlogisticsEMF;
 	}
 
 	public void setConxlogisticsEMF(EntityManagerFactory conxlogisticsEMF) {
 		this.conxlogisticsEMF = conxlogisticsEMF;
 	}
 
 	public void setGlobalTransactionManager(
 			TransactionManager globalTransactionManager) {
 		this.globalTransactionManager = globalTransactionManager;
 	}
 
 	public void setGlobalUserTransaction(UserTransaction globalUserTransaction) {
 		this.globalUserTransaction = globalUserTransaction;
 	}
 
 	public void setGlobalJPATransManager(
 			PlatformTransactionManager globalJPATransManager) {
 		this.globalJPATransManager = globalJPATransManager;
 	}
 
 	public void setJndiTemplate(JndiTemplate jndiTemplate) {
 		this.jndiTemplate = jndiTemplate;
 	}
 	
 
 	public KnowledgeAgent getKagent() {
 		return kagent;
 	}
 
 	public int getKsessionId() {
 		return ksessionId;
 	}
 
 	public StatefulKnowledgeSession getKsession() {
 		return ksession;
 	}
 
 	@Override
 	public EntityManagerFactory getJbpmEMF() {
 		return jbpmEMF;
 	}
 	
 	public UserTransaction acquireUserTransaction() throws NamingException
 	{
 		 Context ctx = jndiTemplate.getContext();
 		 UserTransaction ut = (UserTransaction)ctx.lookup( "java:comp/UserTransaction" );
 		 return ut;
 	}
 	
 	public ManagementFactory getManagementFactory() {
 		return managementFactory;
 	}
 
 	public ProcessManagement getProcessManager() {
 		return processManager;
 	}
 
 	
 	private void start() {
 		jbpmProperties = loadJbpmProperties();
 		KnowledgeBase localKBase = loadKnowledgeBase(jbpmProperties);
 		
 		this.managementFactory = new ManagementFactory(this);
 		this.processManager = this.managementFactory.createProcessManagement();
 		this.taskManager = this.managementFactory.createTaskManagement();
 		this.processGraphManager = new ProcessGraphManagement(this);
 		this.humanTaskManager = new HumanTaskService(this);
 		this.jpaProcessInstanceDbLog = new CustomJPAProcessInstanceDbLog(this.jbpmEMF,this.globalUserTransaction);
 
 		// try to restore known session id for reuse
 		ksessionId = getPersistedSessionId(jbpmProperties.getProperty(
 				"jbpm.conxrepo.tmp.dir",
 				System.getProperty("jboss.server.temp.dir")));
 		// Create knowledge session
 		ksession = createOrLoadStatefulKnowledgeSession(localKBase);
 		
 		KnowledgeBase kbase = ksession.getKnowledgeBase();
 		Collection<KnowledgePackage> pkgs = kbase.getKnowledgePackages();
 		
 
 		
 		persistSessionId(jbpmProperties.getProperty("jbpm.conxrepo.tmp.dir",
 				System.getProperty("jboss.server.temp.dir")));
 		// Additional necessary modifications to the knowledge session
 		new JPAWorkingMemoryDbLogger(ksession);
 		// Adds a work item handler for work items titled "Human Task"		
 		registerWorkItemHandler(ksession, jbpmProperties);
 		addAgendaEventListener(ksession);
 		
 		try {
 			addUsersAndGroups();
 		} catch (NamingException e) {
 			StringWriter sw = new StringWriter();
 			e.printStackTrace(new PrintWriter(sw));
 			String stacktrace = sw.toString();
 			logger.error(stacktrace);
 		}
 	}
 	
 	private void addUsersAndGroups() throws NamingException
 	{
 		 Context ctx = jndiTemplate.getContext();
 		 UserTransaction ut = (UserTransaction)ctx.lookup( "java:comp/UserTransaction" );
 
 		 try
 		 {
 			ut.begin();
 			
 			TaskServiceSession ts = getHumanTaskManager().getLocalService().createSession();
 			
 			// Add users
 			Map vars = new HashMap();
 			Reader reader;
 
 			URL usersURL = BPMServerImpl.class.getClassLoader().getResource(
 					"LoadUsers.mvel");
 			reader = new FileReader(new File(usersURL.toURI()));
 
 			Map<String, User> users = (Map<String, User>) eval(reader, vars);
 			for (User user : users.values()) {
 				ts.addUser(user);
 			}
 
 			URL grpsURL = BPMServerImpl.class.getClassLoader().getResource(
 					"LoadGroups.mvel");
 			reader = new FileReader(new File(grpsURL.toURI()));
 
 			Map<String, Group> groups = (Map<String, Group>) eval(reader, vars);
 			for (Group group : groups.values()) {
 				ts.addGroup(group);
 			}
 			
 			
 			ut.commit();
 		} catch (Exception e) {
 			StringWriter sw = new StringWriter();
 			e.printStackTrace(new PrintWriter(sw));
 			String stacktrace = sw.toString();
 			logger.error(stacktrace);
 		}				
 	}
 	
 	@SuppressWarnings("rawtypes")
 	public static Object eval(Reader reader, Map vars) {
 		return TaskService.eval(reader, vars);
 	}
 	
 	private void stop() {
 		ksession.dispose();
 	}
 	
 
 	private KnowledgeBase loadKnowledgeBase(Properties jbpmProperties) {
 		KnowledgeBase kbase = null;
 		try {
 			GuvnorConnectionUtils guvnorUtils = new GuvnorConnectionUtils(jbpmProperties);
 			List<String> pkgs = guvnorUtils.getPackageNames();
 			boolean canBuild = false;
 			for (String pkgName : pkgs)
 			{
 				canBuild = guvnorUtils.canBuildPackage(pkgName);
 			}
 			
 			if (guvnorUtils.guvnorExists()) {
 				try {
 					ResourceChangeScannerConfiguration sconf = ResourceFactory
 							.getResourceChangeScannerService()
 							.newResourceChangeScannerConfiguration();
 					sconf.setProperty("drools.resource.scanner.interval", "10");
 					ResourceFactory.getResourceChangeScannerService().configure(
 							sconf);
 					ResourceFactory.getResourceChangeScannerService().start();
 					ResourceFactory.getResourceChangeNotifierService().start();
 					KnowledgeAgentConfiguration aconf = KnowledgeAgentFactory
 							.newKnowledgeAgentConfiguration();
 					aconf.setProperty("drools.agent.newInstance", "false");
 					kagent = KnowledgeAgentFactory.newKnowledgeAgent(
 							"Guvnor default", aconf);
 					StringReader changeSet = guvnorUtils.createChangeSet();
 					
 					String changeSetStr = changeSet.toString();
 					
 					kagent.applyChangeSet(ResourceFactory
 							.newReaderResource(changeSet));
 					kbase = kagent.getKnowledgeBase();
 
 					int processCount = kagent.getKnowledgeBase().getProcesses().size();
 					
 					if (processCount > 0)
 					{
 						boolean b = true;
 					}
 					//if (processCount == 0)
 					//	throw new IllegalStateException("0 processes were found in Guvnor");
 				}
 				catch (Throwable e)
 				{
 					StringWriter sw = new StringWriter();
 					e.printStackTrace(new PrintWriter(sw));
 					String stacktrace = sw.toString();
 					logger.error(stacktrace);
 					
 					throw new IllegalStateException("loadKnowledgeBase[Could not load processes from Guvnor]:\r\n"+stacktrace, e);
 				}						
 			} else {
 				logger.warn("Could not connect to Guvnor.");
 				
 				throw new IllegalStateException("Could not connect to Guvnor.");
 			}
 
 			// Create a kbase if we couldn't do that with Guvnor
 			if (kbase == null) {
 				kbase = KnowledgeBaseFactory.newKnowledgeBase();
 			}
 		} 
 		catch (Exception e)
 		{
 			StringWriter sw = new StringWriter();
 			e.printStackTrace(new PrintWriter(sw));
 			String stacktrace = sw.toString();
 			logger.error(stacktrace);
 			
 			throw new IllegalStateException("loadKnowledgeBase:\r\n"+stacktrace, e);
 		}	
 		catch (Error e)
 		{
 			StringWriter sw = new StringWriter();
 			e.printStackTrace(new PrintWriter(sw));
 			String stacktrace = sw.toString();
 			logger.error(stacktrace);
 			
 			throw new IllegalStateException("loadKnowledgeBase:\r\n"+stacktrace, e);			
 		}	
 
 		return kbase;
 	}
 
 	
 	protected Properties loadJbpmProperties() {
 		if (!jbpmProperties.isEmpty()) {
 			return jbpmProperties;
 		}
 		try {
 			jbpmProperties.load(BPMServerImpl.class
 					.getResourceAsStream("/jbpm.conxrepo.properties"));
 		} catch (IOException e) {
 			throw new RuntimeException(
 					"Could not load jbpm.conxrepo.properties", e);
 		}
 
 		return jbpmProperties;
 	}
 
 	private int getPersistedSessionId(String location) {
 		File sessionIdStore = new File(location + File.separator
 				+ "jbpmSessionId.ser");
 		if (sessionIdStore.exists()) {
 			Integer knownSessionId = null;
 			FileInputStream fis = null;
 			ObjectInputStream in = null;
 			try {
 				fis = new FileInputStream(sessionIdStore);
 				in = new ObjectInputStream(fis);
 
 				knownSessionId = (Integer) in.readObject();
 
 				return knownSessionId.intValue();
 
 			} catch (Exception e) {
 				return 0;
 			} finally {
 				if (fis != null) {
 					try {
 						fis.close();
 					} catch (IOException e) {
 					}
 				}
 				if (in != null) {
 					try {
 						in.close();
 					} catch (IOException e) {
 					}
 				}
 			}
 
 		} else {
 			return 0;
 		}
 	}
 
 	private StatefulKnowledgeSession createOrLoadStatefulKnowledgeSession(
 			KnowledgeBase kbase) {
 		// Set up persistence
 		Environment env = KnowledgeBaseFactory.newEnvironment();
 		env.set(EnvironmentName.ENTITY_MANAGER_FACTORY, jbpmEMF);
 
 		// Set up jbpm process instance settings
 		Properties sessionconfigproperties = new Properties();
 		sessionconfigproperties
 				.put("drools.processInstanceManagerFactory",
 						"org.jbpm.persistence.processinstance.JPAProcessInstanceManagerFactory");
 		sessionconfigproperties.put("drools.processSignalManagerFactory",
 				"org.jbpm.persistence.processinstance.JPASignalManagerFactory");
 		KnowledgeSessionConfiguration config = KnowledgeBaseFactory
 				.newKnowledgeSessionConfiguration(sessionconfigproperties);
 
 		boolean createNewKnowledgeSession = true;
 		StatefulKnowledgeSession ksession = null;
 
 		// Create or load knowledge session
 		if (ksessionId > 0) {
 			createNewKnowledgeSession = false;
 			try {
 				logger.debug("Loading knowledge session with id " + ksessionId);
 				ksession = JPAKnowledgeService.loadStatefulKnowledgeSession(
 						ksessionId, kbase, config, env);
 			} catch (RuntimeException e) {
 				e.printStackTrace();
 				logger.error("Error loading knowledge session : "
 						+ e.getMessage());
 				if (e instanceof IllegalStateException) {
 					Throwable cause = ((IllegalStateException) e).getCause();
 					if (cause instanceof InvocationTargetException) {
 						cause = cause.getCause();
 						String exceptionMsg = "Could not find session data for id "
 								+ ksessionId;
 						if (cause != null
 								&& exceptionMsg.equals(cause.getMessage())) {
 							createNewKnowledgeSession = true;
 						}
 					}
 				}
 
 				if (!createNewKnowledgeSession) {
 					String exceptionMsg = e.getMessage();
 					if (e.getCause() != null
 							&& !StringUtils.isEmpty(e.getCause().getMessage())) {
 						exceptionMsg = e.getCause().getMessage();
 					}
 					logger.error("Error loading session data: " + exceptionMsg);
 					throw e;
 				}
 			}
 		}
 
 		if (createNewKnowledgeSession) {
 			env = KnowledgeBaseFactory.newEnvironment();
 			env.set(EnvironmentName.ENTITY_MANAGER_FACTORY, jbpmEMF);
 			env.set(EnvironmentName.TRANSACTION_MANAGER, this.globalTransactionManager);
 			
 			
 			//-- Configure for JPA Entity Persistence support when saving ProcessInstance's with JPA Entities as variables
 	        Environment domainEnv = EnvironmentFactory.newEnvironment();
 	        domainEnv.set(EnvironmentName.ENTITY_MANAGER_FACTORY, conxlogisticsEMF);
 	        domainEnv.set(EnvironmentName.TRANSACTION_MANAGER, this.globalTransactionManager);
 /*	        env.set(EnvironmentName.OBJECT_MARSHALLING_STRATEGIES, new ObjectMarshallingStrategy[]{
 	                    new JPAPlaceholderResolverStrategy(domainEnv),
 	                    new SerializablePlaceholderResolverStrategy(ClassObjectMarshallingStrategyAcceptor.DEFAULT)
 	                });	*/	
 			 try {
 				 Context ctx = jndiTemplate.getContext();
 				 UserTransaction ut = (UserTransaction)ctx.lookup( "java:comp/UserTransaction" );
 				 env.set(EnvironmentName.TRANSACTION,ut);
 			} catch (Exception e) {
 				logger.error("Global JNDI Context Lookup failed: "+e.getMessage());
 			}			
 			
 			ksession = JPAKnowledgeService.newStatefulKnowledgeSession(kbase,
 					null, env);
 			ksessionId = ksession.getId();
 			logger.debug("Created new knowledge session with id " + ksessionId);
 		}
 
 		return ksession;
 	}
 
 	private void persistSessionId(String location) {
 		if (location == null) {
 			return;
 		}
 		FileOutputStream fos = null;
 		ObjectOutputStream out = null;
 		try {
 			fos = new FileOutputStream(location + File.separator
 					+ "jbpmSessionId.ser");
 			out = new ObjectOutputStream(fos);
 			out.writeObject(Integer.valueOf(ksessionId));
 			out.close();
 		} catch (IOException ex) {
 			logger.warn("Error when persisting known session id", ex);
 		} finally {
 			if (fos != null) {
 				try {
 					fos.close();
 				} catch (IOException e) {
 				}
 			}
 			if (out != null) {
 				try {
 					out.close();
 				} catch (IOException e) {
 				}
 			}
 		}
 	}
 	
 	private void registerWorkItemHandler( StatefulKnowledgeSession ksession, Properties consoleProperties ) { 
         //if ("Local".equalsIgnoreCase(consoleProperties.getProperty("jbpm.conxrepo.task.service.strategy", TaskClientFactory.DEFAULT_TASK_SERVICE_STRATEGY))) {
            
 		    LocalTaskService taskService = localHumanTaskServer.getLocalTaskService();
 		    /*
             this.handler = new SyncWSHumanTaskHandler(taskService, ksession);
             this.handler.setLocal(true);
             ksession.getWorkItemManager().registerWorkItemHandler("Human Task", handler);     
             this.handler.connect();*/
             hthandler = new GenericHTWorkItemHandler(ksession);
             hthandler.setClient(taskService);
             hthandler.setLocal(true);
             hthandler.setIpAddress("127.0.0.1");
             hthandler.setPort(9123);            
             hthandler.connect();
             ksession.getWorkItemManager().registerWorkItemHandler("Human Task", hthandler); 
             /*
             //} else  {
             CommandBasedWSHumanTaskHandler handler = new CommandBasedWSHumanTaskHandler(ksession);
             TaskClient client = TaskClientFactory.newAsyncInstance(consoleProperties, "org.drools.process.workitem.wsht.CommandBasedWSHumanTaskHandler");
             
             handler.configureClient(client);
             ksession.getWorkItemManager().registerWorkItemHandler("Human Task", handler);
             handler.connect();
        // }*/
         
     }
 	
 	private static void addAgendaEventListener(final StatefulKnowledgeSession ksession) { 
         final org.drools.event.AgendaEventListener agendaEventListener = new org.drools.event.AgendaEventListener() {
             public void activationCreated(ActivationCreatedEvent event, WorkingMemory workingMemory){
             	ksession.fireAllRules();
             }
             public void activationCancelled(ActivationCancelledEvent event, WorkingMemory workingMemory){
             }
             public void beforeActivationFired(BeforeActivationFiredEvent event, WorkingMemory workingMemory) {
             }
             public void afterActivationFired(AfterActivationFiredEvent event, WorkingMemory workingMemory) {
             }
             public void agendaGroupPopped(AgendaGroupPoppedEvent event, WorkingMemory workingMemory) {
             }
 
             public void agendaGroupPushed(AgendaGroupPushedEvent event, WorkingMemory workingMemory) {
             }
             public void beforeRuleFlowGroupActivated(RuleFlowGroupActivatedEvent event, WorkingMemory workingMemory) {
             }
             public void afterRuleFlowGroupActivated(RuleFlowGroupActivatedEvent event, WorkingMemory workingMemory) {
                 workingMemory.fireAllRules();
             }
             public void beforeRuleFlowGroupDeactivated(RuleFlowGroupDeactivatedEvent event, WorkingMemory workingMemory) {
             }
             public void afterRuleFlowGroupDeactivated(RuleFlowGroupDeactivatedEvent event, WorkingMemory workingMemory) {
             }
         };
         ((StatefulKnowledgeSessionImpl)  ((KnowledgeCommandContext) ((CommandBasedStatefulKnowledgeSession) ksession)
                 .getCommandService().getContext()).getStatefulKnowledgesession() )
                 .session.addEventListener(agendaEventListener);
     	ksession.fireAllRules();
 	}
 	
     public synchronized void checkPackagesFromGuvnor() {
         GuvnorConnectionUtils guvnorUtils = new GuvnorConnectionUtils();
         if(guvnorUtils.guvnorExists()) {
             List<String> guvnorPackages = guvnorUtils.getBuiltPackageNames();
             
             guvnorPackages.removeAll(knownPackages);
             
             if (guvnorPackages.size() > 0) {
                 kagent.applyChangeSet(ResourceFactory.newReaderResource(guvnorUtils.createChangeSet(guvnorPackages)));
                 knownPackages.addAll(guvnorPackages);
             }
         }
     }
     
     /**
      * DS registrations 
      */
 	public void registerWIH(
 			WorkItemHandler wih, Map properties) {
 		try {
 			String workItemId = (String)properties.get("TASK_NAME");
 			
 			ksession.getWorkItemManager().registerWorkItemHandler(workItemId, wih);
 			
 			String processId = (String)properties.get("PROCESS_ID");		
 			logger.debug("registerWIH("+processId+")");		
 			Map<String,WorkItemHandler> list = this.wihCache.get(processId);
 			if (list == null) {
 				list = new HashMap<String,WorkItemHandler>();
 				list.put(workItemId,wih);
 				wihCache.put(processId, list);
 			} else {
 				list.put(workItemId,wih);
 			}
 		} 
 		catch (Exception e)
 		{
 			StringWriter sw = new StringWriter();
 			e.printStackTrace(new PrintWriter(sw));
 			String stacktrace = sw.toString();
 			logger.error(stacktrace);
 		}	
 		catch (Error e)
 		{
 			StringWriter sw = new StringWriter();
 			e.printStackTrace(new PrintWriter(sw));
 			String stacktrace = sw.toString();
 			logger.error(stacktrace);			
 		}	
 
 	}
 
 	public void unregisterWIH(
 			WorkItemHandler wih, Map properties) {
 		try
 		{
 			String processId = (String)properties.get("PROCESS_ID");
 			String workItemId = (String)properties.get("TASK_NAME");
 			
 			logger.debug("unregisterWIH("+processId+")");	
 			Map<String,WorkItemHandler> list = this.wihCache.get(processId);
 			if (list != null) {
 				list.remove(workItemId);
 			}
 			
 			ksession.getWorkItemManager().registerWorkItemHandler(workItemId, null);			
 		}
 		catch (Exception e)
 		{
 			StringWriter sw = new StringWriter();
 			e.printStackTrace(new PrintWriter(sw));
 			String stacktrace = sw.toString();
 			logger.error(stacktrace);
 		}	
 		catch (Error e)
 		{
 			StringWriter sw = new StringWriter();
 			e.printStackTrace(new PrintWriter(sw));
 			String stacktrace = sw.toString();
 			logger.error(stacktrace);			
 		}			
 	}    
 
 	public CustomJPAProcessInstanceDbLog getJpaProcessInstanceDbLog() {
 		return jpaProcessInstanceDbLog;
 	}
 
 	/**
 	 * Process Management methods
 	 */
 	@Override
 	public List<ProcessDefinitionRef> getProcessDefinitions() {
 		return getProcessManager().getProcessDefinitions();
 	}
 
 	@Override
 	public ProcessDefinitionRef getProcessDefinition(String definitionId) {
 		return getProcessManager().getProcessDefinition(definitionId);
 	}
 
 	@Override
 	public List<ProcessDefinitionRef> removeProcessDefinition(
 			String definitionId) {
 		return getProcessManager().removeProcessDefinition(definitionId);
 	}
 
 	@Override
 	public List<ProcessInstanceRef> getProcessInstances(String definitionId) {
 		return getProcessManager().getProcessInstances(definitionId);
 	}
 
 	@Override
 	public ProcessInstanceRef newInstance(String definitionId) {
 		return getProcessManager().newInstance(definitionId);
 	}
 
 	@Override
 	public void setProcessState(String instanceId, STATE nextState) {
 		getProcessManager().setProcessState(instanceId, nextState);
 	}
 
 	@Override
 	public Map<String, Object> getInstanceData(String instanceId) {
 		return getProcessManager().getInstanceData(instanceId);
 	}
 
 	@Override
 	public void signalExecution(String executionId, String signal) {
 		getProcessManager().signalExecution(executionId, signal);
 	}
 
 	@Override
 	public void deleteInstance(String instanceId) {
 		getProcessManager().deleteInstance(instanceId);
 	}
 
 	@Override
 	public void endInstance(String instanceId, RESULT result) {
 		getProcessManager().endInstance(instanceId, result);
 	}
 
 	@Override
 	public ProcessInstanceRef getProcessInstance(String instanceId) {
 		return getProcessManager().getProcessInstance(instanceId);
 	}
 
 	@Override
 	public List<HumanTaskNode> getProcessHumanTaskNodes(String definitionId) {
 		return processGraphManager.getProcessHumanTaskNodes(definitionId);
 	}
 	
 	@Override
     public Map<String, Object> getProcessInstanceVariables(String processInstanceId)
     {
     	return getProcessManager().getProcessInstanceVariables(processInstanceId);
     }
 	
 	@Override
 	public Map<String, String> findVariableInstances(Long processInstanceId)
 	{
 		return getProcessManager().findVariableInstances(processInstanceId);
 	}
     
 	@Override
     public void setProcessInstanceVariables(final String processInstanceId, final Map<String, Object> variables)
     {
 		getProcessManager().setProcessInstanceVariables(processInstanceId, variables);
     }	
 	
 
 	@Override
 	public List<Node> getActiveNode(String instanceId) {
 		return processGraphManager.getActiveNode(instanceId);
 	}
 
 	public Properties getJbpmProperties() {
 		return jbpmProperties;
 	}
 
 	@Override
 	public TaskRef getTaskById(long taskId) {
 		return taskManager.getTaskById(taskId);
 	}
 	
 	@Override
 	public Task getTaskObjectById(long taskId) {
 		return taskManager.getTaskObjectById(taskId);
 	}	
 
 	@Override
 	public void assignTask(long taskId, String idRef, String userId) {
 		taskManager.assignTask(taskId, idRef, userId);
 		
 	}
 	
 	@Override
 	public void nominate(long taskId, String userId) {
 		User nominee = (User)getHumanTaskManager().getLocalService().createSession().getTaskPersistenceManager().findEntity(User.class, userId);
 		taskManager.nominateTask(taskId,nominee);
 	}	
 	
 	@Override
 	public void startTask(long taskId, String userId) {
 		taskManager.startTask(taskId, userId);
 	}
 
 	@Override
 	public void completeTask(long taskId, Map<String, Object> data, String userId) {
 		ContentData objectRes = ContentMarshallerHelper.marshal(data,((GenericHTWorkItemHandler)hthandler).getMarshallerContext(),ksession.getEnvironment());	
		taskManager.completeTask(taskId, objectRes, userId);
 	}
 
 	@Override
 	public void completeTask(long taskId, String outcome, Map<String, Object> data,
 			String userId) {
 		taskManager.completeTask(taskId, data, userId);
 	}
 	
 	@Override
 	public void completeTask(long taskId, ContentData contentData, String userId) {
 		taskManager.completeTask(taskId, contentData, userId);
 	}		
 
 	@Override
 	public void releaseTask(long taskId, String userId) {
 		taskManager.releaseTask(taskId, userId);
 	}
 
 	@Override
 	public List<TaskRef> getAssignedTasks(String idRef) {
 		return taskManager.getAssignedTasks(idRef);
 	}
 
 	@Override
 	public List<TaskRef> getUnassignedTasks(String idRef,
 			String participationType) {
 		return taskManager.getUnassignedTasks(idRef, participationType);
 	}
 
 	@Override
 	public void skipTask(long taskId, String userId) {
 		taskManager.skipTask(taskId, userId);
 	}
 
 	@Override
 	public ProcessInstanceRef newInstance(String definitionId,
 			Map<String, Object> processVars) {
 		return getProcessManager().newInstance(definitionId, processVars);
 	}
 
 	@Override
 	public void setInstanceData(String instanceId, Map<String, Object> data) {
 		getProcessManager().setInstanceData(instanceId, data);
 	}
 
 	public HumanTaskService getHumanTaskManager() {
 		return humanTaskManager;
 	}
 
 	@Override
 	public List<Task> getCreatedTasksByProcessId(Long processInstanceId) {
 		//return taskManager.getCreatedTasksByProcessId(processInstanceId);
 		//String query = "select new org.jbpm.task.query.TaskSummary( t.id, t.taskData.processInstanceId, name.text, subject.text, description.text, t.taskData.status, t.priority, t.taskData.skipable, t.taskData.actualOwner, t.taskData.createdBy, t.taskData.createdOn, t.taskData.activationTime, t.taskData.expirationTime, t.taskData.processId, t.taskData.processSessionId) from Task t  left join t.taskData.createdBy left join t.subjects as subject left join t.descriptions as description left join t.names as name where t.archived = 0 and t.taskData.status = :status and t.taskData.processInstanceId = :processId and ( name.language = :language or t.names.size = 0 ) and  ( subject.language = :language or t.subjects.size = 0 ) and  ( description.language = :language or t.descriptions.size = 0 ) and  t.taskData.expirationTime is null";
 		Query emQuery = getHumanTaskManager().getLocalService().createSession().getTaskPersistenceManager().createQuery("TasksByStatusAndProcessId");
 		emQuery.setParameter("status", org.jbpm.task.Status.Created);
 		emQuery.setParameter("processId", processInstanceId);
 		//emQuery.setParameter("language", "en-UK");
 		List<Task> result = emQuery.getResultList();
 		return result;
 	}
 	
 	
 	@Override
 	public List<Task> getReadyTasksByProcessId(Long processInstanceId) {
 		//String query = "select new org.jbpm.task.query.TaskSummary( t.id, t.taskData.processInstanceId, name.text, subject.text, description.text, t.taskData.status, t.priority, t.taskData.skipable, t.taskData.actualOwner, t.taskData.createdBy, t.taskData.createdOn, t.taskData.activationTime, t.taskData.expirationTime, t.taskData.processId, t.taskData.processSessionId) from Task t  left join t.taskData.createdBy left join t.subjects as subject left join t.descriptions as description left join t.names as name where t.archived = 0 and t.taskData.status = :status and t.taskData.processInstanceId = :processId and ( name.language = :language or t.names.size = 0 ) and  ( subject.language = :language or t.subjects.size = 0 ) and  ( description.language = :language or t.descriptions.size = 0 ) and  t.taskData.expirationTime is null";
 		Query emQuery = getHumanTaskManager().getLocalService().createSession().getTaskPersistenceManager().createQuery("TasksByStatusAndProcessId");
 		emQuery.setParameter("status", org.jbpm.task.Status.Ready);
 		emQuery.setParameter("processId", processInstanceId);
 		//emQuery.setParameter("language", "en-UK");
 		List<Task> result = emQuery.getResultList();
 		return result;
 	}
 
 	@Override
 	public List<Task> getReservedTasksByProcessId(Long processInstanceId) {
 		//String query = "select new org.jbpm.task.query.TaskSummary( t.id, t.taskData.processInstanceId, name.text, subject.text, description.text, t.taskData.status, t.priority, t.taskData.skipable, t.taskData.actualOwner, t.taskData.createdBy, t.taskData.createdOn, t.taskData.activationTime, t.taskData.expirationTime, t.taskData.processId, t.taskData.processSessionId) from Task t  left join t.taskData.createdBy left join t.subjects as subject left join t.descriptions as description left join t.names as name where t.archived = 0 and t.taskData.status = :status and t.taskData.processInstanceId = :processId and ( name.language = :language or t.names.size = 0 ) and  ( subject.language = :language or t.subjects.size = 0 ) and  ( description.language = :language or t.descriptions.size = 0 ) and  t.taskData.expirationTime is null";
 		Query emQuery = getHumanTaskManager().getLocalService().createSession().getTaskPersistenceManager().createQuery("TasksByStatusAndProcessId");
 		emQuery.setParameter("status", org.jbpm.task.Status.Reserved);
 		emQuery.setParameter("processId", processInstanceId);
 		//mQuery.setParameter("language", "en-UK");
 		List<Task> result = emQuery.getResultList();
 		return result;
 	}
 
 	@Override
 	public List<Task> getReadyAndReservedTasksByProcessId(
 			Long processInstanceId) {
 		String query = "select new org.jbpm.task.query.TaskSummary( t.id, t.taskData.processInstanceId, name.text, subject.text, description.text, t.taskData.status, t.priority, t.taskData.skipable, t.taskData.actualOwner, t.taskData.createdBy, t.taskData.createdOn, t.taskData.activationTime, t.taskData.expirationTime, t.taskData.processId, t.taskData.processSessionId) from Task t  left join t.taskData.createdBy left join t.subjects as subject left join t.descriptions as description left join t.names as name where t.archived = 0 and t.taskData.status in :statusList and t.taskData.processInstanceId = :processId and ( name.language = :language or t.names.size = 0 ) and  ( subject.language = :language or t.subjects.size = 0 ) and  ( description.language = :language or t.descriptions.size = 0 ) and  t.taskData.expirationTime is null";
 		Query emQuery = getHumanTaskManager().getLocalService().createSession().getTaskPersistenceManager().createNewQuery(query);
 		ArrayList<org.jbpm.task.Status> statusList = new ArrayList<org.jbpm.task.Status>();
 		statusList.add(org.jbpm.task.Status.Ready);
 		statusList.add(org.jbpm.task.Status.Reserved);
 		emQuery.setParameter("statusList", statusList);
 		emQuery.setParameter("processId", processInstanceId);
 		//emQuery.setParameter("language", "en-UK");
 		List<Task> result = emQuery.getResultList();
 		return result;
 	}
 	
 	@Override
 	public Content getTaskContent(long taskId) {
 		return taskManager.getTaskContent(taskId);
 	}	
 	
 	@Override
     public Object getTaskContentObject(Task task) throws IOException, ClassNotFoundException{
         Content content = taskManager.getTaskContent(task.getTaskData().getDocumentContentId());
         Object readObject = 
                 ContentMarshallerHelper.unmarshall(task.getTaskData().getDocumentType(), 
                                                             content.getContent(), 
                                                             ((GenericHTWorkItemHandler)hthandler).getMarshallerContext(),  
                                                             ksession.getEnvironment());
         /*
         ByteArrayInputStream bais = new ByteArrayInputStream(content.getContent());
         
         ObjectInputStream ois = new ObjectInputStream(bais);
         Object readObject = ois.readObject();    
         ois.close();
 		*/
  
         logger.info(" >>> Object = "+readObject);
         return readObject;
     }		
 }
