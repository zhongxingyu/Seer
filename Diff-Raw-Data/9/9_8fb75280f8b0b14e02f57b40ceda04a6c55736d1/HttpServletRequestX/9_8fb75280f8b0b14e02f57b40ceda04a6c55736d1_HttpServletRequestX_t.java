 package httpServletRequestX;
 
 import httpServletRequestX.accept.header.AcceptHeader;
 import httpServletRequestX.accept.header.AcceptHeaderImpl;
 import httpServletRequestX.inject.GuiceModule;
 
 import javax.servlet.http.HttpServletRequest;
 
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 
 /**
  * This library wraps the {@see HttpServletRequest} and provides a set of convinience functions to handle with the Accept Content type options.
  * 
  * @author Marco Reinwarth <marcoreinwarth@gmail.com>
  */
public class HttpServletRequestX extends HttpServletRequestXWrapper {
 
     private static Injector     injector;
     private static AcceptHeader ACCEPT_HEADER;
 
     /**
      * Guice injection preparations
      */
     static {
         injector = Guice.createInjector(new GuiceModule());
         ACCEPT_HEADER = injector.getInstance(AcceptHeaderImpl.class);
     }
 
     /**
      * Base constructor that consumes the {@see HttpServletRequest} object
      * 
      * @param request
      *            the {@see HttpServletRequest} object that has to be wrapped
      */
     public HttpServletRequestX(HttpServletRequest request) {
         super(request, ACCEPT_HEADER);
     }
 
 }
