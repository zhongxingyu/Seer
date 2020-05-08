 package anyflow.engine.network.http;
 
 import java.lang.annotation.ElementType;
 import java.lang.annotation.Retention;
 import java.lang.annotation.RetentionPolicy;
 import java.lang.annotation.Target;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import org.jboss.netty.handler.codec.http.HttpHeaders;
 import org.jboss.netty.handler.codec.http.HttpRequest;
 import org.jboss.netty.handler.codec.http.HttpResponse;
 import org.jboss.netty.handler.codec.http.HttpResponseStatus;
 import org.jboss.netty.handler.codec.http.QueryStringDecoder;
 import org.jboss.netty.util.CharsetUtil;
 import org.reflections.Reflections;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import anyflow.engine.network.Configurator;
 import anyflow.engine.network.exception.DefaultException;
 
 /**
  * @author anyflow
  * Base class for business logic. The class contains common stuffs for generating business logic.
  */
 public abstract class RequestHandler {
 
 	private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);
 	
 	private HttpRequest request;
 	private HttpResponse response;
 	private Map<String, List<String>> parameters;
 	
 	public void initialize(HttpRequest request, HttpResponse response) {
 		this.request = request;
 		this.response = response;
 		
 		parseParameters();
 	}
 
 	public HttpRequest getRequest() {
 		return request;
 	}
 	
 	public HttpResponse getResponse() {
 		return response;
 	}
 	
 	public String getUri() { 
 		return request.getUri();
 	}
 	
 	public String getHttpMethod() { 
 		return request.getMethod().toString();
 	}
 	
 	public String getProtocolVersion() {
 		return request.getProtocolVersion().toString();
 	}
 	
 	public String getHost() {
 		return HttpHeaders.getHost(request, "unknown"); 
 	}
 	
 	private void parseParameters() {
 		String httpMethod = request.getMethod().toString();
 		String queryStringParam = null;
 		
         if(httpMethod.equalsIgnoreCase("GET")) {
         	queryStringParam = request.getUri();
         }
         else if(httpMethod.equalsIgnoreCase("POST")) {
         	String dummy = "/dummy?";
         	queryStringParam =  dummy + request.getContent().toString(CharsetUtil.UTF_8);
         }
         else {
         	response.setStatus(HttpResponseStatus.METHOD_NOT_ALLOWED);
         	throw new UnsupportedOperationException("only GET/POST http methods are supported.");
         }
         
         QueryStringDecoder queryStringDecoder = new QueryStringDecoder(queryStringParam);
         
         parameters = queryStringDecoder.getParameters();
 	}
 	
 	public String getParameter(String key) {
		return parameters.get(key).get(0);
 	}
 	
 	public List<String> getArrayParameter(String key) {
 		return parameters.get(key);
 	}
 	
 	/**
 	 * @return processed content string
 	 */
 	public abstract String call();
 	
 	/**
 	 * @author anyflow
 	 *
 	 */
 	@Retention(RetentionPolicy.RUNTIME)
 	@Target(ElementType.TYPE)
 	public @interface Handles {
 		/**
 		 * @return supported paths
 		 */
 		String[] paths();
 		
 		/**
 		 * supported http methods
 		 * @return http method string
 		 */
 		String[] httpMethods();
 	}
 	
 	/**
 	 * @param requestedPath
 	 * @param httpMethod
 	 * @return
 	 * @throws DefaultException
 	 */
 	public static Class<? extends RequestHandler> find(String requestedPath, String httpMethod) throws DefaultException {
 
 		Reflections reflections = new Reflections(Configurator.getRequestHandlerPackageRoot());
 		String contextRoot = Configurator.getHttpContextRoot();
 		
 		Set<Class<? extends RequestHandler>> requestHandler = reflections.getSubTypesOf(RequestHandler.class);
 		
 		for(Class<? extends RequestHandler> item : requestHandler) {
 			
 			RequestHandler.Handles bl = item.getAnnotation(RequestHandler.Handles.class);
 			
 			if(bl == null) { 
 				continue; 
 			}
 			
 			for(String method : bl.httpMethods()) {
 				if(method.equalsIgnoreCase(httpMethod) == false) {
 					continue;
 				}
 				
 				for(String rawPath : bl.paths()) {
 					
 					String path = (rawPath.charAt(0) == '/')
 							    ? rawPath
 							    : contextRoot + rawPath;
 					
 					if(requestedPath.equalsIgnoreCase(path)) { 
 						return item;
 					}
 					else {
 						continue;
 					}
 				}
 			}
 		}
 		
 		logger.error("Failed to find requestHandler.");
 		return null;
 	}
 }
