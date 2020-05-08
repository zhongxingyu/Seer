 /**
  * 
  */
 package de.anycook.graph.filter;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.ws.rs.core.MultivaluedMap;
 
 import org.apache.log4j.Logger;
 import com.sun.jersey.spi.container.ContainerRequest;
 import com.sun.jersey.spi.container.ContainerResponse;
 import com.sun.jersey.spi.container.ContainerResponseFilter;
 
 /**
  * Adds Access-Control-Allow headers to response
  * @author Jan Graßegger <jan@anycook.de>
  *
  */
 public class AccessOriginFilter implements ContainerResponseFilter {
 	private final Logger logger;
 	private final static Pattern hostPattern = Pattern.compile("(http://(.*)anycook\\.de).*");
 	
 	/**
 	 * Default constructor
 	 */
 	public AccessOriginFilter() {
 		logger = Logger.getLogger(getClass());
 		logger.debug("filter constructor");
 	}
 
 	/* (non-Javadoc)
 	 * @see com.sun.jersey.spi.container.ContainerResponseFilter#filter(com.sun.jersey.spi.container.ContainerRequest, com.sun.jersey.spi.container.ContainerResponse)
 	 */
 	@Override
 	public ContainerResponse filter(ContainerRequest request,
 			ContainerResponse resp) {
		Matcher hostMatcher = hostPattern.matcher(request.getHeaderValue("Referer"));
 		if(hostMatcher.matches()){
 			
 //			String refHost = referer.getHost();
 			
 			//Quelle: http://stackoverflow.com/questions/5406350/access-control-allow-origin-has-no-influence-in-rest-web-service
 			MultivaluedMap<String, Object> headers =resp.getHttpHeaders();
 			headers.putSingle("Access-Control-Allow-Origin", hostMatcher.group(1)); 
 			headers.putSingle("Access-Control-Allow-Methods", 
 					"POST, PUT,DELETE,GET, OPTIONS");
 
 //		    if ("OPTIONS".equalsIgnoreCase(request.getMethod())) 
 		       headers.putSingle("Access-Control-Allow-Credentials", "true");
 		}
 		
 		return resp;
 	}
 
 	
 	
 	
 
 }
