 package feedreader.web.rest;
 
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Pattern;
 
 import javax.servlet.GenericServlet;
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import common.ioc.Container;
 
 import feedreader.web.ContainerFilter;
 
 /**
  * Servlet for handling REST Api requests.
  * @author jared.pearson
  */
 public class RestServlet extends GenericServlet {
 	private static final long serialVersionUID = 7150818916790895212L;
 	private List<RequestHandlerMapping> requestMappings = null;
 	
 	@Override
 	public void init(ServletConfig config) throws ServletException {
 		
 		//parse the classes parameter
 		List<Class<?>> requestHandlerClasses;
 		try {
 			requestHandlerClasses = parseClassCsv(config.getInitParameter("classes"));
 		} catch (ClassNotFoundException exc) {
 			throw new ServletException(exc);
 		}
 		
 		//parse the RequestHandler annotations from the specified classes
 		requestMappings = new ArrayList<RequestHandlerMapping>();
 		for(Class<?> requestHandlerClass : requestHandlerClasses) {
 			Object handler = null;
 			
 			for(java.lang.reflect.Method method : requestHandlerClass.getMethods()) {
 				if(!method.isAnnotationPresent(RequestHandler.class)) {
 					continue;
 				}
 				RequestHandler requestHandlerAnnotation = method.getAnnotation(RequestHandler.class);
 				
 				String pathRegex = requestHandlerAnnotation.value();
 				Pattern pattern = Pattern.compile(pathRegex);
 				
 				//create the handler or get one that was already created
 				if(handler == null) {
 					try {
 						handler = requestHandlerClass.newInstance();
 					} catch (InstantiationException e) {
 						throw new ServletException(e);
 					} catch (IllegalAccessException e) {
 						throw new ServletException(e);
 					}
 				}
 				
 				requestMappings.add(new RequestHandlerMapping(pattern, handler, method, requestHandlerAnnotation.method()));
 			}
 		}
 	}
 
 	@Override
 	public void service(ServletRequest request, ServletResponse response)
 			throws ServletException, IOException {
 		HttpServletRequest httpRequest = (HttpServletRequest) request;
 		HttpServletResponse httpResponse = (HttpServletResponse) response;
 		
 		//get the container for the current request
 		Container container = ContainerFilter.getContainerFromRequest(httpRequest);
 		
 		try {
 			RequestHandlerMapping mapping = getMapping(httpRequest);
 			mapping.execute(container);
 		} catch(Exception exc) {
 			exc.printStackTrace(System.err);
 			httpResponse.sendError(500);
 		}
 	}
 	
 	/**
 	 * Gets the mapping from the request. If the no mapping is found, then
 	 * an {@link IllegalStateException} is thrown.
 	 */
 	private RequestHandlerMapping getMapping(HttpServletRequest request) {
 		for(RequestHandlerMapping mapping : requestMappings) {
 			if(mapping.handles(request)) {
 				return mapping;
 			}
 		}
 		
 		throw new IllegalStateException("No mapping specified for request");
 	}
 	
 	/**
 	 * Parses a list of class names from the given comma-separated string
 	 */
 	private List<Class<?>> parseClassCsv(String classesValue) throws ClassNotFoundException {
 
 		String[] classNames = null;
 		if(classesValue == null || classesValue.trim().length() == 0) {
 			classNames = new String[0];
 		} else {
 			classNames = classesValue.split(",");
 		}
 		
 		//convert the classNames to classes
 		List<Class<?>> requestHandlerClasses = new ArrayList<Class<?>>(classNames.length);
 		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
 		for(String className : classNames) {
			Class<?> clazz = classLoader.loadClass(className);
 			requestHandlerClasses.add(clazz);
 		}
 		
 		return requestHandlerClasses;
 	}
 	
 	private static class RequestHandlerMapping {
 		private final Pattern pattern;
 		private final Object handler;
 		private final Method method;
 		private final java.lang.reflect.Method handlerMethod;
 		
 		public RequestHandlerMapping(Pattern pattern, Object handler, java.lang.reflect.Method handlerMethod, Method method) {
 			this.pattern = pattern;
 			this.handler = handler;
 			this.handlerMethod = handlerMethod;
 			this.method = method;
 		}
 		
 		/**
 		 * Determines if the request is mapped to this RequestHandler
 		 */
 		public boolean handles(HttpServletRequest request) {
 			if(!request.getMethod().equals(method.toString())) {
 				return false;
 			}
 			
 			String pathInfo = request.getPathInfo();
 			return pattern.matcher(pathInfo).matches();
 		}
 		
 		/**
 		 * Executes the handler against the specified request
 		 */
 		public void execute(Container container)
 			throws ServletException, IOException {
 			
 			//from the parameter types specified on the method, look up the 
 			//objects corresponding to the types from the IOC container
 			Class<?>[] parameterTypes = handlerMethod.getParameterTypes();
 			Object[] values = new Object[parameterTypes.length];
 			for(int index = 0; index < parameterTypes.length; index++) {
 				
 				Class<?> parameterType = parameterTypes[index];
 				values[index] = container.getComponent(parameterType);
 				
 			}
 			
 			//invoke the handler method with parameters requested
 			try {
 				handlerMethod.invoke(handler, values);
 			} catch (IllegalArgumentException e) {
 				throw new ServletException(e);
 			} catch (IllegalAccessException e) {
 				throw new ServletException(e);
 			} catch (InvocationTargetException e) {
 				throw new ServletException(e);
 			}
 		}
 	}
 }
