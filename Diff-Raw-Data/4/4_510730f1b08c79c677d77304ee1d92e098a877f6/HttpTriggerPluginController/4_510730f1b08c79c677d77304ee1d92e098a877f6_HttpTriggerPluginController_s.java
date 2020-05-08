 /**
  * 
  */
 package net.frontlinesms.plugins.httptrigger;
 
 import org.springframework.context.ApplicationContext;
 
 import net.frontlinesms.FrontlineSMS;
 import net.frontlinesms.plugins.BasePluginController;
 import net.frontlinesms.plugins.PluginControllerProperties;
 import net.frontlinesms.plugins.PluginInitialisationException;
 import net.frontlinesms.plugins.PluginSettingsController;
 import net.frontlinesms.plugins.httptrigger.httplistener.GroovyUrlRequestHandler;
 import net.frontlinesms.plugins.httptrigger.httplistener.HttpTriggerServer;
 import net.frontlinesms.plugins.httptrigger.httplistener.UrlMapper;
 import net.frontlinesms.ui.UiGeneratorController;
 import net.frontlinesms.ui.i18n.InternationalisationUtils;
 
 /**
  * This plugin controls an HTTP listener for triggering SMS from outside FrontlineSMS.
  * @author Alex
  */
@PluginControllerProperties(name="HTTP Trigger", i18nKey="plugins.httptrigger.name", iconPath="/icons/import.png", 
		springConfigLocation="classpath:net/frontlinesms/plugins/httptrigger/httptrigger-spring-hibernate.xml",
		hibernateConfigPath=PluginControllerProperties.NO_VALUE)
 public class HttpTriggerPluginController extends BasePluginController implements HttpTriggerEventListener {
 //> STATIC CONSTANTS
 	/** Filename and path of the XML for the HTTP Trigger tab. */
 	private static final String UI_FILE_TAB = "/ui/plugins/httptrigger/httpTriggerTab.xml";
 
 //> INSTANCE PROPERTIES
 	/** The {@link HttpTriggerListener} that is currently running.  This property will be <code>null</code> if no listener is running. */
 	private HttpTriggerListener httpListener;
 	/** Thinlet tab controller for this plugin */
 	private HttpTriggerThinletTabController tabController;
 	/** the {@link FrontlineSMS} instance that this plugin is attached to */
 	private FrontlineSMS frontlineController;
 	private GroovyUrlRequestHandler groovyUrlRequestHandler;
 	private final String I18N_SENDING_TO = "plugins.httptrigger.sending.to";
 	private final String I18N_LISTENER_STOPPING = "plugins.httptrigger.listener.stopping";
 
 //> CONSTRUCTORS
 
 //> ACCESSORS
 	/** @see net.frontlinesms.plugins.PluginController#getTab(net.frontlinesms.ui.UiGeneratorController) */
 	public Object initThinletTab(UiGeneratorController uiController) {
 		this.tabController = new HttpTriggerThinletTabController(this, uiController);
 		
 		
 		Object httpTriggerTab = uiController.loadComponentFromFile(UI_FILE_TAB, tabController);
 		tabController.setTabComponent(httpTriggerTab);
 		tabController.initFields();
 		
 		if(HttpTriggerProperties.getInstance().isAutostart()) {
 			// Start the listener here so that all fields are updated properly.
 			// Starting here is little different from starting in init() with the
 			// current plugin lifecycle - the plugin is only enabled when visible.
 			this.startListener();
 			tabController.enableFields(true);
 		}
 		
 		return httpTriggerTab;
 	}
 
 	/** @see net.frontlinesms.plugins.PluginController#init(net.frontlinesms.FrontlineSMS, org.springframework.context.ApplicationContext) */
 	public void init(FrontlineSMS frontlineController, ApplicationContext applicationContext) throws PluginInitialisationException {
 		this.frontlineController = frontlineController;
 
 		UrlMapper urlMapper = UrlMapper.create(); // TODO get paths from config file
 
 		this.groovyUrlRequestHandler = new GroovyUrlRequestHandler(frontlineController, urlMapper);
 	}
 	
 	/** @see net.frontlinesms.plugins.PluginController#deinit() */
 	public void deinit() {
 		this.stopListener();
 	}
 
 	/**
 	 * Start the HTTP listener.  If there is another listener already running, it will be stopped. 
 	 */
 	public void startListener() {
 		this.stopListener();
 		int portNumber = HttpTriggerProperties.getInstance().getListenPort();
 		this.httpListener = new HttpTriggerServer(this, groovyUrlRequestHandler, portNumber);
 		this.httpListener.start();
 	}
 
 	/** Stop the {@link #httpListener} if it is runnning. */
 	public void stopListener() {
 		if(this.httpListener != null) {
 			this.httpListener.pleaseStop();
 			this.log(InternationalisationUtils.getI18NString(I18N_LISTENER_STOPPING, this.httpListener.toString()));
 			this.httpListener = null;
 		}
 	}
 	
 	public boolean isRunning() {
 		return this.httpListener != null;
 	}
 
 //> INSTANCE HELPER METHODS
 	
 //> HTEL METHODS
 	/** @see HttpTriggerEventListener#log(String) */
 	public void log(String message) {
 		if(this.tabController != null) {
 			this.tabController.log(message);
 		}
 		this.log.trace(message);
 	}
 	
 	/** @see net.frontlinesms.plugins.httptrigger.HttpTriggerEventListener#sendSms(java.lang.String, java.lang.String) */
 	public void sendSms(String toPhoneNumber, String message) {
 		this.log(InternationalisationUtils.getI18NString(I18N_SENDING_TO, toPhoneNumber, message));
 		frontlineController.sendTextMessage(toPhoneNumber, message);
 	}
 
 //> STATIC FACTORIES
 
 //> STATIC HELPER METHODS
 
 	public PluginSettingsController getSettingsController(UiGeneratorController uiController) {
 		return new HttpTriggerSettingsController(this, uiController, getIcon(this.getClass()));
 	}
 }
