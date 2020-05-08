 package vio.service.rest.filters;
 
 import java.io.IOException;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  * This filter wraps CORS and JSONP requests. Mainly for debugging purposes.
  * Note: this opens a security hole on the site
  *
  * Suppose there is REST service /doc that
  * returns an JSON in form: {"field":"value"}.
  * If you activate this filter
  * the request is in the form of /doc?callback = someFunc
  * return
  * someFunc({"field":"value"})
  *
  * @see http://ru.wikipedia.org/wiki/JSONP
  * @see https://developer.mozilla.org/en-US/docs/HTTP/Access_control_CORS
  * @author moroz
  */
 public class CORSandJSONPFilter extends GenericFilter {
 
     //this parameters must be in web.xml
     public static final String JSONP_PARAMETER_NAME = "JSONPParameter";
     public static final String EPOSE_HEADERS_PARAMETER_NAME = "exposeHeadersList";
     //cors support
     public static final String //request CORS headers
             REQUEST_ORIGIN_HEADER = "Origin",
             REQUEST_ALLOW_HEADERS = "Access-Control-Request-Headers",
             //request CORS headers
             RESPONSE_ALLOW_ORIGIN = "Access-Control-Allow-Origin",
             RESPONSE_ALLOW_METHODS = "Access-Control-Allow-Methods",
             RESPONSE_ALLOW_HEADERS = "Access-Control-Allow-Headers",
             RESPONSE_ALLOW_AGE = "Access-Control-Max-Age",
             RESPONSE_EXPOSE_HEADERS = "Access-Control-Expose-Headers",
             RESPONSE_ALLOW_CREDENTIALS = "Access-Control-Allow-Credentials";
 
     public CORSandJSONPFilter() {
     }
 
     @Override
     public final void doBeforeProcessing(ServletRequest request, ServletResponse response)
             throws IOException, ServletException {
 
         log("JSONPFilter:DoBeforeProcessing");
 
         String jsonPadding = getJsonPadding(request);
 
         HttpServletResponse httpResponse = (HttpServletResponse) response;
         HttpServletRequest httpRequest = (HttpServletRequest) request;
 
         if ((jsonPadding != null) && (jsonPadding.length() > 0)) {
             httpResponse.getOutputStream().println(jsonPadding + "(");
         } else if (getOrigin(httpRequest) != null) {
 
             // this security hole for debug&devel purposes 
            setOriginHeaders(httpRequest, httpResponse);// in case Request Method:GET we need before processing
         }
 
     }
 
     private void setOriginHeaders(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
         httpResponse.setHeader(RESPONSE_ALLOW_ORIGIN, httpRequest.getHeader(REQUEST_ORIGIN_HEADER));
         httpResponse.setHeader(RESPONSE_ALLOW_METHODS, "*");
         httpResponse.setHeader(RESPONSE_ALLOW_HEADERS, httpRequest.getHeader(REQUEST_ALLOW_HEADERS));
         httpResponse.setHeader(RESPONSE_EXPOSE_HEADERS, getExposeHeadersList());
         httpResponse.setHeader(RESPONSE_ALLOW_CREDENTIALS, "true");
         httpResponse.setHeader(RESPONSE_ALLOW_AGE, "1728000");
     }
 
     @Override
     public final void doAfterProcessing(ServletRequest request, ServletResponse response)
             throws IOException, ServletException {
 
         log("JSONPFilter:DoAfterProcessing");
 
         HttpServletRequest httpRequest = (HttpServletRequest) request;
         HttpServletResponse httpResponse = (HttpServletResponse) response;
 
         String jsonPadding = getJsonPadding(request);
 
         if ((jsonPadding != null) && (jsonPadding.length() > 0)) {
 
             response.getOutputStream().println(")"); //close function bracket
 
         } else if (getOrigin(httpRequest) != null) {
             // this security hole for debug&devel purposes 
            setOriginHeaders(httpRequest, httpResponse);// in case Request Method:GET we need after processing
         }
     }
 
     private String getExposeHeadersList() {
         String ret = getFilterConfig().getInitParameter(EPOSE_HEADERS_PARAMETER_NAME);
         return ret != null ? ret : "";
     }
 
     private String getJsonPadding(ServletRequest request) {
         try {
             return request.getParameter(
                     getFilterConfig().getInitParameter(JSONP_PARAMETER_NAME)
             );
         } catch (Exception e) {
             return null;
         }
     }
 
     private String getOrigin(HttpServletRequest request) {
         String result = request.getHeader(REQUEST_ORIGIN_HEADER);
         log("JSONPFilter:Request Origin:" + result);
         return result;
     }
 }
