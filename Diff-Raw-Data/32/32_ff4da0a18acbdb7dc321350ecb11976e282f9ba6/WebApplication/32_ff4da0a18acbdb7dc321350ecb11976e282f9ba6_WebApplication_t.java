 package org.vaadin.navigator7;
 
 import java.io.IOException;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.vaadin.navigator7.interceptor.ExceptionPage;
 import org.vaadin.navigator7.interceptor.Interceptor;
 import org.vaadin.navigator7.interceptor.ParamChangeListenerInterceptor;
 import org.vaadin.navigator7.interceptor.ParamInjectInterceptor;
import org.vaadin.navigator7.interceptor.PageInvocation.PageInstantiationStrategy;
 import org.vaadin.navigator7.uri.ParamUriAnalyzer;
 
import com.vaadin.ui.Component;

 /**
  * You may extend this class to manually register your pages. If you do that, you must specify the class name as an init parameter of the NavigableApplicationServlet (as MyWebApplication in this example):
  * <pre><servlet-class>org.vaadin.navigator7.NavigableApplicationServlet</servlet-class>
     <init-param>
         <description>Vaadin application class to start</description>
         <param-name>application</param-name>
         <param-value>example.ui.application.MyNavigableApplication</param-value>
     </init-param>
     <init-param>
         <description>Navigator7 WebApplication class to start (optionnal)</description>
         <param-name>webApplication</param-name>
         <param-value>example.ui.application.MyWebApplication</param-value>
     </init-param>
   </servlet>
  * </pre>
  * 
  * There should be one instance of this for the whole web application = 1 instance per servlet context.
  * The name is confusing with com.vaadin.Application (which has one instance per HttpSession) because
  * probably com.vaadin.Application should be renamed "UserContext" (for Java web developers, the term "Application" means ServletContext)
  * 
  * See http://vaadin.com/forum/-/message_boards/message/155212#_19_message_159169
  * 
  * 
  * @author John Rizzo - BlackBeltFactory.com
  *
  */
 public class WebApplication {
 
     public static final String WEBAPPLICATION_CONTEXT_ATTRIBUTE_NAME = WebApplication.class.getName();
 
     /** Holds the ServletContext instance of the current thread.
      * If it's null for the current thread, we are not in a web thread but in a batch thread.
      * 
      * We need the ServletContext because we store the WebApplication instance there.
      *   
      * @See http://vaadin.com/forum/-/message_boards/message/155212?_19_delta=10&_19_keywords=&_19_advancedSearch=false&_19_andOperator=true&cur=2#_19_message_166110
      **/
     static protected ThreadLocal<ServletContext> currentServletContext = new ThreadLocal<ServletContext>();
 
     /** A batch thread may need the WebApplication instance. 
      * For example, to build an url (with new ParamPageResource(MyPage.class, myEntity)).
      * If this happens, we can get and instance of the WebApplication here (currentServletContext will return null).
      * 
      * This is unfortunate, but somehow dictated by the Servlet specification. Good servlet container practice tells to put the reference in the ServletContext. Batch needs tell to put it in a static variable (singleton). 
      * An improvement would be to store the name of the WebApplication subclass (and all the Vaadin config) in a more neutral place than the web.xml.
      * That way, if the batch thread starts before the first web request (thread) arrives, or if the batch executes outside of the web contained, then we could instantiate from the batch thread (if not instantiated yet, of course).
      *
      * There is probably a difficult design choice to be made by Vaadin guys here, to integrate Navigator7 into Vaadin7 (difficult design choice to have the notion of Web app at all in Vaadin).
      * Keeping the instance in both the ServletContext and a static variable is an overlap.
      *    Keeping it in the ServletContext only is not enough (would make batch jobs fail). 
      *    Maybe keeping it in this static reference only?
      * Note that frameworks as Spring have the same (unsolved as far as I know) problem.
      *    In web applications, they keep the Spring ApplicationContext in the ServletContext.
      *    As Quartz is usually started by Spring, it's easy for Spring to pass its (Web)ApplicationContext to Quartz,
      *    and so to batch jobs. But batch jobs have to do some gym to extract the Spring (Web)ApplicationContext from Quartz,
      *    it's not transparent.
      * We don't have that possibility here (Vaadin does not start batch jobs). 
      * Maybe somebody will have a better idea ;-) 
      */
     static protected WebApplication staticReference;
     
    private static final PageInstantiationStrategy DEFAULT_PAGE_INSTANTIATION_STRATEGY = new DefaultPageStrategy();
     
     /** Don't hesitate to use this method ;-)
      * Returns null if we are not in a web thread (or a badly initialized web app) */
     public static WebApplication getCurrent() {
         ServletContext servletContext = currentServletContext.get();
         if (servletContext == null) {  // We are in a batch thread, probably: http://vaadin.com/forum/-/message_boards/message/216481?_19_delta=10&_19_keywords=&_19_advancedSearch=false&_19_andOperator=true&cur=3#_19_message_216481
             if (staticReference == null) {
                 throw new RuntimeException("WebApplication has not been instantiated yet. " +
                 		"A common cause is that the thread executing this, is a batch thread, AND that the NavigableApplicationServlet has not bee initialized yet. " +
                 		"Suggestions:" +
                 		" delay your batch;" +
                 		" or call WebApplication.init(YourWebApplication.class) manually at the beginning of your batch;" +
                 		" or call WebApplication.init(YourWebApplication.class) in a ServletContextListener.contextInitialized() when your web application starts instead of NavigableApplicationServlet.init()");
             } else {
                 return staticReference;
             }
             
         } else { // web thread (usual case).
             return (WebApplication)servletContext.getAttribute(WEBAPPLICATION_CONTEXT_ATTRIBUTE_NAME);
         }
     }
     
     /**
      * Should be called once by NavigableApplicationServlet, or by your servlet extending com.vaadin.terminal.gwt.server.ApplicationServlet
      * Instantiates your WebApplication descendant (that you specified in your web.xml file) and attaches it to the ServletContext.
      */
     @SuppressWarnings("unchecked")
     public static synchronized void init(ServletConfig servletConfig, ServletContext servletContext, ClassLoader classLoader) {
 
         WebApplication webApplication; 
         
         if (staticReference == null) {
 
             // Loads the application class using the same class loader
             // as the servlet itself
 
             // Gets the application class name, then class.
             final String applicationClassName = servletConfig.getInitParameter("webApplication");
             Class<? extends WebApplication> applicationClass;
             if (applicationClassName == null) {  // Using defaults.
                 applicationClass = WebApplication.class;  // User is probably a beginner and did not made his own WebApplication descendant yet.
             } else {
                 Class<?> clazz;
                 try {
                     clazz = classLoader.loadClass(applicationClassName);
                 } catch (final ClassNotFoundException e) {
                     throw new RuntimeException("Failed to load application class: [" + applicationClassName + "]");
                 }
                 if (! WebApplication.class.isAssignableFrom(clazz)) {
                     throw new RuntimeException("Class given as parameter is no subclass of WebApplication (and it should): "
                             + clazz.getName() + ". Check your web.xml configuration (init-param of the NavigableApplicationServlet configuration)." );
 
                 }
                 applicationClass = (Class<? extends WebApplication>) clazz;
             }
 
             // Ok, we got the class, let's instantiate it.
             webApplication = instantiate(applicationClass);
             
         } else { // a batch thread did the initialization job already (probably by calling init(Class) directly, before the this init has been called.
             webApplication = staticReference;
         }
         
         servletContext.setAttribute(WEBAPPLICATION_CONTEXT_ATTRIBUTE_NAME, webApplication);
         staticReference = webApplication;
     }
     
     /** Call this at the beginning of a batch if you fear that the NavigableApplicationServlet.init (triggering the other WebApplication.init()) may not have been called (yet or never). */
     public static synchronized void init(Class<? extends WebApplication> applicationClass) {
         if (staticReference == null) { // We are indeed the first to initialize the WebApplication.
             staticReference = instantiate(applicationClass);
             // We don't put it in the ServletContext because we don't have it (yet).
             
         } // else, already instantiated (by a previous batch, or by the NavigableApplicationServlet.
     }
 
     private static WebApplication instantiate(Class<? extends WebApplication> applicationClass) {
         try {
             return applicationClass.newInstance();
         } catch (final IllegalAccessException e) {
             throw new RuntimeException("Failed to instantiate WebApplication (sub?) class: " + applicationClass.getName(), e);
         } catch (final InstantiationException e) {
             throw new RuntimeException("Failed to instantiate WebApplication (sub?) class: " + applicationClass.getName(), e);
         }
     }
     
     /** Should be called at the start of every request by NavigableApplicationServlet, or by your servlet extending com.vaadin.terminal.gwt.server.ApplicationServlet
      * I'd prefer to do that in a Filter, but it would be against the Vaadin current architecture 
      * Note that Vaadin TransactionListeners have no access to the ServletContext => we cannot use TransactionListeners. */
     @SuppressWarnings("unchecked")
     static public void beforeService(HttpServletRequest request,
             HttpServletResponse response, ServletContext servletContext) throws ServletException, IOException {
         WebApplication.currentServletContext.set(servletContext);
     }
 
     /** Should be called by NavigableApplicationServlet at the end of every request, or by your servlet extending com.vaadin.terminal.gwt.server.ApplicationServlet */
     @SuppressWarnings("unchecked")
     static public void afterService(HttpServletRequest request,
             HttpServletResponse response, ServletContext servletContext) throws ServletException, IOException {
         WebApplication.currentServletContext.remove();
     }
 
     
     
     /**
      * Copy of com.vaadin.Application.getVersion() method. In fact it makes more sense to have such a version definition here and com.vaadin.Application.getVersion() method should be moved here in WebApplication.
      * 
      * Override this method to return correct version number of your
      * Application. Version information is delivered for example to Testing
      * Tools test results. By default this returns a string "NONVERSIONED".
      * 
      * @return version string
      */
     public String getVersion() {
         return "NONVERSIONED";
     }
 
 
     ///////////////////////////////////////////// Navigable stuff (to be moved in a NavigableWebAppliction subclass?) /////////////////////////////////////////////////////
     
     public WebApplication() {
         // Register this page used for displaying exceptions (by default).
         registerPages(new Class[] {ExceptionPage.class});
         
         // Register default interceptors
         registerInterceptors();
     }
     
     /** Override this method to add your own interceptor to the defaults.
      * Just don't call super.registerInterceptor if you don't want the default to be registered.
      */
     protected void registerInterceptors() {
         registerInterceptor( new ParamChangeListenerInterceptor() );
         registerInterceptor( new ParamInjectInterceptor() );
     }
     
     protected NavigatorConfig navigatorConfig = new NavigatorConfig();
     
     /** Don't hesitate to change this value with another descendant of UriAnalyser in your constructor's descendant. */
     protected ParamUriAnalyzer uriAnalyzer = new ParamUriAnalyzer();
 
 
     
 
     public void registerPages(Class[] pageClasses) {
         navigatorConfig.registerPages(pageClasses);
     }
 
     /** Scans the pages annotated with @Page in the classpath, for the sub-package of the package given as parameter */
     public void registerPages(String packageName) {
         // do some scanning of @Page annotated classes and add them to the navigator.
         navigatorConfig.registerPages(packageName);
     }
 
     public void registerInterceptor(Interceptor interceptor) {
         navigatorConfig.getInterceptorList().add(interceptor);
     }
     
     public NavigatorConfig getNavigatorConfig() {
         return navigatorConfig;
     }
 
     public ParamUriAnalyzer getUriAnalyzer() {
         return uriAnalyzer;
     }
 
     public void setUriAnalyzer(ParamUriAnalyzer paramUriAnalyzer) {
         uriAnalyzer = paramUriAnalyzer;
     }
 
    public PageInstantiationStrategy getPageInstantiationStrategy() {
    	return DEFAULT_PAGE_INSTANTIATION_STRATEGY;
    }
 
    
    private static class DefaultPageStrategy implements PageInstantiationStrategy {
 
		@Override
		public <P extends Component> P createPageInstance(Class<P> pageClass,
				String params) throws Exception {
			return pageClass.newInstance();
		}
    }
     
 }
