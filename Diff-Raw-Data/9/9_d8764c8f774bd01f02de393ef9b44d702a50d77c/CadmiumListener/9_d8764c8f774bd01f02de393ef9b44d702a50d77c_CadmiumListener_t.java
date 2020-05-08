 package com.meltmedia.cadmium.servlets.guice;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 import javax.servlet.ServletContext;
 import javax.servlet.ServletContextEvent;
 
 import org.apache.commons.io.IOUtils;
 import org.jgroups.JChannel;
 import org.jgroups.MembershipListener;
 import org.jgroups.MessageListener;
 import org.jgroups.Receiver;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ch.qos.logback.classic.LoggerContext;
 import ch.qos.logback.classic.joran.JoranConfigurator;
 import ch.qos.logback.core.joran.spi.JoranException;
 import ch.qos.logback.core.util.StatusPrinter;
 
 import com.google.inject.AbstractModule;
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 import com.google.inject.Module;
 import com.google.inject.Scopes;
 import com.google.inject.TypeLiteral;
 import com.google.inject.name.Names;
 import com.google.inject.servlet.GuiceServletContextListener;
 import com.google.inject.servlet.ServletModule;
 import com.meltmedia.cadmium.core.CommandAction;
 import com.meltmedia.cadmium.core.ContentService;
 import com.meltmedia.cadmium.core.CoordinatedWorker;
 import com.meltmedia.cadmium.core.FileSystemManager;
 import com.meltmedia.cadmium.core.SiteDownService;
 import com.meltmedia.cadmium.core.commands.CommandMapProvider;
 import com.meltmedia.cadmium.core.commands.CommandResponse;
 import com.meltmedia.cadmium.core.commands.CurrentStateCommandAction;
 import com.meltmedia.cadmium.core.commands.MaintenanceCommandAction;
 import com.meltmedia.cadmium.core.commands.HistoryRequestCommandAction;
 import com.meltmedia.cadmium.core.commands.HistoryResponseCommandAction;
 import com.meltmedia.cadmium.core.commands.StateUpdateCommandAction;
 import com.meltmedia.cadmium.core.commands.SyncCommandAction;
 import com.meltmedia.cadmium.core.commands.UpdateCommandAction;
 import com.meltmedia.cadmium.core.commands.UpdateDoneCommandAction;
 import com.meltmedia.cadmium.core.commands.UpdateFailedCommandAction;
 import com.meltmedia.cadmium.core.git.GitService;
 import com.meltmedia.cadmium.core.history.HistoryManager;
 import com.meltmedia.cadmium.core.lifecycle.LifecycleService;
 import com.meltmedia.cadmium.core.messaging.ChannelMember;
 import com.meltmedia.cadmium.core.messaging.MembershipTracker;
 import com.meltmedia.cadmium.core.messaging.MessageReceiver;
 import com.meltmedia.cadmium.core.messaging.MessageSender;
 import com.meltmedia.cadmium.core.messaging.ProtocolMessage;
 import com.meltmedia.cadmium.core.messaging.jgroups.JChannelProvider;
 import com.meltmedia.cadmium.core.messaging.jgroups.JGroupsMessageSender;
 import com.meltmedia.cadmium.core.messaging.jgroups.MultiClassReceiver;
 import com.meltmedia.cadmium.core.meta.ConfigProcessor;
 import com.meltmedia.cadmium.core.meta.MetaConfigProvider;
 import com.meltmedia.cadmium.core.meta.MimeTypeConfigProcessor;
 import com.meltmedia.cadmium.core.meta.RedirectConfigProcessor;
 import com.meltmedia.cadmium.core.meta.SiteConfigProcessor;
 import com.meltmedia.cadmium.core.meta.SslRedirectConfigProcessor;
 import com.meltmedia.cadmium.core.worker.CoordinatedWorkerImpl;
 import com.meltmedia.cadmium.email.jersey.EmailService;
 import com.meltmedia.cadmium.search.guice.SearchModule;
 import com.meltmedia.cadmium.servlets.ErrorPageFilter;
 import com.meltmedia.cadmium.servlets.FileServlet;
 import com.meltmedia.cadmium.servlets.MaintenanceFilter;
 import com.meltmedia.cadmium.servlets.RedirectFilter;
 import com.meltmedia.cadmium.servlets.SslRedirectFilter;
 import com.meltmedia.cadmium.servlets.jersey.MaintenanceService;
 import com.meltmedia.cadmium.servlets.jersey.HistoryService;
 import com.meltmedia.cadmium.servlets.jersey.StatusService;
 import com.meltmedia.cadmium.servlets.jersey.UpdateService;
 import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
 
 import com.meltmedia.cadmium.vault.guice.VaultModule;
 import com.meltmedia.cadmium.vault.service.VaultConstants;
 
 /**
  * Builds the context with the Guice framework. To see how this works, go to:
  * http://code.google.com/p/google-guice/wiki/ServletModule
  * 
  * @author Christian Trimble
  */
 
 public class CadmiumListener extends GuiceServletContextListener {
   private final Logger log = LoggerFactory.getLogger(getClass());
 
   public static final String CONFIG_PROPERTIES_FILE = "config.properties";
   public static final String BASE_PATH_ENV = "com.meltmedia.cadmium.contentRoot";
   public static final String SSH_PATH_ENV = "com.meltmedia.cadmium.github.sshKey";
   public static final String LAST_UPDATED_DIR = "com.meltmedia.cadmium.lastUpdated";
   public static final String JGROUPS_CHANNEL_CONFIG_URL = "com.meltmedia.cadmium.jgroups.channel.config";
   public static final String SSL_HEADER = "REQUEST_IS_SSL";
   public File sharedContentRoot;
   public File applicationContentRoot;
   private String repoDir = "git-checkout";
   private String contentDir = "renderedContent";
   private File sshDir;
   private List<ChannelMember> members;
   private String warName;
   private String repoUri;
   private String channelConfigUrl;
   private MaintenanceFilter maintenanceFilter;
   
   // Email config
   private String mailJNDIName;
   private String mailSessionStrategy;
   private String mailMessageTransformer;
   
   private ServletContext context;
 
   private Injector injector = null;
   
   private Properties epsilonProperties;
 
   @Override
   public void contextDestroyed(ServletContextEvent event) {
     try {
       JChannel channel = injector.getInstance(JChannel.class);
       if (channel != null) {
         try {
           channel.close();
         } catch (Exception e) {
           log.warn("Failed to close jgroups channel", e);
         }
       }
     } catch (Exception e) {
       log.warn("Failed to get channel", e);
     }
     try {
       GitService git = injector.getInstance(GitService.class);
       if (git != null) {
         try {
           git.close();
         } catch (Exception e) {
           log.warn("Failed to close GitService", e);
         }
       }
     } catch (Exception e) {
       log.warn("Failed to get git service", e);
     }
     super.contextDestroyed(event);
   }
 
   @Override
   public void contextInitialized(ServletContextEvent servletContextEvent) {
     maintenanceFilter = new MaintenanceFilter();
     context = servletContextEvent.getServletContext();
     Properties cadmiumProperties = loadProperties(new Properties(), context, "/WEB-INF/cadmium.properties", log);
     
     //Epsilon Props
     epsilonProperties = new Properties();
     String epsilonPropsFile = servletContextEvent.getServletContext().getRealPath("/WEB-INF/epsilon.properties");
     if(FileSystemManager.canRead(epsilonPropsFile)){
       FileReader reader = null;
       try{
         reader = new FileReader(epsilonPropsFile);
         epsilonProperties.load(reader);
       } catch(Exception e) {
         log.warn("Failed to load epsilon.properties file");
       } finally {
         if(reader != null) {
           try{
             reader.close();
           } catch(Exception e){}
         }
       }
     }
     
     
     Properties configProperties = new Properties();
     configProperties.putAll(System.getenv());
     configProperties.putAll(System.getProperties());
 
     sharedContentRoot = sharedContextRoot(configProperties, context, log);
 
     // compute the directory for this application, based on the war name.
     warName = getWarName(context);
     
     applicationContentRoot = applicationContentRoot(sharedContentRoot, warName, log);
     
     loadProperties(configProperties, new File(applicationContentRoot, CONFIG_PROPERTIES_FILE), log);
 
     if ((sshDir = getSshDir(configProperties, sharedContentRoot )) != null) {
       GitService.setupSsh(sshDir.getAbsolutePath());
     }
     
     repoUri = cadmiumProperties.getProperty("com.meltmedia.cadmium.git.uri");
     String branch = cadmiumProperties.getProperty("com.meltmedia.cadmium.branch");
     mailJNDIName = cadmiumProperties.getProperty("com.meltmedia.email.jndi");
     mailMessageTransformer = cadmiumProperties.getProperty("melt.mail.messagetransformer");
     mailSessionStrategy = cadmiumProperties.getProperty("melt.mail.sessionstrategy");
     
     if(repoUri != null && branch != null) {
       GitService cloned = null;
       try {
         cloned = GitService.initializeContentDirectory(repoUri, branch, this.sharedContentRoot.getAbsolutePath(), warName);
       } catch(Exception e) {
         throw new RuntimeException(e);
       } finally {
         IOUtils.closeQuietly(cloned);
       }
     }
 
     String repoDir = servletContextEvent.getServletContext().getInitParameter("repoDir");
     if (repoDir != null && repoDir.trim().length() > 0) {
       this.repoDir = repoDir;
     }
     String contentDir = servletContextEvent.getServletContext()
         .getInitParameter("contentDir");
     if (contentDir != null && contentDir.trim().length() > 0) {
       this.contentDir = contentDir;
     }
     if (configProperties.containsKey(LAST_UPDATED_DIR)) {
       File cntDir = new File(configProperties.getProperty(LAST_UPDATED_DIR));
       if (cntDir.exists() && cntDir.canRead()) {
         this.contentDir = cntDir.getName();
       }
     }
     File repoFile = new File(this.applicationContentRoot, this.repoDir);
     if (repoFile.isDirectory() && repoFile.canWrite()) {
       this.repoDir = repoFile.getAbsoluteFile().getAbsolutePath();
     }
 
     File contentFile = new File(this.applicationContentRoot, this.contentDir);
     if (contentFile.exists() && contentFile.isDirectory()
         && contentFile.canWrite()) {
       this.contentDir = contentFile.getAbsoluteFile().getAbsolutePath();
     } else {
       log.warn("The content directory exists, but we cannot write to it.");
       this.contentDir = contentFile.getAbsoluteFile().getAbsolutePath();
     }
     
     String channelCfgUrl = System.getProperty(JGROUPS_CHANNEL_CONFIG_URL);
     if(channelCfgUrl != null) {
       File channelCfgFile = null;
       URL fileUrl = null;
       try {
         fileUrl = new URL(channelCfgUrl);
       } catch(Exception e) {
         channelCfgFile = new File(channelCfgUrl);
       }
       if(fileUrl == null && channelCfgFile != null) {
         if(!channelCfgFile.isAbsolute() && !channelCfgFile.exists()) {
           channelCfgFile = new File(this.sharedContentRoot, channelCfgUrl);
           if(channelCfgFile.exists()) {
             this.channelConfigUrl = "file://" + channelCfgFile.getAbsoluteFile().getAbsolutePath();
           }
         } else {
           this.channelConfigUrl = "file://" + channelCfgFile.getAbsoluteFile().getAbsolutePath();
         }
       } else if(fileUrl != null) {
         this.channelConfigUrl = fileUrl.toString();
       }
     }
 
     injector = Guice.createInjector(createServletModule(), createModule());
     maintenanceFilter.stop();
     super.contextInitialized(servletContextEvent);
   }
 
   @Override
   protected Injector getInjector() {
     return injector;
   }
 
   private ServletModule createServletModule() {
     return new ServletModule() {
       @Override
       protected void configureServlets() {
         Map<String, String> maintParams = new HashMap<String, String>();
         maintParams.put("ignorePrefix", "/system");
         
         Map<String, String> fileParams = new HashMap<String, String>();
         fileParams.put("basePath", contentDir);
 
         serve("/system/*", "/api/*").with(GuiceContainer.class);
 
         serve("/*").with(FileServlet.class, fileParams);
 
         filter("/*").through(maintenanceFilter, maintParams);
         filter("/*").through(ErrorPageFilter.class, maintParams);
         filter("/*").through(RedirectFilter.class);
         filter("/*").through(SslRedirectFilter.class);
         
       }
     };
   }
   
   private Module createModule() {
     return new AbstractModule() {
       @Override
       protected void configure() {
         Properties configProperties = new Properties();
         configProperties.putAll(System.getenv());
         configProperties.putAll(System.getProperties());
 
         if (new File(applicationContentRoot, CONFIG_PROPERTIES_FILE).exists()) {
           try {
             configProperties.load(new FileReader(new File(
                 applicationContentRoot, CONFIG_PROPERTIES_FILE)));
           } catch (Exception e) {
             log.warn("Failed to load properties file ["
                 + CONFIG_PROPERTIES_FILE + "] from content directory.", e);
           }
         }
 
 
         bind(MaintenanceFilter.class).toInstance(maintenanceFilter);
         bind(SiteDownService.class).toInstance(maintenanceFilter);
 
         bind(FileServlet.class).in(Scopes.SINGLETON);
         bind(ContentService.class).to(FileServlet.class);
 
         bind(MessageSender.class).to(JGroupsMessageSender.class);
 
         try {
           bind(GitService.class).toInstance(
               GitService.createGitService(repoDir));
         } catch (Exception e) {
           throw new Error("Failed to bind git service");
         }
 
         members = Collections.synchronizedList(new ArrayList<ChannelMember>());
         bind(new TypeLiteral<List<ChannelMember>>() {
         }).annotatedWith(Names.named("members")).toInstance(members);
 
         bind(CommandAction.class)
             .annotatedWith(Names.named(ProtocolMessage.CURRENT_STATE.name()))
             .to(CurrentStateCommandAction.class).in(Scopes.SINGLETON);
         bind(CommandAction.class)
             .annotatedWith(Names.named(ProtocolMessage.STATE_UPDATE.name()))
             .to(StateUpdateCommandAction.class).in(Scopes.SINGLETON);
         bind(CommandAction.class)
             .annotatedWith(Names.named(ProtocolMessage.SYNC.name()))
             .to(SyncCommandAction.class).in(Scopes.SINGLETON);
         bind(CommandAction.class)
             .annotatedWith(Names.named(ProtocolMessage.UPDATE.name()))
             .to(UpdateCommandAction.class).in(Scopes.SINGLETON);
         bind(CommandAction.class)
             .annotatedWith(Names.named(ProtocolMessage.UPDATE_DONE.name()))
             .to(UpdateDoneCommandAction.class).in(Scopes.SINGLETON);
         bind(CommandAction.class)
             .annotatedWith(Names.named(ProtocolMessage.UPDATE_FAILED.name()))
             .to(UpdateFailedCommandAction.class).in(Scopes.SINGLETON);
         bind(CommandAction.class)
             .annotatedWith(Names.named(ProtocolMessage.MAINTENANCE.name()))
             .to(MaintenanceCommandAction.class).in(Scopes.SINGLETON);
         bind(CommandAction.class)
             .annotatedWith(Names.named(ProtocolMessage.HISTORY_REQUEST.name()))
             .to(HistoryRequestCommandAction.class).in(Scopes.SINGLETON);
         bind(CommandAction.class)
             .annotatedWith(Names.named(ProtocolMessage.HISTORY_RESPONSE.name()))
             .to(HistoryResponseCommandAction.class).in(Scopes.SINGLETON);
         
 
         bind(CommandResponse.class)
             .annotatedWith(Names.named(ProtocolMessage.HISTORY_RESPONSE.name()))
             .to(HistoryResponseCommandAction.class).in(Scopes.SINGLETON);
 
         bind(new TypeLiteral<Map<ProtocolMessage, CommandAction>>() {}).annotatedWith(Names.named("commandMap")).toProvider(CommandMapProvider.class);
 
         
         
         bind(String.class).annotatedWith(Names.named("contentDir")).toInstance(contentDir);
 
         String environment = System.getProperty("com.meltmedia.cadmium.environment", "dev");
         
         // Bind channel name
         bind(String.class).annotatedWith(Names.named(JChannelProvider.CHANNEL_NAME)).toInstance("CadmiumChannel-v2.0-"+warName+"-"+environment);
         
         bind(String.class).annotatedWith(Names.named("applicationContentRoot")).toInstance(applicationContentRoot.getAbsoluteFile().getAbsolutePath());
         
         if(repoUri != null) {
           bind(String.class).annotatedWith(Names.named("com.meltmedia.cadmium.git.uri")).toInstance(repoUri);
         }
         
         bind(HistoryManager.class);
 
         bind(Properties.class).annotatedWith(Names.named(CONFIG_PROPERTIES_FILE)).toInstance(configProperties);
 
         // Bind Config file URL
         if(channelConfigUrl == null) {
           log.info("Using internal tcp.xml configuration file for JGroups.");
           URL propsUrl = JChannelProvider.class.getClassLoader().getResource("tcp.xml");
           bind(URL.class).annotatedWith(Names.named(JChannelProvider.CONFIG_NAME)).toInstance(propsUrl);
         } else {
           try {
             log.info("Using {} configuration file for JGroups.", channelConfigUrl);
             bind(URL.class).annotatedWith(Names.named(JChannelProvider.CONFIG_NAME)).toInstance(new URL(channelConfigUrl));
           } catch (MalformedURLException e) {
             log.error("Failed to setup jgroups with the file specified ["+channelConfigUrl+"]. Failing back to built in configuration!", e);
           }
         }
 
         // Bind JChannel provider
         bind(JChannel.class).toProvider(JChannelProvider.class).in(Scopes.SINGLETON);
 
         bind(MembershipListener.class).to(MembershipTracker.class);
         bind(MessageListener.class).to(MessageReceiver.class);
 
         bind(LifecycleService.class);
         bind(CoordinatedWorker.class).to(CoordinatedWorkerImpl.class);
         
         //Bind Meta-config objects
         bind(RedirectConfigProcessor.class);
         bind(MimeTypeConfigProcessor.class);
         bind(SslRedirectConfigProcessor.class);
         bind(new TypeLiteral<List<ConfigProcessor>>() {}).toProvider(MetaConfigProvider.class).in(Scopes.SINGLETON);
         
         bind(SiteConfigProcessor.class);
         
         //This should be the name of a header that BigIp will set if the incoming request was SSL
         bind(String.class).annotatedWith(Names.named(SslRedirectFilter.SSL_HEADER_NAME)).toInstance(SSL_HEADER);
 
         bind(Receiver.class).to(MultiClassReceiver.class).asEagerSingleton();
         
         install(new VaultModule());
         install(new SearchModule());
         
         //bind vault cache-directory
         bind(String.class).annotatedWith(Names.named(VaultConstants.CACHE_DIRECTORY)).toInstance(new File(applicationContentRoot, "vault").getAbsoluteFile().getAbsolutePath());
 
         // Bind Jersey Endpoints
         bind(UpdateService.class).asEagerSingleton();
         bind(MaintenanceService.class).asEagerSingleton();
         bind(HistoryService.class).asEagerSingleton();
         bind(StatusService.class).asEagerSingleton();
         
         // bind email services
         bind(String.class).annotatedWith(Names.named("com.meltmedia.email.jndi")).toInstance(mailJNDIName);
         bind(String.class).annotatedWith(Names.named("melt.mail.messagetransformer")).toInstance(mailMessageTransformer);
         bind(String.class).annotatedWith(Names.named("melt.mail.sessionstrategy")).toInstance(mailSessionStrategy);
         bind(com.meltmedia.cadmium.mail.internal.EmailServiceImpl.class).asEagerSingleton();
         bind(EmailService.class).asEagerSingleton();
         
         // bind Epsilon 
       /* bind(Properties.class).annotatedWith(Names.named("com.meltmedia.cadmium.epsilon.props")).toInstance(epsilonProperties);
         bind(HcpPortPoolImpl.class).asEagerSingleton();                
         bind(HcpEpsilonClientImpl.class).asEagerSingleton();
         
         // HCP Reg Form
        bind(RegisterResource.class).asEagerSingleton();*/
         
       }
     };
   }
   
   public void configureLogback( ServletContext servletContext, File logDir ) throws IOException {
     LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
     try {
       JoranConfigurator configurator = new JoranConfigurator();
       configurator.setContext(context);
       context.reset(); 
       context.putProperty("logDir", logDir.getCanonicalPath());
       configurator.doConfigure(servletContext.getResource("WEB-INF/context-logback.xml"));
     } catch (JoranException je) {
       // StatusPrinter will handle this
     }
     StatusPrinter.printInCaseOfErrorsOrWarnings(context);
   }
   
   public static Properties loadProperties( Properties properties, ServletContext context, String path, Logger log ) {
     Reader reader = null;
     try{
       reader = new InputStreamReader(context.getResourceAsStream(path), "UTF-8");
       properties.load(reader);
     } catch(Exception e) {
       log.warn("Failed to load "+path);
     } finally {
       IOUtils.closeQuietly(reader);
     }
     return properties;
   }
   
   public static Properties loadProperties( Properties properties, File configFile, Logger log ) {
     if( !configFile.exists() || !configFile.canRead()) return properties;
     
     Reader reader = null;
     try{
       reader = new FileReader(configFile);
       properties.load(reader);
     } catch(Exception e) {
       log.warn("Failed to load "+configFile.getAbsolutePath());
     } finally {
       IOUtils.closeQuietly(reader);
     }
     return properties;
   }
   
   public static File sharedContextRoot( Properties configProperties, ServletContext context, Logger log ) {
     File sharedContentRoot = null;
     
     if (configProperties.containsKey(BASE_PATH_ENV)) {
       sharedContentRoot = new File(configProperties.getProperty(BASE_PATH_ENV));
       if (!sharedContentRoot.exists() || !sharedContentRoot.canRead() || !sharedContentRoot.canWrite()) {
         if (!sharedContentRoot.mkdirs()) {
           sharedContentRoot = null;
         }
       }
     }
     
     if (sharedContentRoot == null) {
       log.warn("Could not access cadmium content root.  Using the tempdir.");
       sharedContentRoot = (File) context.getAttribute("javax.servlet.context.tempdir");
     }
     return sharedContentRoot;
   }
   
   public static String getWarName( ServletContext context ) {
     String[] pathSegments = context.getRealPath("/WEB-INF/web.xml").split("/");
     return pathSegments[pathSegments.length - 3];
   }
   
   public static File applicationContentRoot(File sharedContentRoot, String warName, Logger log) {
     File applicationContentRoot = new File(sharedContentRoot, warName);
     if (!applicationContentRoot.exists())
       applicationContentRoot.mkdir();
 
     if (applicationContentRoot == null) {
       throw new RuntimeException("Could not make application content root.");
     } else {
       log.info("Application content root:"
           + applicationContentRoot.getAbsolutePath());
     }
     return applicationContentRoot;
 
   }
   
   public static File getSshDir(Properties configProperties, File sharedContentRoot ) {
     File sshDir = null;
     if (configProperties.containsKey(SSH_PATH_ENV)) {
       sshDir = new File(configProperties.getProperty(SSH_PATH_ENV));
       if (!sshDir.exists() && !sshDir.isDirectory()) {
         sshDir = null;
       }
     }
     if (sshDir == null) {
       sshDir = new File(sharedContentRoot, ".ssh");
       if (!sshDir.exists() && !sshDir.isDirectory()) {
         sshDir = null;
       }
     }
     return sshDir;
   }
 }
