 package de.flower.rmt.ui.page.error;
 
 import de.flower.common.ui.util.LoggingUtils;
 import org.apache.wicket.protocol.http.servlet.ForwardAttributes;
 import org.apache.wicket.request.Request;
 import org.apache.wicket.request.RequestHandlerStack;
 import org.apache.wicket.request.http.handler.RedirectRequestHandler;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.stereotype.Component;
 
 import javax.annotation.Resource;
 import javax.servlet.http.HttpServletRequest;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Redirects commonly observed old bookmark errors to sensible pages. Avoids displaying 404 page
  * for users that are too lazy to update their bookmarks.
  *
  * @author flowerrrr
  */
 @Component
 public class PageNotFoundAutoRedirecter {
 
     private final static Logger log = LoggerFactory.getLogger(PageNotFoundAutoRedirecter.class);
 
     @Resource(name = "page404RedirectMapping")
     private Map<String, String> redirectMapping = new HashMap<>();
 
     public void checkAutoRedirect(final Request request) {
         HttpServletRequest servletRequest = (HttpServletRequest) request.getContainerRequest();
         ForwardAttributes forwardAttributes = ForwardAttributes.of(servletRequest);
 
         String url = redirectMapping.get(forwardAttributes.getServletPath());
         if (url != null) {
            log.info("Redirecting [{}] to [{}]", LoggingUtils.toString(request), url);
             throw new RequestHandlerStack.ReplaceHandlerException(new RedirectRequestHandler(url), false);
         }
     }
 }
