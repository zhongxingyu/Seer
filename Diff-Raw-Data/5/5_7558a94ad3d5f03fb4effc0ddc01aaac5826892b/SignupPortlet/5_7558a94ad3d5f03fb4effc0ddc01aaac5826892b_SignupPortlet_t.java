 package org.gridsphere.portlets.core.registration;
 
 import org.gridsphere.portlet.impl.PortletURLImpl;
 import org.gridsphere.portlet.service.PortletServiceException;
 import org.gridsphere.provider.event.jsr.ActionFormEvent;
 import org.gridsphere.provider.event.jsr.RenderFormEvent;
 import org.gridsphere.provider.portlet.jsr.ActionPortlet;
 import org.gridsphere.provider.portletui.beans.MessageBoxBean;
 import org.gridsphere.provider.portletui.beans.TextFieldBean;
 import org.gridsphere.services.core.mail.MailMessage;
 import org.gridsphere.services.core.mail.MailService;
 import org.gridsphere.services.core.portal.PortalConfigService;
 import org.gridsphere.services.core.request.Request;
 import org.gridsphere.services.core.request.RequestService;
 import org.gridsphere.services.core.security.password.PasswordEditor;
 import org.gridsphere.services.core.security.password.PasswordManagerService;
 import org.gridsphere.services.core.security.role.PortletRole;
 import org.gridsphere.services.core.security.role.RoleManagerService;
 import org.gridsphere.services.core.user.User;
 import org.gridsphere.services.core.user.UserManagerService;
 
 import javax.portlet.*;
 import javax.servlet.http.HttpServletRequest;
 import java.util.*;
 
 public class SignupPortlet extends ActionPortlet {
 
     private static String ACTIVATE_ACCOUNT_LABEL = "activateaccount";
 
     private static long REQUEST_LIFETIME = 1000 * 60 * 60 * 24 * 3; // 3 days
 
     public static final String LOGIN_ERROR_FLAG = "LOGIN_FAILED";
     public static final Integer LOGIN_ERROR_UNKNOWN = new Integer(-1);
 
     public static final String DO_VIEW_USER_EDIT_LOGIN = "signup/createaccount.jsp"; //edit user
 
     private UserManagerService userManagerService = null;
     private RoleManagerService roleService = null;
     private PasswordManagerService passwordManagerService = null;
     private PortalConfigService portalConfigService = null;
     private RequestService requestService = null;
     private MailService mailService = null;
 
     private String activateAccountURL = null;
     private String denyAccountURL = null;
     private String notificationURL = null;
 
     public void init(PortletConfig config) throws PortletException {
 
         super.init(config);
 
         userManagerService = (UserManagerService) createPortletService(UserManagerService.class);
         roleService = (RoleManagerService) createPortletService(RoleManagerService.class);
         passwordManagerService = (PasswordManagerService) createPortletService(PasswordManagerService.class);
         requestService = (RequestService) createPortletService(RequestService.class);
         mailService = (MailService) createPortletService(MailService.class);
         portalConfigService = (PortalConfigService) createPortletService(PortalConfigService.class);
 
         DEFAULT_VIEW_PAGE = "doNewUser";
     }
 
 
     protected String getLocalizedText(HttpServletRequest req, String key) {
         Locale locale = req.getLocale();
         ResourceBundle bundle = ResourceBundle.getBundle("gridsphere.resources.Portlet", locale);
         return bundle.getString(key);
     }
 
     public void doNewUser(RenderFormEvent evt)
             throws PortletException {
 
         RenderRequest req = evt.getRenderRequest();
         RenderResponse res = evt.getRenderResponse();
 
         if (notificationURL == null) {
             notificationURL = res.createActionURL().toString();
         }
 
         if (activateAccountURL == null) {
             PortletURL accountURL = res.createActionURL();
             ((PortletURLImpl) accountURL).setAction("approveAccount");
             ((PortletURLImpl) accountURL).setLayout("register");
             ((PortletURLImpl) accountURL).setEncoding(false);
             activateAccountURL = accountURL.toString();
         }
         if (denyAccountURL == null) {
             PortletURL denyURL = res.createActionURL();
             ((PortletURLImpl) denyURL).setAction("denyAccount");
             ((PortletURLImpl) denyURL).setLayout("register");
             ((PortletURLImpl) denyURL).setEncoding(false);
             denyAccountURL = denyURL.toString();
         }
 
         boolean canUserCreateAccount = Boolean.valueOf(portalConfigService.getProperty(PortalConfigService.CAN_USER_CREATE_ACCOUNT)).booleanValue();
         if (!canUserCreateAccount) return;
 
         MessageBoxBean msg = evt.getMessageBoxBean("msg");
 
         String savePasswds = portalConfigService.getProperty(PortalConfigService.SAVE_PASSWORDS);
         if (savePasswds.equals(Boolean.TRUE.toString())) {
             req.setAttribute("savePass", "true");
         }
 
         String error = (String) req.getPortletSession(true).getAttribute("error");
         if (error != null) {
             msg.setValue(error);
             req.getPortletSession(true).removeAttribute("error");
         } else {
             String adminApproval = portalConfigService.getProperty("ADMIN_ACCOUNT_APPROVAL");
             if (adminApproval.equals(Boolean.TRUE.toString())) {
                 msg.setKey("LOGIN_ACCOUNT_CREATE_APPROVAL");
             } else {
                 msg.setKey("LOGIN_CREATE_ACCT");
             }
         }
         setNextState(req, DO_VIEW_USER_EDIT_LOGIN);
         log.debug("in doViewNewUser");
     }
 
     public void doSaveAccount(ActionFormEvent evt)
             throws PortletException {
         ActionRequest req = evt.getActionRequest();
         String savePasswds = portalConfigService.getProperty(PortalConfigService.SAVE_PASSWORDS);
         if (savePasswds.equals(Boolean.TRUE.toString())) {
             req.setAttribute("savePass", "true");
         }
 
         boolean canUserCreateAccount = Boolean.valueOf(portalConfigService.getProperty(PortalConfigService.CAN_USER_CREATE_ACCOUNT)).booleanValue();
         if (!canUserCreateAccount) return;
 
         try {
             //check if the user is new or not
             validateUser(evt);
             //new and valid user and will save it
             notifyNewUser(evt);
 
             setNextState(req, "doConfirmSave");
         } catch (PortletException e) {
             //invalid user, an exception was thrown
             //back to edit
             log.error("Could not create account: ", e);
             req.getPortletSession(true).setAttribute("error", e.getMessage());
             setNextState(req, DEFAULT_VIEW_PAGE);
         }
     }
 
     public void doConfirmSave(RenderFormEvent evt) {
         MessageBoxBean msg = evt.getMessageBoxBean("msg");
         msg.setKey("SIGNUP_CONFIRM");
         setNextState(evt.getRenderRequest(), "signup/confirmsave.jsp");
     }
 
     private void validateUser(ActionFormEvent event)
             throws PortletException {
         log.debug("Entering validateUser()");
         PortletRequest req = event.getActionRequest();
 
         // Validate user name
         String userName = event.getTextFieldBean("userName").getValue();
         if (userName.equals("")) {
             createErrorMessage(event, this.getLocalizedText(req, "USER_NAME_BLANK") + "<br />");
             throw new PortletException("user name is blank!");
         }
 
         if (this.userManagerService.existsUserName(userName)) {
             createErrorMessage(event, this.getLocalizedText(req, "USER_EXISTS") + "<br />");
             throw new PortletException("user exists already");
         }
 
         // Validate full name
 
         String firstName = event.getTextFieldBean("firstName").getValue();
         if (firstName.equals("")) {
             createErrorMessage(event, this.getLocalizedText(req, "USER_GIVENNAME_BLANK") + "<br />");
             throw new PortletException("first name is blank");
         }
 
         String lastName = event.getTextFieldBean("lastName").getValue();
         if (lastName.equals("")) {
             createErrorMessage(event, this.getLocalizedText(req, "USER_FAMILYNAME_BLANK") + "<br />");
             throw new PortletException("last name is blank");
         }
 
         // Validate e-mail
         String eMail = event.getTextFieldBean("emailAddress").getValue();
         if (eMail.equals("")) {
             createErrorMessage(event, this.getLocalizedText(req, "USER_NEED_EMAIL") + "<br />");
             throw new PortletException("email is blank");
         } else if ((eMail.indexOf("@") < 0)) {
             createErrorMessage(event, this.getLocalizedText(req, "USER_NEED_EMAIL") + "<br />");
             throw new PortletException("email address invalid");
         } else if ((eMail.indexOf(".") < 0)) {
             createErrorMessage(event, this.getLocalizedText(req, "USER_NEED_EMAIL") + "<br />");
             throw new PortletException("email address invalid");
         }
 
         //Validate password
 
         String savePasswds = portalConfigService.getProperty(PortalConfigService.SAVE_PASSWORDS);
         if (savePasswds.equals(Boolean.TRUE.toString())) {
             if (isInvalidPassword(event)) throw new PortletException("password no good!");
         }
 
         //retrieve the response
         String response = event.getTextFieldBean("captchaTF").getValue();
 
         String captchaValue = (String) req.getPortletSession(true).getAttribute(nl.captcha.servlet.Constants.SIMPLE_CAPCHA_SESSION_KEY, PortletSession.APPLICATION_SCOPE);
         if (!response.equals(captchaValue)) {
             createErrorMessage(event, this.getLocalizedText(req, "USER_CAPTCHA_MISMATCH"));
             throw new PortletException("captcha challenge mismatch!");
         }
 
         log.debug("Exiting validateUser()");
     }
 
     private boolean isInvalidPassword(ActionFormEvent event) {
         PortletRequest req = event.getActionRequest();
         // Validate password
         String passwordValue = event.getPasswordBean("password").getValue();
         String confirmPasswordValue = event.getPasswordBean("confirmPassword").getValue();
 
         if (passwordValue == null) {
             createErrorMessage(event, this.getLocalizedText(req, "USER_PASSWORD_NOTSET"));
             return true;
         }
 
         // Otherwise, password must match confirmation
         if (!passwordValue.equals(confirmPasswordValue)) {
             createErrorMessage(event, (this.getLocalizedText(req, "USER_PASSWORD_MISMATCH")) + "<br />");
             return true;
             // If they do match, then validate password with our service
         } else {
             passwordValue = passwordValue.trim();
 
             if (passwordValue.length() == 0) {
                 createErrorMessage(event, this.getLocalizedText(req, "USER_PASSWORD_BLANK"));
                 return true;
             }
             if (passwordValue.length() < 5) {
                 createErrorMessage(event, this.getLocalizedText(req, "USER_PASSWORD_TOOSHORT"));
                 return true;
             }
         }
         return false;
     }
 
     private User saveUser(Request request) {
         log.debug("Entering saveUser()");
         // Account request
 
         // Create edit account request
 
         User newuser = this.userManagerService.createUser();
 
         // Edit account attributes
         newuser.setUserName(request.getAttribute("userName"));
         newuser.setFirstName(request.getAttribute("firstName"));
         newuser.setLastName(request.getAttribute("lastName"));
         newuser.setFullName(request.getAttribute("lastName") + ", " + request.getAttribute("firstName"));
         newuser.setEmailAddress(request.getAttribute("emailAddress"));
         newuser.setOrganization(request.getAttribute("organization"));
 
         long now = Calendar.getInstance().getTime().getTime();
         newuser.setAttribute(User.CREATEDATE, String.valueOf(now));
 
         // Submit changes
         this.userManagerService.saveUser(newuser);
 
         String savePasswds = portalConfigService.getProperty(PortalConfigService.SAVE_PASSWORDS);
         if (savePasswds.equals(Boolean.TRUE.toString())) {
             PasswordEditor editor = passwordManagerService.editPassword(newuser);
             String password = request.getAttribute("password");
 
             editor.setValue(password);
             passwordManagerService.saveHashedPassword(editor);
         }
 
         // Save user role
         List<PortletRole> defaultRoles = roleService.getDefaultRoles();
         for (PortletRole role : defaultRoles) {
             roleService.addUserToRole(newuser, role);
         }
 
         log.debug("Exiting saveUser()");
         return newuser;
     }
 
 
     public void notifyNewUser(ActionFormEvent evt) throws PortletException {
         ActionRequest req = evt.getActionRequest();
         ActionResponse res = evt.getActionResponse();
         TextFieldBean emailTF = evt.getTextFieldBean("emailAddress");
 
         // create a request
         Request request = requestService.createRequest(ACTIVATE_ACCOUNT_LABEL);
         long now = Calendar.getInstance().getTime().getTime();
 
         request.setLifetime(new Date(now + REQUEST_LIFETIME));
 
         // request.setUserID(user.getID());
 
         request.setAttribute("userName", evt.getTextFieldBean("userName").getValue());
         request.setAttribute("firstName", evt.getTextFieldBean("firstName").getValue());
         request.setAttribute("lastName", evt.getTextFieldBean("lastName").getValue());
         request.setAttribute("emailAddress", evt.getTextFieldBean("emailAddress").getValue());
         request.setAttribute("organization", evt.getTextFieldBean("organization").getValue());
 
         // put hashed pass in request
         String savePasswds = portalConfigService.getProperty(PortalConfigService.SAVE_PASSWORDS);
         if (savePasswds.equals(Boolean.TRUE.toString())) {
             String pass = evt.getPasswordBean("password").getValue();
             pass = passwordManagerService.getHashedPassword(pass);
             request.setAttribute("password", pass);
         }
 
         requestService.saveRequest(request);
 
         MailMessage mailToUser = new MailMessage();
         mailToUser.setSender(portalConfigService.getProperty(PortalConfigService.MAIL_FROM));
         StringBuffer body = new StringBuffer();
 
 
         String activateURL = activateAccountURL + "&reqid=" + request.getOid();
 
         String denyURL = denyAccountURL + "&reqid=" + request.getOid();
 
         // check if this account request should be approved by an administrator
         boolean accountApproval = Boolean.valueOf(portalConfigService.getProperty(PortalConfigService.ADMIN_ACCOUNT_APPROVAL)).booleanValue();
         if (accountApproval) {
             String admin = portalConfigService.getProperty(PortalConfigService.PORTAL_ADMIN_EMAIL);
             mailToUser.setEmailAddress(admin);
             String mailSubject = portalConfigService.getProperty("LOGIN_ACCOUNT_APPROVAL_ADMIN_MAILSUBJECT");
             if (mailSubject == null) mailSubject = getLocalizedText(req, "LOGIN_ACCOUNT_APPROVAL_ADMIN_MAILSUBJECT");
             mailToUser.setSubject(mailSubject);
             String adminBody = portalConfigService.getProperty("LOGIN_ACCOUNT_APPROVAL_ADMIN_MAIL");
             if (adminBody == null) adminBody = getLocalizedText(req, "LOGIN_ACCOUNT_APPROVAL_ADMIN_MAIL");
             body.append(adminBody).append("\n\n");
             body.append(getLocalizedText(req, "LOGIN_ACCOUNT_APPROVAL_ALLOW")).append("\n\n");
             body.append(activateURL).append("\n\n");
             body.append(getLocalizedText(req, "LOGIN_ACCOUNT_APPROVAL_DENY")).append("\n\n");
             body.append(denyURL).append("\n\n");
         } else {
             mailToUser.setEmailAddress(emailTF.getValue());
 
             String mailSubjectHeader = portalConfigService.getProperty("LOGIN_ACTIVATE_SUBJECT");
             String loginActivateMail = portalConfigService.getProperty("LOGIN_ACTIVATE_BODY");
 
             if (mailSubjectHeader == null) mailSubjectHeader = getLocalizedText(req, "LOGIN_ACTIVATE_SUBJECT");
             mailToUser.setSubject(mailSubjectHeader);
 
             if (loginActivateMail == null) loginActivateMail = getLocalizedText(req, "LOGIN_ACTIVATE_MAIL");
             body.append(loginActivateMail).append("\n\n");
             body.append(activateURL).append("\n\n");
         }
 
         body.append(getLocalizedText(req, "USERNAME")).append("\t");
         body.append(evt.getTextFieldBean("userName").getValue()).append("\n");
         body.append(getLocalizedText(req, "GIVENNAME")).append("\t");
         body.append(evt.getTextFieldBean("firstName").getValue()).append("\n");
         body.append(getLocalizedText(req, "FAMILYNAME")).append("\t");
         body.append(evt.getTextFieldBean("lastName").getValue()).append("\n");
         body.append(getLocalizedText(req, "ORGANIZATION")).append("\t");
         body.append(evt.getTextFieldBean("organization").getValue()).append("\n");
         body.append(getLocalizedText(req, "EMAILADDRESS")).append("\t");
         body.append(evt.getTextFieldBean("emailAddress").getValue()).append("\n");
 
         mailToUser.setBody(body.toString());
 
         try {
             mailService.sendMail(mailToUser);
         } catch (PortletServiceException e) {
             createErrorMessage(evt, this.getLocalizedText(req, "LOGIN_FAILURE_MAIL"));
             throw new PortletException("Unable to send mail message!", e);
         }
 
         boolean adminRequired = Boolean.valueOf(portalConfigService.getProperty(PortalConfigService.ADMIN_ACCOUNT_APPROVAL));
         if (adminRequired) {
             createSuccessMessage(evt, this.getLocalizedText(req, "LOGIN_ACCT_ADMIN_MAIL"));
         } else {
             createSuccessMessage(evt, this.getLocalizedText(req, "LOGIN_ACCT_MAIL"));
         }
 
     }
 
     private void doEmailAction(ActionFormEvent event, String msg, boolean createAccount) {
         ActionRequest req = event.getActionRequest();
         ActionResponse res = event.getActionResponse();
         String id = req.getParameter("reqid");
         User user = null;
         Request request = requestService.getRequest(id, ACTIVATE_ACCOUNT_LABEL);
         if (request != null) {
             requestService.deleteRequest(request);
 
             String subject = "";
             String body = "";
             if (createAccount) {
                 user = saveUser(request);
                 createSuccessMessage(event, msg + " " + user.getUserName());
 
                 // send the user an email
                 subject = portalConfigService.getProperty("LOGIN_ACCOUNT_APPROVAL_ACCOUNT_CREATED");
                 if (subject == null) subject = getLocalizedText(req, "LOGIN_ACCOUNT_APPROVAL_ACCOUNT_CREATED");
                 body = portalConfigService.getProperty("LOGIN_ACCOUNT_APPROVAL_ACCOUNT_CREATED_BODY");
                 if (body == null) body = getLocalizedText(req, "LOGIN_ACCOUNT_APPROVAL_ACCOUNT_CREATED");
             } else {
                 createSuccessMessage(event, msg);
 
                 // send the user an email
                 subject = portalConfigService.getProperty("LOGIN_ACCOUNT_APPROVAL_ACCOUNT_DENY");
                 if (subject == null) subject = getLocalizedText(req, "LOGIN_ACCOUNT_APPROVAL_ACCOUNT_DENY");
                 body = portalConfigService.getProperty("LOGIN_ACCOUNT_APPROVAL_ACCOUNT_DENY_BODY");
                 if (body == null) body = getLocalizedText(req, "LOGIN_ACCOUNT_APPROVAL_ACCOUNT_DENY");
             }
 
             MailMessage mailToUser = new MailMessage();
             mailToUser.setEmailAddress(user.getEmailAddress());
             mailToUser.setSender(portalConfigService.getProperty(PortalConfigService.MAIL_FROM));
             mailToUser.setSubject(subject);
             StringBuffer msgbody = new StringBuffer();
 
             msgbody.append(body).append("\n\n");
             msgbody.append(notificationURL);
             mailToUser.setBody(body.toString());
 
             try {
                 mailService.sendMail(mailToUser);
             } catch (PortletServiceException e) {
                 log.error("Error: ", e);
                 createErrorMessage(event, this.getLocalizedText(req, "LOGIN_FAILURE_MAIL"));
             }
         }
         setNextState(req, DEFAULT_VIEW_PAGE);
 
     }
 
     public void activate(ActionFormEvent event) {
         PortletRequest req = event.getActionRequest();
         String msg = this.getLocalizedText(req, "USER_NEW_ACCOUNT") +
                 "<br>" + this.getLocalizedText(req, "USER_PLEASE_LOGIN");
         doEmailAction(event, msg, true);
         setNextState(req, "signup/activate.jsp");
     }
 
     public void approveAccount(ActionFormEvent event) {
         PortletRequest req = event.getActionRequest();
         String msg = this.getLocalizedText(req, "LOGIN_ACCOUNT_APPROVAL_ACCOUNT_CREATED");
         doEmailAction(event, msg, true);
        setNextState(req, "signup/approve.jsp");
     }
 
     public void denyAccount(ActionFormEvent event) {
         PortletRequest req = event.getActionRequest();
         String msg = this.getLocalizedText(req, "LOGIN_ACCOUNT_APPROVAL_ACCOUNT_DENY");
         doEmailAction(event, msg, false);
        setNextState(req, "signup/deny.jsp");
     }
 
 
 }
