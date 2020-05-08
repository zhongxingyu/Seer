 package nz.ac.auckland.jetty;
 
 import org.eclipse.jetty.security.Authenticator;
 import org.eclipse.jetty.security.ConstraintSecurityHandler;
 import org.eclipse.jetty.security.SecurityHandler;
 import org.eclipse.jetty.security.ServerAuthException;
 import org.eclipse.jetty.server.Authentication;
 import org.eclipse.jetty.server.Request;
 import org.eclipse.jetty.server.Response;
 import org.eclipse.jetty.server.UserIdentity;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 
 public class RemoteUserSecurityHandler extends ConstraintSecurityHandler {
   private static final Logger log = LoggerFactory.getLogger(RemoteUserSecurityHandler.class);
 
 
   public RemoteUserSecurityHandler() {
     RemoteUserUtils.initialize(); // look for an outside role source
 
     setAuthenticator(new Authenticator(){
       @Override
       public void setConfiguration(AuthConfiguration configuration) {
       }
 
       @Override
       public String getAuthMethod() {
         return "form";
       }
 
	    @Override
	    public void prepareRequest(ServletRequest servletRequest) {

	    }

	    @Override
       public Authentication validateRequest(ServletRequest request, ServletResponse response, boolean mandatory) throws ServerAuthException {
         return ((Request)request).getAuthentication();
       }
 
       @Override
       public boolean secureResponse(ServletRequest request, ServletResponse response, boolean mandatory, Authentication.User validatedUser) throws ServerAuthException {
         return false;
       }
     });
   }
 
   @Override
   public void handle(String pathInContext, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
     baseRequest.setAuthentication(RemoteUserUtils.ensureRemoteUserSetIfIncluded(baseRequest));
 
     super.handle(pathInContext, baseRequest, request, response);    //To change body of overridden methods use File | Settings | File Templates.
   }
 }
