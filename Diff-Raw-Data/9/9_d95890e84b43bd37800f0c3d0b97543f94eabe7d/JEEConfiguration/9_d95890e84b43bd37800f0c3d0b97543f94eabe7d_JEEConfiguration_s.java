 package org.layr.jee.commons;
 
 import java.io.IOException;
 import java.util.Date;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 
 import javax.servlet.ServletContext;
 
 import org.layr.commons.Cache;
 import org.layr.commons.classpath.AbstractSystemResourceLoader;
 import org.layr.commons.classpath.ClassPathResourceLoader;
 import org.layr.engine.components.IComponentFactory;
 
 public class JEEConfiguration {
 	
 	private boolean isCacheEnabled;
 	private String defaultResource;
 	private AbstractSystemResourceLoader[] applicationContexts;
 	private Set<String> availableLocalResourceFiles;
 	private ServletContext servletContext;
 	private Map<String, IComponentFactory> registeredTagLibs;
 	private Cache cache;
 
 	public JEEConfiguration( ServletContext servletContext ) {
 		setServletContext(servletContext);
 		setCacheEnabled( readSystemProperty("cacheEnabled", "false") );
		setDefaultResource( "/theme/" );
 		createCache();
 	}
 
 	public void createCache(){
 		String contextPath = getServletContext().getContextPath();
 		String now = String.valueOf( new Date().getTime() );
 		setCache( Cache.newInstance( contextPath + now ) );
 	}
 
 	public void setApplicationContexts(AbstractSystemResourceLoader[] applicationContexts) {
 		this.applicationContexts = applicationContexts;
 	}
 
 	public AbstractSystemResourceLoader[] getApplicationContexts() {
 		if (applicationContexts == null) {
 			initializeApplicationContexts();
 		}
 		return applicationContexts;
 	}
 
 	public void initializeApplicationContexts() {
 		applicationContexts = new AbstractSystemResourceLoader[] {
 				new ServletContextResourceLoader(getServletContext(), getClassLoader()),
 				new ClassPathResourceLoader(getClassLoader())
 			};
 
 		setAvailableLocalResourceFiles(new TreeSet<String>());
 		for (AbstractSystemResourceLoader loader : getApplicationContexts())
 			try {
 				for (String url : loader.retrieveAvailableResources())
 					getAvailableLocalResourceFiles().add(url);
 			} catch (IOException e) {
 				continue;
 			}
 	}
 
 	public Set<String> getAvailableLocalResourceFiles() {
 		return availableLocalResourceFiles;
 	}
 
 	public void setAvailableLocalResourceFiles(
 			Set<String> availableLocalResourceFiles) {
 		this.availableLocalResourceFiles = availableLocalResourceFiles;
 	}
 
 	public String readSystemProperty( String propertyName, String defaultValue ){
 		String propertyValue = System.getProperty("org.layr.config." + propertyName);
 		if ( propertyValue == null || propertyValue.isEmpty() )
 			propertyValue = defaultValue;
 		return propertyValue;
 	}
 
 	public boolean isCacheEnabled() {
 		return isCacheEnabled;
 	}
 
 	public void setCacheEnabled(String isCacheEnabled){
 		setCacheEnabled( Boolean.valueOf(isCacheEnabled) );
 	}
 
 	public void setCacheEnabled(boolean isCacheEnabled) {
 		this.isCacheEnabled = isCacheEnabled;
 	}
 
 	public String getDefaultResource() {
 		return defaultResource;
 	}
 
 	public void setDefaultResource(String defaultResource) {
 		this.defaultResource = defaultResource;
 	}
 
 	public ClassLoader getClassLoader() {
 		return Thread.currentThread().getContextClassLoader();
 	}
 
 	public ServletContext getServletContext() {
 		return servletContext;
 	}
 
 	public void setServletContext(ServletContext servletContext) {
 		this.servletContext = servletContext;
 	}
 
 	public void setRegisteredTagLibs(
 			Map<String, IComponentFactory> registeredTagLibs) {
 		this.registeredTagLibs = registeredTagLibs;
 	}
 
 	public Map<String, IComponentFactory> getRegisteredTagLibs() {
 		return registeredTagLibs;
 	}
 
 	public Cache getCache() {
 		return cache;
 	}
 
 	public void setCache(Cache cache) {
 		this.cache = cache;
 	}
 }
