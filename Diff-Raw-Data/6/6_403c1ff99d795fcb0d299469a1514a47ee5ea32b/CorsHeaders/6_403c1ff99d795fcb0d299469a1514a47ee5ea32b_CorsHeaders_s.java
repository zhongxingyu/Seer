 package org.jcors.model;
 
 /**
  * Http Headers used in CORS
  * 
  * @author Diego Silveira
  */
 public final class CorsHeaders {
 
 	private CorsHeaders() {
 	}
 	
 	public static final String ORIGIN_HEADER = "Origin";
 	public static final String ACCESS_CONTROL_ALLOW_ORIGIN_HEADER = "Access-Control-Allow-Origin";
 	public static final String ACCESS_CONTROL_ALLOW_CREDENTIALS_HEADER = "Access-Control-Allow-Credentials";
 	public static final String ACCESS_CONTROL_ALLOW_METHODS_HEADER = "Access-Control-Allow-Methods";
	public static final String ACCESS_CONTROL_ALLOW_HEADERS_HEADER = "Access-Control-Allow-Headers ";
 	public static final String ACCESS_CONTROL_REQUEST_METHOD_HEADER = "Access-Control-Request-Method";
 	public static final String ACCESS_CONTROL_REQUEST_HEADERS_HEADER = "Access-Control-Request-Headers";
 	public static final String ACCESS_CONTROL_EXPOSE_HEADERS_HEADER = "Access-Control-Expose-Headers";
 	public static final String ACCESS_CONTROL_MAX_AGE_HEADER = "Access-Control-Max-Age";
 
}
