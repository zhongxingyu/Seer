 /**
  * 
  */
 package de.anycook.api.filter.cors;
 
 import org.apache.log4j.Logger;
 
 import javax.ws.rs.container.ContainerRequestContext;
 import javax.ws.rs.container.ContainerResponseContext;
 import javax.ws.rs.container.ContainerResponseFilter;
 import javax.ws.rs.core.MultivaluedMap;
 import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 
 /**
  * Adds Access-Control-Allow headers to response
  * @author Jan Graßegger <jan@anycook.de>
  *
  */
@Provider
 public class CorsFilter implements ContainerResponseFilter {
 	public final static Pattern hostPattern = Pattern.compile("http://(.*)anycook\\.de");
 	private final Logger logger;
 	
 	/**
 	 * 
 	 */
 	public CorsFilter() {
 		logger = Logger.getLogger(getClass());
 	}
 	
 	/* (non-Javadoc)
 	 * @see com.sun.jersey.spi.container.ContainerResponseFilter#filter(com.sun.jersey.spi.container.ContainerRequest, com.sun.jersey.spi.container.ContainerResponse)
 	 */
 	@Override
 	public void filter(ContainerRequestContext requestContext,
 			ContainerResponseContext responseContext) {
         String method = requestContext.getMethod();
         String path = requestContext.getUriInfo().getPath();
 		logger.debug(String.format("filtering: %s:%s", method, path));
 		
 		CORSRequestType corsType = CORSRequestType.detect(requestContext);
 		
 		switch(corsType){
 		case PREFLIGHT:
 			filterPreFlight(requestContext, responseContext);
 			
 		case ACTUAL:
 			filterActualRequest(requestContext, responseContext);
 		}
 	}
 
     private void filterPreFlight(ContainerRequestContext requestContext,
                                  ContainerResponseContext responseContext) {
         String origin = requestContext.getHeaderString("Origin");
         if(!checkOrigin(origin))
             responseContext.setStatusInfo(Response.Status.FORBIDDEN);
 
         String methodHeader = requestContext.getHeaderString("Access-Control-Request-Method");
         if(methodHeader == null)
             responseContext.setStatusInfo(Response.Status.FORBIDDEN);
 
         String requestHeaders =
                 requestContext.getHeaderString("Access-Control-Request-Headers");
         logger.info("requestHeader: "+requestHeaders);
 
         MultivaluedMap<String, Object> headers = responseContext.getHeaders();
         headers.putSingle("Access-Control-Allow-Origin", origin);
         headers.putSingle("Access-Control-Allow-Credentials", "true");
         headers.putSingle("Access-Control-Allow-Methods", methodHeader);
         headers.putSingle("Access-Control-Allow-Headers", "x-requested-with," +
                (requestHeaders == null ? "" : requestHeaders));
     }
 	
 	private void filterActualRequest(ContainerRequestContext requestContext,
                                      ContainerResponseContext responseContext) {
 		String origin = requestContext.getHeaderString("Origin");
 		if(!checkOrigin(origin))
             responseContext.setStatusInfo(Response.Status.FORBIDDEN);
 
         MultivaluedMap<String, Object> headers = responseContext.getHeaders();
         headers.putSingle("Access-Control-Allow-Origin", origin);
         headers.putSingle("Access-Control-Allow-Credentials", "true");
 	}
 
 
 
 
 	public static boolean checkOrigin(String origin){
 		if(origin == null)
 			return false;
 		Matcher hostMatcher = hostPattern.matcher(origin);
 		return hostMatcher.matches();
 	}
 
 	/**
 	 * @param hh
 	 * @return
 	 */
 //	public static Response buildResponse(String origin) {
 ////		if(!checkOrigin(origin))
 ////			throw new WebApplicationException(401);
 //		
 //		ResponseBuilder resp = Response.ok();
 //		resp.header("Access-Control-Allow-Origin", origin);
 //		resp.header("Access-Control-Allow-Credentials", "true");
 //		return resp.build();
 //	}
 
 	
 	
 	
 
 }
