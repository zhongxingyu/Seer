 package com.skplanet.cask.container;
 
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.net.InetAddress;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 import java.util.TreeMap;
 
 import javax.management.InstanceAlreadyExistsException;
 import javax.management.MBeanRegistrationException;
 import javax.management.MBeanServer;
 import javax.management.MBeanServerFactory;
 import javax.management.MalformedObjectNameException;
 import javax.management.NotCompliantMBeanException;
 import javax.management.ObjectName;
 import javax.servlet.ServletException;
 
 import org.apache.catalina.Context;
 import org.apache.catalina.LifecycleException;
 import org.apache.catalina.Wrapper;
 import org.apache.catalina.connector.Connector;
 import org.apache.catalina.core.StandardHost;
 import org.apache.catalina.deploy.ErrorPage;
 import org.apache.catalina.deploy.SecurityCollection;
 import org.apache.catalina.deploy.SecurityConstraint;
 import org.apache.catalina.startup.Tomcat;
 import org.apache.coyote.AbstractProtocol;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.support.BeanDefinitionRegistry;
 import org.springframework.beans.factory.support.GenericBeanDefinition;
 import org.springframework.web.context.support.XmlWebApplicationContext;
 import org.springframework.web.servlet.DispatcherServlet;
 import org.springframework.web.servlet.handler.BeanNameUrlHandlerMapping;
 import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
 
 import com.skplanet.cask.container.config.ConfigReader;
 import com.skplanet.cask.container.config.ServerConfig;
 import com.skplanet.cask.container.config.ServiceInfo;
 import com.skplanet.cask.container.dao.ServerRuntimeDao;
 import com.skplanet.cask.container.dao.ServiceRuntimeDao;
 
 
 public class Container {
 
     Logger logger = LoggerFactory.getLogger(Container.class);
        
     private boolean live = false;
     private Tomcat tomcat;
     //private Server server;
     private Context context;
     private Wrapper servlet;
     
     private String hostName;
 
     private XmlWebApplicationContext rootAppContext;
     private XmlWebApplicationContext servletAppContext;
 
     private MBeanServer mBeanServer = null;
 
     //private static Configuration systemConfig = ConfigReader.getInstance().getSystemConfig();
 
     // context dir = /{server.home}/SERVER_BASE_DIR/WEBAPP_BASE_DIR/CONTEXT_BASE_DIR
     // ${server.home} from jvm argument (-Dserver.home).
     private String SERVER_BASE_DIR = ConfigReader.getInstance().getHome() + "/web";
     private static final String WEBAPP_BASE_DIR = ".";
     private static final String CONTEXT_BASE_DIR = ".";
 
     // url = http://{server.host}:{server.port}/CONTEXT_PATH/DISPATCHER_PATH/
     private String CONTEXT_PATH = ConfigReader.getInstance().getServerConfig().getServerInfo().getContextPath();
     private String DISPATCHER_PATH = ConfigReader.getInstance().getServerConfig().getServerInfo().getServicePath() + "*";
     
     private static final String DISPATCHER_NAME = "appServlet";
     private static final String DISPATCHER_XML = "classpath:servlet-context.xml";
     private static final String DISPATCHER_CLASS = "org.springframework.web.servlet.DispatcherServlet";
 
     // private static final String ROOT_CONTEXT_XML = "classpath:root-context.xml";
     // private static final String ROOT_CONTEXT_CLASS = "org.springframework.web.context.ContextLoaderListener";
 
     private static final String HANDLER_MAPPING_NAME = "handlerMapping";
     private static final String BEAN_NAME_URL_HANDLER_MAPPING_CLASS="org.springframework.web.servlet.handler.BeanNameUrlHandlerMapping";
 
     private static final String MBEAN_DOMAIN = "Cask";
     private static final String MBEAN_KEY = "type";
 
     private static int serverRuntimeDbPkId = -1;
     private static final String serviceAdapter = "com.skplanet.cask.container.service.SimpleServiceAdapter";
     
     public boolean isLive() {
         return live;
     }
     public static int getServerRuntimeDbPkId() {
         return serverRuntimeDbPkId;
     }
     
     public void start() throws ServletException, LifecycleException,
             MalformedObjectNameException, InstanceAlreadyExistsException,
             MBeanRegistrationException, NotCompliantMBeanException, 
             NoSuchMethodException, InvocationTargetException, IllegalAccessException,
             Exception {
 
         ServerConfig serverConfig = ConfigReader.getInstance().getServerConfig();
         
         tomcat = new Tomcat();
         tomcat.setHostname(serverConfig.getServerInfo().getName());
         tomcat.setPort(serverConfig.getServerInfo().getPort());
         tomcat.setBaseDir(SERVER_BASE_DIR);
 
        // connection close
        // set maxKeepAliveRequests = 1 : for keep-alive disable
        tomcat.getConnector().setAttribute("maxKeepAliveRequests", 1);

         InetAddress addr = InetAddress.getByName(serverConfig.getServerInfo().getName());
         hostName = addr.getHostName();
         
         logger.info("hostname : {} {}, port : {}, base : {}, app base : {} ",
                 new Object[]{serverConfig.getServerInfo().getName(),
                              hostName,
                              serverConfig.getServerInfo().getPort(), 
                              SERVER_BASE_DIR,
                              WEBAPP_BASE_DIR});
 
         logger.info("context path : {}, context base : {}, dispatcher name : {}, dispatcher path: {} ",
                 new Object[]{CONTEXT_PATH,
                              CONTEXT_BASE_DIR,
                              DISPATCHER_NAME,
                              DISPATCHER_PATH});
         
         tomcat.getHost().setAppBase(WEBAPP_BASE_DIR);
         //server = tomcat.getServer();
         // AprLifecycleListener listener = new AprLifecycleListener();
         // server.addLifecycleListener(listener);
         
         context = tomcat.addContext(CONTEXT_PATH, CONTEXT_BASE_DIR);
         Tomcat.initWebappDefaults(context);
         // context.addApplicationListener(ROOT_CONTEXT_CLASS);
         // ApplicationParameter param = new ApplicationParameter();
         // param.setName("contextConfigLocation");
         // param.setValue(ROOT_CONTEXT_XML);
         // context.addApplicationParameter(param);
         
         if(ConfigReader.getInstance().getServerConfig().getServerInfo().getErrorRedirect() != null) {
             addErrorPages(
                     context, 
                     new String[]{"401", "402", "403", "404", "405", "406", "500", "501"}, 
                     ConfigReader.getInstance().getServerConfig().getServerInfo().getErrorRedirect());
         }
         
         servlet = Tomcat.addServlet(context, DISPATCHER_NAME, DISPATCHER_CLASS);
         servlet.addInitParameter("contextConfigLocation", DISPATCHER_XML);
         servlet.setLoadOnStartup(1);
         context.addServletMapping(DISPATCHER_PATH, DISPATCHER_NAME);
 
         
         Context defaultContext =  tomcat.addContext("/", ".");
         Wrapper defaultServlet = defaultContext.createWrapper();
         defaultServlet.setName("default");
         
         defaultServlet.setServletClass("org.apache.catalina.servlets.DefaultServlet");
         defaultServlet.addInitParameter("debug", "0");
         defaultServlet.addInitParameter("listings", "false");
         defaultServlet.setLoadOnStartup(1);
         defaultContext.addChild(defaultServlet);
         if(ConfigReader.getInstance().getServerConfig().getServerInfo().getStaticErrorRedirect() != null) {
             addErrorPages(
                     defaultContext, 
                     new String[]{"401", "402", "403", "404", "405", "406", "500", "501"}, 
                     ConfigReader.getInstance().getServerConfig().getServerInfo().getStaticErrorRedirect());
         }
         
         defaultContext.addServletMapping("/", "default");
         
         //((StandardHost)tomcat.getHost()).setErrorReportValveClass("com.skplanet.cask.container.ErrorReport");
 //        
         //Context jspContext =  tomcat.addContext("/jsp", "./jsp"); 
         Wrapper jspServlet = defaultContext.createWrapper();
         jspServlet.setName("jsp");
         jspServlet.setServletClass("org.apache.jasper.servlet.JspServlet");
         jspServlet.addInitParameter("fork", "false");
         jspServlet.addInitParameter("xpoweredBy", "false");
         jspServlet.setLoadOnStartup(2);
         defaultContext.addChild(jspServlet);
         defaultContext.addServletMapping("*.jsp", "jsp");
         
         
         Connector connector = tomcat.getConnector();
         AbstractProtocol protocolHandler = (AbstractProtocol)connector.getProtocolHandler();
 
         protocolHandler.setMaxConnections(serverConfig.getServerInfo().getMaxThreads());
         protocolHandler.setMaxThreads(serverConfig.getServerInfo().getMaxThreads());
         
         logger.info("max connection : {}, max threads : {}",
                 new Object[] {
                 protocolHandler.getMaxConnections(),
                 protocolHandler.getMaxThreads()});     
         
         tomcat.start();
         
         DispatcherServlet disp = (DispatcherServlet)servlet.getServlet();
         servletAppContext = (XmlWebApplicationContext)disp.getWebApplicationContext();
         // Wrapper ttt = (Wrapper)context.findChild("appServlet");
 
         // rootAppContext = (XmlWebApplicationContext)WebApplicationContextUtils
         // .getWebApplicationContext(context.getServletContext());
 
         if(serverConfig.getDataSourceInfoList() != null) {
             ConnectionPoolRegistry.getInstance().init(serverConfig.getDataSourceInfoList());
         }
         
         addServices();
                  
         BeanNameUrlHandlerMapping map = 
                 (BeanNameUrlHandlerMapping)
                 servletAppContext.getBean(BEAN_NAME_URL_HANDLER_MAPPING_CLASS);
         map.initApplicationContext();
         
         if(serverConfig.saveRuntimeIntoDb()) {
             
             logger.info("Inserting server runtime information into DB.");
             
             ServerRuntimeDao serverDao = new ServerRuntimeDao();
             serverDao.updateAbnormal(serverConfig.getServerInfo().getName(), 
                                      serverConfig.getServerInfo().getPort());
             
             serverRuntimeDbPkId = serverDao.insert(
                     serverConfig.getServerInfo().getName(), 
                     serverConfig.getServerInfo().getPort(), 
                     ConfigReader.getInstance().getHome(), 
                     ConfigReader.getInstance().getVersion());
         
             ServiceRuntimeDao serviceDao = new ServiceRuntimeDao();    
             serviceDao.insert(
                     serverRuntimeDbPkId, 
                     serverConfig.getServiceInfoList(), 
                     ServiceRuntimeRegistry.getInstance());
         }
         
         logger.info("Server started.");
         live = true;
         
         logger.info("batch job initailized");
         BatchJobManager.getInstance().runAllBatch();
         
         tomcat.getServer().await();
         
     }
     public void stop() throws LifecycleException {
         tomcat.stop();
         tomcat.destroy();
     }
     public void addErrorPages(Context ctx, String[] errors, String url) {
         
         for(int i = 0; i < errors.length; i++) {
             
             ErrorPage error = new ErrorPage();
             error.setErrorCode(errors[i]);
             error.setLocation(url);
             ctx.addErrorPage(error);
         
         }
 
     }
     public void addServices() throws Exception {
 
         List<ServiceInfo> serviceList = 
                 ConfigReader.getInstance().getServerConfig().getServiceInfoList();
         
         for(int i = 0; i < serviceList.size(); i++) {
             ServiceInfo info = serviceList.get(i);
             addService(info.getUrl(), 
                         serviceAdapter, 
                         info.getClassName(), 
                         info.getClassName());
             
             logger.info("Register service : {} ==> ({} ==>) {}", 
                     new Object[] {info.getUrl(), 
                                   serviceAdapter, 
                                   info.getClassName()} );
          
             Object newMBeanObj = null;
             if(info.getMBeanClassName() != null) {
                 newMBeanObj = addMBean(
                         MBEAN_DOMAIN, 
                         MBEAN_KEY,
                         info.getUrl(),
                         info.getMBeanClassName());
                 
                 logger.info("Register mbean : {}:{}={} ==> {} ",  
                         new Object[] {MBEAN_DOMAIN, MBEAN_KEY, info.getUrl(), info.getMBeanClassName()} );
             }
             
             ServiceRuntimeRegistry.getInstance().addServiceRuntimeInfo(
             		info.getUrl(),
             		newMBeanObj,
             		null,
             		info.getMethod(),
             		info.getInClass(),
             		info.getOutClass());
         }
     }
     
     public Map<String, Object> getServices() {
         String[] beanNames = servletAppContext.getBeanDefinitionNames();
         
         Map<String, Object> beanMap = new TreeMap<String, Object>();
         
         for(int i = 0; i < beanNames.length; ++i) {
             Object obj = servletAppContext.getBean(beanNames[i]);
             beanMap.put(beanNames[i], obj);
         }
         return beanMap;
     }
     
     
     public void addService(String url, String adapterClass, String serviceBeanName, 
                             String handlerClass) throws Exception {
 
         addHandler(url, adapterClass);
         addHandler(serviceBeanName, handlerClass);
     
         Object service = servletAppContext.getBean(serviceBeanName);
         Object serviceAdapter = servletAppContext.getBean(url);
     
         Class<?>[] args = new Class<?>[] {Object.class, Object.class};
         Method method = serviceAdapter.getClass().getMethod("setService", args);
         method.invoke(serviceAdapter, serviceAdapter, service);
     }
          
     private void addHandler(String beanName, Class<?> handlerClass) {    
         //BeanDefinition bd = new RootBeanDefinition(handlerClass);
         GenericBeanDefinition bd = new GenericBeanDefinition();
         bd.setBeanClass(handlerClass);
         getBeanDefinitionRegistry().registerBeanDefinition(beanName, bd);
     }
     private void addHandler(String beanName, String handlerClass) {    
         //BeanDefinition bd = new RootBeanDefinition(handlerClass);
         GenericBeanDefinition bd = new GenericBeanDefinition();
         bd.setBeanClassName(handlerClass);
         getBeanDefinitionRegistry().registerBeanDefinition(beanName, bd);
     }
 
     private void addHandlerMapping(String url, String beanName) {
         // simple url handler mapping : not used 
         // have to have a definition in servlet.xml to invoke simpleurlhandlermapping in dispatcherServlet
         // (not spring default mapping handler)
         // instead, use beannameurlmapping 
         SimpleUrlHandlerMapping urlMapping = (SimpleUrlHandlerMapping) servletAppContext
                 .getBean(HANDLER_MAPPING_NAME);
 
         Properties prop = new Properties();
         prop.setProperty(url, beanName);
         urlMapping.setMappings(prop);
         urlMapping.initApplicationContext();
     }
     public Object addMBean(String domain, String key, String value, String mbeanObject) throws Exception {
 
         ArrayList<MBeanServer> mServerArray = MBeanServerFactory
                 .findMBeanServer(null);
         mBeanServer = mServerArray.get(0);
         
         ObjectName name = new ObjectName(domain, key, value);
         
         Object newObj = Class.forName(mbeanObject).newInstance();
         mBeanServer.registerMBean(newObj, name);
         
         return newObj;
     }
     
     public void removeMBeans() throws Exception {
         
         ArrayList<MBeanServer> mServerArray = MBeanServerFactory
                 .findMBeanServer(null);
         mBeanServer = mServerArray.get(0);
         String pattern = String.format(
                 "%s:%s=*",
                 Container.getMBeanDomain(), Container.getMBeanKey());
         ObjectName name = new ObjectName(pattern);
         Set<ObjectName> names = mBeanServer.queryNames(name, null);
         
         if(names.size() > 0) {
             Iterator<ObjectName> it = names.iterator();
             while(it.hasNext()) {
                 mBeanServer.unregisterMBean(it.next());    
             }
         }
     }
     
     public XmlWebApplicationContext getRootAppContext() {
         return rootAppContext;
     }
 
     
     public XmlWebApplicationContext getServletAppContext() {
         return servletAppContext;
     }    
     private BeanDefinitionRegistry getBeanDefinitionRegistry() {
         return (BeanDefinitionRegistry) servletAppContext.getBeanFactory();
     }  
 
     public static String getMBeanDomain() {
         return MBEAN_DOMAIN;
     }
     public static String getMBeanKey() {
         return MBEAN_KEY;
     }
 }
