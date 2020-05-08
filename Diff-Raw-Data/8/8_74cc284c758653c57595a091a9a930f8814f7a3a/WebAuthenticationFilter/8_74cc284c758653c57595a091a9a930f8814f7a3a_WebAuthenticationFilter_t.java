 package edu.gatech.oad.rocket.findmythings.server;
 
 import java.util.Collection;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 
 import org.apache.shiro.SecurityUtils;
 import org.apache.shiro.authc.AuthenticationException;
 import org.apache.shiro.authc.AuthenticationToken;
 import org.apache.shiro.authc.DisabledAccountException;
 import org.apache.shiro.authc.ExcessiveAttemptsException;
 import org.apache.shiro.authc.IncorrectCredentialsException;
 import org.apache.shiro.authc.LockedAccountException;
 import org.apache.shiro.authc.UnknownAccountException;
 import org.apache.shiro.session.Session;
 import org.apache.shiro.subject.Subject;
 import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;
 
 import com.google.common.collect.Maps;
 
 import edu.gatech.oad.rocket.findmythings.server.util.*;
 import edu.gatech.oad.rocket.findmythings.server.web.*;
 
 public class WebAuthenticationFilter extends FormAuthenticationFilter {
 
 	static final Logger LOGGER = Logger.getLogger(WebAuthenticationFilter.class.getName());
 
	public WebAuthenticationFilter() {
 		super();
 		setUsernameParam(Parameters.USERNAME);
 		setPasswordParam(Parameters.PASSWORD);
 		setRememberMeParam(Parameters.REMEMBER_ME);
 	}
 
 	@Override
 	protected void setFailureAttribute(ServletRequest request, AuthenticationException ae) {
 		if (ae instanceof UnknownAccountException) {
 			request.setAttribute(Parameters.FAILURE_REASON, Errors.Login.NO_SUCH_USER);
 		} else if (ae instanceof IncorrectCredentialsException) {
 			request.setAttribute(Parameters.FAILURE_REASON, Errors.Login.BAD_PASSWORD);
 		} else if (ae instanceof LockedAccountException) {
 			request.setAttribute(Parameters.FAILURE_REASON, Errors.Login.ACCNT_LOCKED);
 		} else if (ae instanceof DisabledAccountException) {
 			request.setAttribute(Parameters.FAILURE_REASON, Errors.Login.ACCT_DISABLE);
 		} else if (ae instanceof ExcessiveAttemptsException) {
 			request.setAttribute(Parameters.FAILURE_REASON, Errors.Login.MANY_ATTEMPT);
 		} else {
 			request.setAttribute(Parameters.FAILURE_REASON, Errors.Login.INVALID_DATA);
 		}
 	}
 
 	@Override
 	protected boolean executeLogin(ServletRequest request, ServletResponse response) throws Exception {
         AuthenticationToken token = createToken(request, response);
         Subject subject = SecurityUtils.getSubject();
 		Session originalSession = subject.getSession();
 
         Map<Object, Object> attributes = Maps.newLinkedHashMap();
         Collection<Object> keys = originalSession.getAttributeKeys();
         for(Object key : keys) {
             Object value = originalSession.getAttribute(key);
             if (value != null) {
                 attributes.put(key, value);
             }
         }
         originalSession.stop();
 
         try {
 		subject.login(token);
 
 		Session newSession = subject.getSession();
             for(Object key : attributes.keySet() ) {
                 newSession.setAttribute(key, attributes.get(key));
             }
 
            LOGGER.fine("Creating a new instance of DatastoreRealm");
 
             return onLoginSuccess(token, subject, request, response);
         } catch (AuthenticationException e) {
            LOGGER.fine("Failed log in.");
             setFailureAttribute(request, e);
 		return onLoginFailure(token, e, request, response);
         }
 	}
 
 }
