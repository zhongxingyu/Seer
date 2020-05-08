 package be.mira.codri.server.spring;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.slf4j.Logger;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver;
 
 /**
  *
  * @author tim
  */
 public class LoggingExceptionResolver extends DefaultHandlerExceptionResolver {
     //
     // Member data
     //
     
     @Slf4jLogger
     private Logger mLogger;
     
     
     //
     // Functionality
     //
 
     @Override
     protected final ModelAndView doResolveException(final HttpServletRequest iRequest, final HttpServletResponse iResponse, final Object iHandler, final Exception iException) {
         mLogger.error("Error handling {}", iRequest.getRequestURL().toString(), iException);
         return super.doResolveException(iRequest, iResponse, iHandler, iException);
     }
 }
