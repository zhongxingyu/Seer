 package no.magott.spring.configuration.logging;
 
 import org.apache.log4j.helpers.FileWatchdog;
 import org.springframework.beans.factory.DisposableBean;
 import org.springframework.beans.factory.InitializingBean;
 import org.springframework.util.Log4jConfigurer;
 import static org.springframework.util.Log4jConfigurer.CLASSPATH_URL_PREFIX;;
 
/**
 * Enables automatic reload of log4j configuration similar to org.springframework.web.util.Log4jConfigListener
 * but can be used outside a web container, such as stand alone JRE applications.
 * @author morten andersen-gott
 *
 */
 public class Log4jDynamicConfigurer implements InitializingBean, DisposableBean{
 
     public static final String DEFAULT_LOG4J_PROPERTIES = CLASSPATH_URL_PREFIX+"log4j.properties";
     private static final long DEFAULT_DELAY = FileWatchdog.DEFAULT_DELAY;
     
     private long refreshInterval = DEFAULT_DELAY;
     private String location =DEFAULT_LOG4J_PROPERTIES;
     
     public void afterPropertiesSet() throws Exception {
         Log4jConfigurer.initLogging(location, refreshInterval);
     }
 
     public void destroy() throws Exception {
         Log4jConfigurer.shutdownLogging();        
     }
 
     public void setRefreshInterval(long refreshInterval) {
         this.refreshInterval = refreshInterval;
     }
 
     public void setLocation(String location) {
         this.location = location;
     }
    
 
 }
