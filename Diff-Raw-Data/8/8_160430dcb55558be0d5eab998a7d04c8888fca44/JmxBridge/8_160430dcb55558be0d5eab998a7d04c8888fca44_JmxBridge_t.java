 /**
  * A singleton that contains JMX MBean proxies that can allow a JMX service to invoke local EJBs inside
  * an application's class loading context.
  * <br>User: Joshua Davis
  * Date: Aug 29, 2007
  * Time: 5:55:18 AM
  */
 package org.yajul.jmx;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Provides an in-JVM bridge between a JMX service and a POJO implementation
  * that lives inside an EAR or a WAR deployment.  The bridge is a singleton
  * that contains a set of proxy objects associated with the implementation class
  * name.   The proxy objects are initialized by a startup Servlet (or any
  * object that has access to the EAR/WAR class loader).   The lifecycle calls
  * from the JMX bean are routed through the proxy to the implementation POJO.
  * <h2>What you need to use it:</h2>
  * <ol><li>A JMX Management Bean, deployed as a JMX service
  * (a '.sar' in JBoss).  The MBean code will not load the implementation class
  * directly, but it will know the name of the class.</li>
  * <li>An implementation POJO, deployed in a WAR or EAR.  This must implement
  * <tt>org.yajul.ee5.jmx.Lifecycle</tt>.</li>
  * <li>A startup Servlet, or equivalent code that gets executed when the EAR/WAR
  * is deployed.  This is what will instantiate and register the POJO
  * implementation with the bridge singleton.</li>
  * </ol>
  * <h2>Usage:</h2>
  * <ol><li>Create the implementation POJO in your WAR/EAR module.  Implement
  * the Lifecycle interface.  <i>NOTE: Do not package the YAJUL jars inside the
  * EAR or WAR.</i></li>
  * <li>Create or modify an existing startup Servlet.  Call the register() method
  * in the JmxBridge singleton for each implementation POJO.</li>
  * <li>Create the JMX MBean.  In the JMX MBean methods, get the proxy using
  * the implementation class name (<i>Don't link directly with the class, that
  * would defeat the purpose!</i>).  Delegate the start() and stop() methods to
  * the proxy.</li>
  * <li>Package the JMX MBean appropriately for your container.  <i>Make sure the
  * jar with the JmxBridge code is in the 'root' classloader, and not deployed
  * with the JMX MBean, otherwise the bridge will not function.</i>  In JBoss
  * you can do this by simply adding the YAJUL jar to the server <tt>lib</tt>
  * directory.</li>
  * <li>Deploy the YAJUL jar, the JMX MBean, and the EAR or WAR.  When the
  * server starts you should see the Proxy being registered and the POJO
  * implementation being created when the startup Servlet runs.</li>
  * </ol>
  * <br>User: Joshua Davis
  * Date: Aug 29, 2007
  * Time: 5:58:11 AM
  */
 public class JmxBridge {
     private static final Logger log = LoggerFactory.getLogger(JmxBridge.class);
 
     private Map<String, Proxy> proxiesByImplementationClassName;
     private ImplementationProvider implementationProvider;
 
     private static JmxBridge INSTANCE = new JmxBridge();
 
     public static JmxBridge getInstance() {
         // NOTE: This it's own singleton to decouple it from SingletonManager.
         return INSTANCE;
     }
 
     public JmxBridge() {
         proxiesByImplementationClassName = new HashMap<String, Proxy>();
        implementationProvider = new DefaultImplementationProvider();
         log.info("created.");
     }
 
     /**
      * @return the implementation provider
      */
     public ImplementationProvider getImplementationProvider() {
         synchronized (this) {
             return implementationProvider;
         }
     }
 
     /**
      * Sets the implementation provider (e.g. a microcontainer for the JMX MBean implementations)
      *
      * @param implementationProvider the implementation provider
      */
     public void setImplementationProvider(ImplementationProvider implementationProvider) {
         synchronized (this) {
             this.implementationProvider = implementationProvider;
         }
     }
 
     /**
      * Stop all MBeans.  Clear everything.
      */
     public void reset() {
         synchronized (this) {
             for (Proxy proxy : proxiesByImplementationClassName.values()) {
                 proxy.stop();
             }
             proxiesByImplementationClassName.clear();
         }
     }
 
     /**
      * Return the proxy for the given MBean implementation.
      *
      * @param implementationClassName the implementation class
      * @return the proxy
      */
     public Proxy getProxy(String implementationClassName) {
         synchronized (this) {
             return doGetProxy(implementationClassName);
         }
     }
 
     /**
      * Register a specific implementation class with the bridge.   Invoke this from a suitable class loading context.  For example, from
      * a startup Servlet.
      *
      * @param implClass the implementation class.
      * @throws Exception if the proxies created by the MBeans could not be initialized.
      */
     public void register(Class implClass) throws Exception {
         synchronized (this) {
             // Get or create the proxy.
             Proxy proxy = doGetProxy(implClass.getName());
             // Initialize it now.
             proxy.initialize();
             log.info(implClass.getName() + " registered.");
         }
     }
 
     /**
      * Initializes all the proxies.   Invoke this from a suitable class loading context.  For example, from
      * a startup Servlet.
      *
      * @param provider class lookup / MBean factory
      * @throws Exception if the proxies created by the MBeans could not be initialized.
      */
     public void initializeProxies(ImplementationProvider provider) throws Exception {
         synchronized (this) {
            if (provider != null)
                this.implementationProvider = provider;
             for (Proxy proxy : proxiesByImplementationClassName.values()) {
                 proxy.initialize();
             }
         }
         log.info("initializeProxies() : completed.");
     }
 
     /**
      * Initializes all the proxies.   Invoke this from a suitable class loading context.  For example, from
      * a startup Servlet.
      *
      * @throws Exception if the proxies created by the MBeans could not be initialized.
      */
     public void initializeProxies() throws Exception {
        initializeProxies(null);
     }
 
     private Proxy doGetProxy(String implementationClassName) {
         Proxy proxy = proxiesByImplementationClassName.get(implementationClassName);
         if (proxy == null) {
             proxy = new Proxy(implementationClassName, this);
             proxiesByImplementationClassName.put(implementationClassName, proxy);
         }
         return proxy;
     }
 
     Class<?> getImplementationClass(String className) throws ClassNotFoundException {
         return implementationProvider.getImplementationClass(className);
     }
 
     <T> T getImplementation(Class<T> implementationClass) {
         return implementationProvider.getImplementation(implementationClass);
     }
 }
