 package ch.cern.atlas.apvs.server;
 
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpSession;
 
import org.atmosphere.cpr.AtmosphereConfig;
 import org.atmosphere.cpr.AtmosphereResource;
 import org.atmosphere.handler.ReflectorServletProcessor;
 import org.slf4j.LoggerFactory;
 
 /**
  * @author Mark Donszelmann
  */
 public class AtmosphereHandler extends ReflectorServletProcessor {
 	private org.slf4j.Logger log = LoggerFactory.getLogger(getClass().getName());
 
     @Override
    public void init(AtmosphereConfig servletConfig) throws ServletException {
         super.init(servletConfig);
         Logger.getLogger("").setLevel(Level.INFO);
         Logger.getLogger("org.atmosphere.gwt").setLevel(Level.ALL);
         Logger.getLogger("ch.cern.atlas.apvs").setLevel(Level.ALL);
         Logger.getLogger("").getHandlers()[0].setLevel(Level.ALL);
         log.info("Updated logging levels");
     }
     
     @Override
     public void onRequest(AtmosphereResource resource) throws IOException {
         resource.getBroadcaster().setID("GWT_COMET");
         HttpSession session = resource.getRequest().getSession(false);
         if (session != null) {
             log.debug("Got session with id: " + session.getId());
             log.debug("Time attribute: " + session.getAttribute("time"));
         } else {
         	log.warn("No session");
         }
         log.info("Url: " + resource.getRequest().getRequestURL()
                     + "?" + resource.getRequest().getQueryString());       
         String agent = resource.getRequest().getHeader("user-agent");
         log.info(agent);
     }
     
     @Override
     public void destroy() {
         log.info("Comet disconnected");
     }
 
 }
