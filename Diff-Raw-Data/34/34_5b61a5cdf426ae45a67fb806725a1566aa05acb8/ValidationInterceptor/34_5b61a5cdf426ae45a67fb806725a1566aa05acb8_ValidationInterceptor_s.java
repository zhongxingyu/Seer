 package cs.wintoosa.controller.interceptor;
 
 import com.google.gson.*;
 import cs.wintoosa.service.ChecksumChecker;
 import java.io.IOException;
 import java.util.Map.Entry;
 import java.util.Set;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
 import java.util.logging.*;
 import javax.servlet.ServletInputStream;
 import javax.servlet.http.HttpServletRequestWrapper;
 
 /**
  *
  * @author jonimake
  */
 public class ValidationInterceptor extends HandlerInterceptorAdapter {
     
     private final static Logger logger = Logger.getLogger(ValidationInterceptor.class.getName()); 
 
     @Override
     public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
         
         boolean isOk = super.preHandle(request, response, handler);
         
         if(!request.getMethod().equalsIgnoreCase("PUT"))
             return isOk; //only handle PUTs
         
         JsonObject json = convertToJsonObject(request);
        
         isOk = isValid(json);
         return isOk;
     }
     
     private JsonObject convertToJsonObject(HttpServletRequest request) {
         logger.info("method = " + request.getMethod() + "\n" +
                     "requestUri = " + request.getRequestURI() + "\n" +
                     "queryString = " + request.getQueryString());
         if(request.getContentLength() == -1)
             return null;
         byte[] buffer = new byte[request.getContentLength()];
         try{
             request.getInputStream().readLine(buffer, 0, request.getContentLength());
         } catch (IOException e) {
             System.out.println(e.toString());
         }
         
         String data = new String(buffer);
         logger.info("data = " + data);
         
         JsonObject json = null;
         JsonParser parser = new JsonParser();
         if(request.getQueryString() != null) {
             logger.info("queryString = " + request.getQueryString());
             JsonElement parse = parser.parse(request.getQueryString());
             json = parse.getAsJsonObject();
             logger.info("json: " + json.toString());
         }
         return json;
     }
     
     private boolean isValid(JsonObject json) {
         
         if(json == null)
             return false;
         
         JsonElement checksumJson = json.get("checksum");
         
         if(checksumJson == null)
             return false;
         
         String checksum = checksumJson.getAsString();
         json.remove("checksum");
         String calculatedChecksum = ChecksumChecker.calcSHA1(json.toString());
         
         logger.info(checksum + " equals " + calculatedChecksum + ": " + checksum.equals(calculatedChecksum));
         
         return calculatedChecksum.equals(checksum);
     }
 }
