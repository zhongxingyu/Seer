 package war.webapp.action;
 
 import java.io.IOException;
 import java.io.Serializable;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.cxf.common.util.StringUtils;
 import org.springframework.security.AccessDeniedException;
 import org.springframework.security.Authentication;
 import org.springframework.security.AuthenticationTrustResolver;
 import org.springframework.security.AuthenticationTrustResolverImpl;
 import org.springframework.security.context.SecurityContext;
 import org.springframework.security.context.SecurityContextHolder;
 
 import war.webapp.Constants;
 import war.webapp.model.Role;
 import war.webapp.model.User;
 import war.webapp.service.RoleManager;
 import war.webapp.service.UserExistsException;
 import war.webapp.util.ConvertUtil;
 
 /**
  * JSF Page class to handle editing a user with a form.
  * 
  * @author mraible
  */
 public class UserForm extends BasePage implements Serializable {
     private static final long serialVersionUID = -1141119853856863204L;
     private RoleManager roleManager;
     private String id;
     private User user = new User();
     private Map<String, String> availableRoles;
     private String[] userRoles;
 
     public void setId(String id) {
         this.id = id;
     }
 
     public User getUser() {
         return user;
     }
 
     public void setUser(User user) {
         this.user = user;
     }
 
     public void setRoleManager(RoleManager roleManager) {
         this.roleManager = roleManager;
     }
 
     public String add() {
         user = new User();
        user.setVersion(null);
         user.setEnabled(true);
         user.addRole(new Role(Constants.USER_ROLE));
         return "editProfile";
     }
 
     public String cancel() {
         if (log.isDebugEnabled()) {
             log.debug("Entering 'cancel' method");
         }
 
         if (!"list".equals(getParameter("from"))) {
             return "mainMenu";
         } else {
             return "cancel";
         }
     }
 
     public String edit() {
         HttpServletRequest request = getRequest();
 
         // if a user's id is passed in
         if (id != null) {
             // lookup the user using that id
             user = userManager.getUser(id);
         } else {
             user = userManager.getUserByUsername(request.getRemoteUser());
         }
 
         if (user.getUsername() != null && isRememberMe()) {
             // if user logged in with remember me, display a warning that
             // they can't change passwords
             log.debug("checking for remember me login...");
             log.trace("User '" + user.getUsername() + "' logged in with cookie");
             addMessage("userProfile.cookieLogin");
         }
 
         return "editProfile";
     }
 
     /**
      * Convenience method for view templates to check if the user is logged in
      * with RememberMe (cookies).
      * 
      * @return true/false - false if user interactively logged in.
      */
     public boolean isRememberMe() {
         if (user != null && user.getId() == null)
             return false; // check for add()
 
         AuthenticationTrustResolver resolver = new AuthenticationTrustResolverImpl();
         SecurityContext ctx = SecurityContextHolder.getContext();
 
         if (ctx != null) {
             Authentication auth = ctx.getAuthentication();
             return resolver.isRememberMe(auth);
         }
         return false;
     }
 
     public String save() throws IOException {
         setUserRoles(getRoles());
         generateFloor();
         generateUsername();
         generatePassword();
         
         for (int i = 0; (userRoles != null) && (i < userRoles.length); i++) {
             String roleName = userRoles[i];
             user.addRole(roleManager.getRole(roleName));
         }
         
         Integer originalVersion = user.getVersion();
 
         try {
             user = userManager.saveUser(user);
         } catch (AccessDeniedException ade) {
             // thrown by UserSecurityAdvice configured in aop:advisor
             // userManagerSecurity
             log.warn(ade.getMessage());
             getResponse().sendError(HttpServletResponse.SC_FORBIDDEN);
             return null;
         } catch (UserExistsException e) {
             addError("errors.existing.user", new Object[] { user.getUsername() });
 
             // reset the version # to what was passed in
             user.setVersion(originalVersion);
             return "editProfile";
         }
 
         if (!"list".equals(getParameter("from"))) {
             addMessage("user.saved");
             return "mainMenu"; // return to main Menu
         } else {
             addMessage("user.added", user.getFullName());
             return "list"; // return to list screen
         }
     }
     
     private String[] getRoles() {
         if (getRequest().getParameterValues("userForm:userRoles") == null) {
             return new String[] { Constants.USER_ROLE };
         }
         return getRequest().getParameterValues("userForm:userRoles");
     }
 
     private void generateFloor() {
         int hostelFloor = getFloorFromRoom(user.getAddress().getHostelRoom());
         user.getAddress().setHostelFloor(hostelFloor);
     }
 
     private int getFloorFromRoom(Integer hostelRoom) {
         return hostelRoom / 100;
     }
 
     private void generateUsername() {
         if (StringUtils.isEmpty(user.getUsername())) {
             StringBuilder username = new StringBuilder();
             username.append(user.getAddress().getHostelRoom()).append(user.getLastName())
                     .append(user.getFirstName().charAt(0)).append(user.getMiddleName().charAt(0));
             user.setUsername(username.toString());
            //TODO find out why tomcat set to this fields Integer(0) and Long(0)
            //user.setVersion(null);
            //user.setId(null);
         }
     }
     
     private void generatePassword() {
         if (StringUtils.isEmpty(user.getPassword())) {
             user.setPassword("pass");
         }
     }
 
     public String delete() {
         userManager.removeUser(getUser().getId().toString());
         addMessage("user.deleted", getUser().getFullName());
 
         return "list";
     }
 
     /**
      * Convenience method to determine if the user came from the list screen
      * 
      * @return String
      */
     public String getFrom() {
         if ((id != null) || (getParameter("editUser:add") != null) || ("list".equals(getParameter("from")))) {
             return "list";
         }
 
         return "";
     }
 
     // Form Controls ==========================================================
     @SuppressWarnings({ "unchecked", "rawtypes" })
     public Map<String, String> getAvailableRoles() {
         if (availableRoles == null) {
             List roles = (List) getServletContext().getAttribute(Constants.AVAILABLE_ROLES);
             availableRoles = ConvertUtil.convertListToMap(roles);
         }
 
         return availableRoles;
     }
 
     public String[] getUserRoles() {
         userRoles = new String[user.getRoles().size()];
 
         int i = 0;
 
         if (userRoles.length > 0) {
             for (Role role : user.getRoles()) {
                 userRoles[i] = role.getName();
                 i++;
             }
         }
 
         return userRoles;
     }
 
     public void setUserRoles(String[] userRoles) {
         this.userRoles = userRoles;
     }
 
 }
