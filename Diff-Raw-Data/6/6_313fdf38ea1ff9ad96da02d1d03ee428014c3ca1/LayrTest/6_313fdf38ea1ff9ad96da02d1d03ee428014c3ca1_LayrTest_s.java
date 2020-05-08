 package layr.test;
 
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.ParameterizedType;
 import java.util.Map;
 
 import javax.naming.NamingException;
 import javax.servlet.ServletException;
 
 import layr.LifeCycle;
 import layr.RequestContext;
 import layr.annotation.WebResource;
 
 import org.junit.Before;
 
 public abstract class LayrTest<R> {
 
 	protected RequestContext layrContext;
 	protected LifeCycle lifeCycle;
 	protected R resource;
 
 	@Before
 	public void setup() throws ClassNotFoundException, NamingException, ServletException, IOException, InstantiationException, IllegalAccessException {
 		if (layrContext != null)
 			return;
 
 		lifeCycle = LifeCycleTestFactory.initializeLifeCycle();
 		layrContext = lifeCycle.getLayrRequestContext();
 
 		instantiateResource();
 		setupRequest();
 
 		lifeCycle.bindParameters();
 	}
 
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	public void instantiateResource() throws InstantiationException,
 			IllegalAccessException {
 		resource = (R)((Class)((ParameterizedType)this.getClass().
 			       getGenericSuperclass()).getActualTypeArguments()[0]).newInstance();
 		
 		WebResource webResource = resource.getClass().getAnnotation(WebResource.class);
 		if ( webResource == null )
 			throw new InstantiationException("Resource isn't annotated as @WebResource.");
 		
 		setServletPath(
 			webResource.value().isEmpty()
 				? webResource.rootURL()
 				: webResource.value());
 		lifeCycle.setTargetInstance(resource);
 	}
 
 	public void setupRequest() {}
 
 	public void setRequestURL(String url) {
 		((HttpServletRequestStub)layrContext.getRequest()).setRequestURL(url);
 	}
 
 	public void setLoggedInUser(String userName){
 		((HttpServletRequestStub)layrContext.getRequest()).setRemoteUser(userName);
 	}
 
 	public void setServletPath( String servletPath ) {
 		layrContext.setServletPath(servletPath);
 	}
 	
 	public void setContextpath(String contextPath ) {
 		((ServletContextStub)layrContext.getServletContext()).setContextPath(contextPath);
 		((HttpServletRequestStub)layrContext.getRequest()).setContextPath(contextPath);
 	}
 	
 	public void sendParameters(Map<String, String> parameters) {
 		((HttpServletRequestStub)layrContext.getRequest()).setParameters(parameters);
 	}
 	
 	public void sendParameter( String name, String value ){
 		((HttpServletRequestStub)layrContext.getRequest()).getParameters().put(name, value);
 	}
 
 	public Object invokeCurrentRequestMethod() throws ServletException,
 			IOException, IllegalAccessException, InvocationTargetException {
 		return LifeCycleTestFactory.invokeCurrentRequestMethod( lifeCycle, resource );
 	}
 
 }
