 package com.conx.logistics.kernel.pageflow.engine;
 
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.naming.Context;
 import javax.persistence.EntityManagerFactory;
 import javax.transaction.UserTransaction;
 
 import org.jboss.bpm.console.client.model.ProcessInstanceRef;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.jndi.JndiTemplate;
 import org.springframework.transaction.PlatformTransactionManager;
 import org.vaadin.mvp.eventbus.EventBus;
 import org.vaadin.mvp.presenter.IPresenter;
 
 import com.conx.logistics.kernel.bpm.services.IBPMService;
 import com.conx.logistics.kernel.pageflow.engine.ui.TaskWizard;
 import com.conx.logistics.kernel.pageflow.services.IPageFlowManager;
 import com.conx.logistics.kernel.pageflow.services.IPageFlowSession;
 import com.conx.logistics.kernel.pageflow.services.ITaskWizard;
 import com.conx.logistics.kernel.pageflow.services.PageFlowPage;
 import com.conx.logistics.kernel.ui.service.contribution.IMainApplication;
 import com.conx.logistics.mdm.domain.application.Feature;
 import com.conx.logistics.mdm.domain.task.TaskDefinition;
 
 public class PageFlowEngineImpl implements IPageFlowManager {
 
 	protected Logger logger = LoggerFactory.getLogger(this.getClass());
 	private IBPMService bpmService;
 	private EntityManagerFactory conxlogisticsEMF;
 	private PlatformTransactionManager globalTransactionManager;
 	private List<IPageFlowSession> sessions;
 
 	private JndiTemplate jndiTemplate;
 	private UserTransaction userTransaction;
 
 	public PageFlowEngineImpl() {
 		this.sessions = new ArrayList<IPageFlowSession>();
 	}
 
 	public void setJndiTemplate(JndiTemplate jndiTemplate) {
 		this.jndiTemplate = jndiTemplate;
 	}
 	
 
 	public void setUserTransaction(UserTransaction userTransaction) {
 		this.userTransaction = userTransaction;
 	}
 
 	@SuppressWarnings("unused")
 	public void setBpmService(IBPMService bpmService) {
 		try {
 			this.bpmService = bpmService;
 			//bpmService.startNewProcess("","");
 			System.out.println("Test");
 		} 
 		catch (Exception e)
 		{
 			StringWriter sw = new StringWriter();
 			e.printStackTrace(new PrintWriter(sw));
 			String stacktrace = sw.toString();
 		}	
 		catch (Error e)
 		{
 			StringWriter sw = new StringWriter();
 			e.printStackTrace(new PrintWriter(sw));
 			String stacktrace = sw.toString();
 		}		
 	}
 
 	public void start() {
 		try 
 		{
			ProcessInstanceRef pi = bpmService.newInstance("defaultPackage.goat1");
 		} catch (Exception e) {
 			e.printStackTrace();
 		}		
 
 	}
 
 	public void stop() {
 	}
 
 	/** EntityManagerFactories */
 	private final Map<String, Map<String,PageFlowPage>> pageCache = Collections
 			.synchronizedMap(new HashMap<String, Map<String,PageFlowPage>>());
 	private IMainApplication mainApp;	
 	
 
 	public IMainApplication getMainApp() {
 		return mainApp;
 	}
 
 	private Map<String,PageFlowPage> getPages(String processId) {
 		return pageCache.get(processId);
 	}
 
 	@Override
 	public IPageFlowSession closePageFlowSession(String userid,
 			Long pageFlowSessionId) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public IPageFlowSession resumePageFlowSession(String userid,
 			Long pageFlowSessionId) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public void registerPageFlowPage(
 			PageFlowPage page, Map<String, Object> properties) {
 		String processId = (String)properties.get(PageFlowPage.PROCESS_ID);
 		String taskName = (String)properties.get(PageFlowPage.TASK_NAME);
 		logger.debug("registerPageFlowPage("+processId+","+taskName+")");		
 		Map<String,PageFlowPage> map = this.pageCache.get(processId);
 		if (map == null) {
 			map = new HashMap<String,PageFlowPage>();
 			pageCache.put(processId, map);
 		} 
 		map.put(taskName,page);	
 	}
 
 	public void unregisterPageFlowPage(
 			PageFlowPage page, Map<String, Object> properties) {
 		String processId = (String)properties.get(PageFlowPage.PROCESS_ID);
 		logger.debug("unregisterPageFlowPage("+processId+")");	
 		Map<String,PageFlowPage> map = this.pageCache.get(processId);
 		String taskName = (String)properties.get(PageFlowPage.TASK_NAME);
 		if (map != null) {
 			map.remove(taskName);
 		}
 	}
 
 	@Override
 	public IPageFlowSession startPageFlowSession(String userId, TaskDefinition td) {
 		//		ProcessInstanceRef pi = bpmService.newInstance(td.getProcessId());
 		//IPageFlowSession session = new PageFlowSessionImpl(null, userId, getPages(td.getProcessId()), this.bpmService, conxlogisticsEMF);
 		//sessions.add(session);
 		return null;
 	}
 
 	public EntityManagerFactory getConxlogisticsEMF() {
 		return conxlogisticsEMF;
 	}
 
 	public void setConxlogisticsEMF(EntityManagerFactory conxlogisticsEMF) {
 		this.conxlogisticsEMF = conxlogisticsEMF;
 	}
 
 	public PlatformTransactionManager getGlobalTransactionManager() {
 		return globalTransactionManager;
 	}
 
 	public void setGlobalTransactionManager(
 			PlatformTransactionManager globalTransactionManager) {
 		this.globalTransactionManager = globalTransactionManager;
 	}
 
 	@Override
 	public ITaskWizard createTaskWizard(Map<String, Object> properties) throws Exception {
 		ProcessInstanceRef pi = null;
 		PageFlowSessionImpl session = null;
 
 		String processId = (String)properties.get("processId");
 		String userId = (String)properties.get("userId");
 		Feature   onCompletionCompletionFeature = (Feature)properties.get("onCompletionFeature");
 		IPresenter<?, ? extends EventBus>   onCompletionCompletionViewPresenter = (IPresenter<?, ? extends EventBus>)properties.get("onCompletionViewPresenter");
 		
 
 		Context ctx = jndiTemplate.getContext();
 		UserTransaction ut = this.userTransaction;//(UserTransaction)ctx.lookup( "java:comp/UserTransaction" );
 
 		// 1. Create process instance		
 		try
 		{
 			ut.begin();
 
 			pi = bpmService.newInstance(processId);
 
 			ut.commit();
 		}
 		catch(Exception e)
 		{
 			StringWriter sw = new StringWriter();
 			e.printStackTrace(new PrintWriter(sw));
 			String stacktrace = sw.toString();
 			logger.error(stacktrace);				 
 			ut.rollback();
 			throw e;
 		}
 
 		// 2. Create session
 		try
 		{
 			ut.begin();
 
 			Map<String,PageFlowPage> pageList = pageCache.get(processId);
 			session = new PageFlowSessionImpl(pi, userId, pageList, this.bpmService, conxlogisticsEMF, onCompletionCompletionFeature);
 			sessions.add(session);	
 
 			ut.commit();
 		}
 		catch(Exception e)
 		{
 			ut.rollback();
 			throw e;
 		}		 
 
 		// 3. Start first task in process
 		try
 		{
 			ut.begin();
 
 			session.start();
 
 			ut.commit();
 		}
 		catch(Exception e)
 		{
 			ut.rollback();
 			throw e;
 		}			
 		
 		// 4. Create wizard
 		TaskWizard wizard = new TaskWizard(session, this,onCompletionCompletionFeature,onCompletionCompletionViewPresenter);
 		wizard.setSizeFull();
 		if (session.getOrderedPageList() != null) {
 			for (PageFlowPage page : session.getOrderedPageList()) {
 				page.initialize(session.getEmf(), globalTransactionManager);
 				wizard.addStep(page);
 			}
 		}
 		/*
 		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
 		def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
 		TransactionStatus status = this.globalTransactionManager.getTransaction(def);
 		try {
 			String processId = (String)properties.get("processId");
 			String userId = (String)properties.get("userId");
 
 
 			pi = bpmService.newInstance(processId);
 
 			this.globalTransactionManager.commit(status);
 
 			Map<String,IPageFlowPage> pageList = pageCache.get(processId);
 
 			session = new PageFlowSessionImpl(pi, userId, pageList, this.bpmService, conxlogisticsEMF);
 			sessions.add(session);
 
 			//TaskWizard tw = new TaskWizard(session);
 		}
 		catch (Exception ex) {
 			this.globalTransactionManager.rollback(status);
 			throw ex;
 		}
 		 */		
 
 		return wizard;
 	}
 
 	@Override
 	public ITaskWizard executeTaskWizard(ITaskWizard tw, Object data) throws Exception {
 		//Context ctx = jndiTemplate.getContext();
 		//UserTransaction ut = (UserTransaction)ctx.lookup( "java:comp/UserTransaction" );
 		if (((TaskWizard)tw).currentStepIsLastStep())
 		{
 			((TaskWizard)tw).getSession().completeProcess(this.userTransaction,data);
 		}
 		else
 		{
 			((TaskWizard)tw).getSession().executeNext(this.userTransaction,data);
 		}
 		return tw;
 	}
 
 	@Override
 	public void setMainApplication(IMainApplication mainApp) {
 		this.mainApp = mainApp;
 	}
 }
