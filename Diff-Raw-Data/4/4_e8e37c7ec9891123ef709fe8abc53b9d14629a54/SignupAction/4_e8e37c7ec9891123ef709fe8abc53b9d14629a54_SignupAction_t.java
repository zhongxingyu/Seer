 package nl.bhit.mtor.server.webapp.action;
 
 
 import org.apache.struts2.ServletActionContext;
 
 import nl.bhit.mtor.Constants;
 import nl.bhit.mtor.model.Role;
 import nl.bhit.mtor.model.Status;
 import nl.bhit.mtor.model.User;
 import nl.bhit.mtor.server.webapp.util.RequestUtil;
 import nl.bhit.mtor.service.UserExistsException;
 
 import org.springframework.mail.MailException;
 import org.springframework.security.access.AccessDeniedException;
 import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
 import org.springframework.security.core.context.SecurityContextHolder;
 
 import javax.servlet.http.HttpServletResponse;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Action to allow new users to sign up.
  */
 public class SignupAction extends BaseAction {
     private static final long serialVersionUID = 6558317334878272308L;
     private User user;
     private String cancel;
 
     public void setCancel(String cancel) {
         this.cancel = cancel;
     }
 
     public void setUser(User user) {
         this.user = user;
     }
 
     /**
      * Return an instance of the user - to display when validation errors occur
      * @return a populated user
      */
     public User getUser() {
         return user;
     }
 
     /**
      * When method=GET, "input" is returned. Otherwise, "success" is returned.
      * @return cancel, input or success
      */
     public String execute() {
         if (cancel != null) {
             return CANCEL;
         }
         if (ServletActionContext.getRequest().getMethod().equals("GET")) {
             return INPUT;
         }
         return SUCCESS;
     }
 
     /**
      * Returns "input"
      * @return "input" by default
      */
     public String doDefault() {
         return INPUT;
     }
 
     /**
      * Save the user, encrypting their passwords if necessary
      * @return success when good things happen
      * @throws Exception when bad things happen
      */
     public String save() throws Exception {
         user.setEnabled(true);
 
         
         user.addRole(roleManager.getRole(Constants.USER_ROLE));
         //remove according new requirement for issue #30 signup role
 //        // Set the default anonymous role on this new user
 //        if (roleManager.getRole(Constants.ANONYMOUS_ROLE) == null) {
 //        	roleManager.saveRole(new Role(Constants.ANONYMOUS_ROLE));
 //        }        
 //        user.addRole(roleManager.getRole(Constants.ANONYMOUS_ROLE));
         
         user.setEmail(user.getUsername());
 
         try {
             user = userManager.saveUser(user);
         } catch (AccessDeniedException ade) {
             // thrown by UserSecurityAdvice configured in aop:advisor userManagerSecurity 
             log.warn(ade.getMessage());
             getResponse().sendError(HttpServletResponse.SC_FORBIDDEN);
             return null; 
         } catch (UserExistsException e) {
             log.warn(e.getMessage());
             List<Object> args = new ArrayList<Object>();
             args.add(user.getUsername());
             args.add(user.getEmail());
             addActionError(getText("errors.existing.user", args));
 
             // redisplay the unencrypted passwords
             user.setPassword(user.getConfirmPassword());
             return INPUT;
         }
 
         saveMessage(getText("user.registered"));
         getSession().setAttribute(Constants.REGISTERED, Boolean.TRUE);
 
         // log user in automatically
         UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                 user.getUsername(), user.getConfirmPassword(), user.getAuthorities());
         auth.setDetails(user);
         SecurityContextHolder.getContext().setAuthentication(auth);
 
         // Send an account information e-mail
         mailMessage.setSubject(getText("signup.email.subject"));
 
         try {
             sendUserMessage(user, getText("signup.email.message"), RequestUtil.getAppURL(getRequest()));
         } catch (MailException me) {
             addActionError(me.getMostSpecificCause().getMessage());
         }
 
         return SUCCESS;
     }
    
    public List getStatusList() {
		return Status.getAsList();
	}
 }
