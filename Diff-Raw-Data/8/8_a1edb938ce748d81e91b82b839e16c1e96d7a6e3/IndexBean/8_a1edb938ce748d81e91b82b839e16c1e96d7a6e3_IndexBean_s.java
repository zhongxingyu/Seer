 /*
  * To change this template, choose Tools | Templates and open the template in the editor.
  */
 package com.blazebit.security.web.bean.main;
 
 import java.io.IOException;
 import java.io.Serializable;
 import java.security.Principal;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 import javax.annotation.PostConstruct;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ViewScoped;
 import javax.faces.context.ExternalContext;
 import javax.faces.context.FacesContext;
 import javax.faces.event.AjaxBehaviorEvent;
 import javax.faces.event.ValueChangeEvent;
 import javax.inject.Inject;
 import javax.security.auth.Subject;
 import javax.security.auth.login.LoginContext;
 import javax.security.auth.login.LoginException;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.commons.lang3.StringUtils;
 import org.jboss.logging.Logger;
 
 import com.blazebit.security.PermissionManager;
 import com.blazebit.security.auth.model.Credentials;
 import com.blazebit.security.impl.model.Company;
 import com.blazebit.security.impl.model.User;
 import com.blazebit.security.impl.service.resource.UserDataAccess;
 import com.blazebit.security.web.context.UserSession;
 import com.blazebit.security.web.service.api.CompanyService;
 import com.blazebit.security.web.service.api.UserService;
 
 /**
  * 
  * @author cuszk
  */
 @ManagedBean(name = "indexBean")
 @ViewScoped
 public class IndexBean implements Serializable {
 
     protected Logger log = Logger.getLogger(IndexBean.class);
 
     /**
      * 
      */
     private static final long serialVersionUID = 1L;
     @Inject
     private UserService userService;
 
     @Inject
     private UserSession userSession;
 
     @Inject
     private PermissionManager permissionManager;
 
     @Inject
     private UserDataAccess userDataAccess;
 
     private List<User> users = new ArrayList<User>();
     private Company selectedCompany;
     List<Company> companies;
 
     @Inject
     private CompanyService companyService;
 
     @Inject
     private LoginContext loginContext;
 
     @Inject
     private Credentials credentials;
 
     @PostConstruct
     public void init() {
         companies = companyService.findCompanies();
        if (!companies.isEmpty() && userSession.getSelectedCompany()==null) {
             setSelectedCompany(companyService.findCompanies().get(0));
            users = userService.findUsers(selectedCompany);
            users.add(0, userService.findUser("superAdmin", selectedCompany));
         }
     }
 
     public void logInAs(User user) {
         credentials.setLogin(String.valueOf(user.getId()));
         credentials.setPassword("");
         ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
         HttpServletRequest request = (HttpServletRequest) externalContext.getRequest();
 
         if (!isLoggedIn()) {
             try {
                 loginContext.login();
                 if (loginContext.getSubject() != null) {
 
                     if (request.getUserPrincipal() != null) {
                         // guest is logged in
                         request.logout();
                     }
                     // sets the request user principal and the roles
                     request.authenticate((HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse());
 
                     if (request.getUserPrincipal() != null) {
                         // successful login+authentication
                         log.info("Logged in  as " + getLoggedInPrincipal(loginContext.getSubject()) + ", authenticated as " + request.getUserPrincipal().getName());
 
                         userSession.setAdmin(userService.findUser("superAdmin", selectedCompany));
                         userSession.setSelectedCompany(selectedCompany);
                     } else {
                         throw new LoginException("Failed to authenticate " + credentials.getLogin());
                     }
                 } else {
                     throw new LoginException("Failed to get logged in subject for " + credentials.getLogin());
                 }
             } catch (LoginException e) {
                 log.error("Login failed: " + credentials.getLogin(), e);
             } catch (IOException e) {
                 log.error("Authentication failed: " + credentials.getLogin(), e);
             } catch (ServletException e) {
                 log.error("Authentication failed: " + credentials.getLogin(), e);
             }
         } else {
             log.warn("Already logged in: " + getLoggedInPrincipal(loginContext.getSubject()));
         }
     }
 
     /**
      * checks logged in subject and principal
      * 
      * @return
      */
     private boolean isLoggedIn() {
         ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
         HttpServletRequest request = (HttpServletRequest) externalContext.getRequest();
         String loginContextPrincipal = getLoggedInPrincipal(loginContext.getSubject());
         String requestPrincipal = request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : null;
 
         boolean subjectExists = getLoggedInPrincipal(loginContext.getSubject()) != null;
         boolean principalExists = request.getUserPrincipal() != null && !request.getUserPrincipal().getName().equals("guest");
 
         return subjectExists && principalExists && StringUtils.equals(loginContextPrincipal, requestPrincipal);
     }
 
     /**
      * 
      * @param subject
      * @return logged in user name
      */
     private String getLoggedInPrincipal(Subject subject) {
         if (subject != null) {
             Set<Principal> principals = subject.getPrincipals();
             if (!principals.isEmpty()) {
                 Principal principal = principals.iterator().next();
                 if (principal != null) {
                     return principal.getName();
                 }
             }
         }
         return null;
     }
 
     /**
      * invalidates session, clears subject and principal
      */
     public void logout() {
         if (isLoggedIn()) {
             // if (loginContext.getSubject() != null) {
             clearLoginContext();
             // invalidate session
             ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
             HttpSession session = (HttpSession) externalContext.getSession(false);
             if (session != null) {
                 session.invalidate();
                 externalContext.getSession(true);
             }
         } else {
             log.warn("Already logged out.");
         }
     }
 
     private void clearLoginContext() {
         ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
         HttpServletRequest request = (HttpServletRequest) externalContext.getRequest();
         try {
             // LoginModule's logout. Clears subject and roles.
             if (loginContext.getSubject() != null) {
                 loginContext.logout();
             } else {
                 log.warn("LoginContext subject was found empty!");
             }
             // logout clear the user principal
             request.logout();
         } catch (LoginException e) {
             log.error("Logout " + request.getUserPrincipal() + " failed", e);
         } catch (ServletException e) {
             log.error("Logout " + request.getUserPrincipal() + " failed", e);
         }
     }
 
     public void beUser(User user) {
         FacesContext context = FacesContext.getCurrentInstance();
         ExternalContext externalContext = context.getExternalContext();
         if (isLoggedIn()) {
             String prevLoggedInUser = ((HttpServletRequest) externalContext.getRequest()).getUserPrincipal().getName();
             if (isLoggedIn()) {
                 clearLoginContext();
             } else {
                 log.warn("Already logged out.");
             }
             logInAs(user);
             // store previous logged in user in session
             externalContext.getSessionMap().put("user", Integer.valueOf(prevLoggedInUser));
         } else {
             log.warn("No one is logged in.");
         }
     }
 
     public void resetLoggedInUser() {
         FacesContext context = FacesContext.getCurrentInstance();
         ExternalContext externalContext = context.getExternalContext();
         HttpSession session = (HttpSession) externalContext.getSession(false);
         Integer userId = (Integer) session.getAttribute("user");
         User user = userDataAccess.findUser(userId);
         // clear prev logged in user
         session.setAttribute("user", null);
         if (user != null) {
             clearLoginContext();
             logInAs(user);
         } else {
             log.warn("No logged in user to restore.");
         }
     }
 
     public void reset(User user) {
         permissionManager.removeAllPermissions(user);
     }
 
     public List<User> getUsers() {
         return users;
     }
 
     public void setUsers(List<User> users) {
         this.users = users;
     }
 
     public List<Company> getCompanies() {
         return companies;
     }
 
     public Company getSelectedCompany() {
         return selectedCompany;
     }
 
     public void setSelectedCompany(Company selectedCompany) {
         // clearLoginContext();
         this.selectedCompany = selectedCompany;
         userSession.setSelectedCompany(selectedCompany);
         users = userService.findUsers(selectedCompany);
         users.add(0, userService.findUser("superAdmin", selectedCompany));
     }
 
     public void changeSelectedCompany(Company selectedCompany) {
         logout();
         setSelectedCompany(selectedCompany);
     }
 
     public void changeCompany(ValueChangeEvent event) {
         Company newCompany = (Company) event.getNewValue();
         setSelectedCompany(newCompany);
     }
 
     public void handleCompanyChange(AjaxBehaviorEvent event) {
         System.out.println("change");
     }
 
     public void saveCompanyConfiguration() {
         Company company = companyService.saveCompany(selectedCompany);
 
         // userSession.setSelectedCompany(company);
     }
 
 }
