 /*
  * @author <a href="mailto:michael.russell@aei.mpg.de">Michael Russell</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.portlets.core.admin.users;
 
 import org.gridlab.gridsphere.portlet.*;
 import org.gridlab.gridsphere.portlet.impl.SportletUser;
 import org.gridlab.gridsphere.portlet.service.PortletServiceException;
 import org.gridlab.gridsphere.provider.event.FormEvent;
 import org.gridlab.gridsphere.provider.portlet.ActionPortlet;
 import org.gridlab.gridsphere.provider.portletui.beans.*;
 import org.gridlab.gridsphere.services.core.portal.PortalConfigService;
 import org.gridlab.gridsphere.services.core.portal.PortalConfigSettings;
 import org.gridlab.gridsphere.services.core.security.acl.AccessControlManagerService;
 import org.gridlab.gridsphere.services.core.security.acl.GroupRequest;
 import org.gridlab.gridsphere.services.core.security.acl.GroupEntry;
 import org.gridlab.gridsphere.services.core.security.password.PasswordEditor;
 import org.gridlab.gridsphere.services.core.security.password.PasswordManagerService;
 import org.gridlab.gridsphere.services.core.user.UserManagerService;
 import org.gridlab.gridsphere.portlets.core.login.LoginPortlet;
 
 import javax.servlet.UnavailableException;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 public class UserManagerPortlet extends ActionPortlet {
 
     // JSP pages used by this portlet
     public static final String DO_VIEW_USER_LIST = "admin/users/doViewUserList.jsp";
     public static final String DO_VIEW_USER_VIEW = "admin/users/doViewUserView.jsp";
     public static final String DO_VIEW_USER_EDIT = "admin/users/doViewUserEdit.jsp";
 
     // Portlet services
     private UserManagerService userManagerService = null;
     private PasswordManagerService passwordManagerService = null;
     private AccessControlManagerService aclManagerService = null;
     private PortalConfigService portalConfigService = null;
 
 
     public void init(PortletConfig config) throws UnavailableException {
         super.init(config);
         log.debug("Entering initServices()");
         try {
             this.userManagerService = (UserManagerService) config.getContext().getService(UserManagerService.class);
             this.aclManagerService = (AccessControlManagerService) config.getContext().getService(AccessControlManagerService.class);
             this.passwordManagerService = (PasswordManagerService) config.getContext().getService(PasswordManagerService.class);
             this.portalConfigService = (PortalConfigService) getPortletConfig().getContext().getService(PortalConfigService.class);
         } catch (PortletServiceException e) {
             log.error("Unable to initialize services!", e);
         }
         log.debug("Exiting initServices()");
         DEFAULT_HELP_PAGE = "admin/users/help.jsp";
         DEFAULT_VIEW_PAGE = "doListUsers";
     }
 
     public void initConcrete(PortletSettings settings) throws UnavailableException {
         super.initConcrete(settings);
     }
 
     public void doListUsers(FormEvent evt)
             throws PortletException {
         PortletRequest req = evt.getPortletRequest();
         List userList = this.userManagerService.getUsers();
         req.setAttribute("userList", userList);
         setNextState(req, DO_VIEW_USER_LIST);
     }
 
     public void doViewUser(FormEvent evt)
             throws PortletException {
         PortletRequest req = evt.getPortletRequest();
 
         String userID = evt.getAction().getParameter("userID");
         User user = this.userManagerService.getUser(userID);
         if (user != null) {
             // should check for non-null user !
             req.setAttribute("user", user);
             HiddenFieldBean hf = evt.getHiddenFieldBean("userID");
             hf.setValue(user.getID());
             PortletGroup coreGroup = aclManagerService.getCoreGroup();
             PortletRole role = aclManagerService.getRoleInGroup(user, coreGroup);
             req.setAttribute("role", role.getName());
             CheckBoxBean accountCB = evt.getCheckBoxBean("accountCB");
             String disabled = (String)user.getAttribute(User.DISABLED);
             if ((disabled != null) && ("TRUE".equalsIgnoreCase(disabled))) {
                 accountCB.setSelected(true);
             }
             accountCB.setDisabled(true);
             setNextState(req, DO_VIEW_USER_VIEW);
         } else {
             setNextState(req, DEFAULT_VIEW_PAGE);
         }
     }
 
     public void doNewUser(FormEvent evt)
             throws PortletException {
 
         PortletRequest req = evt.getPortletRequest();
 
         // indicate to edit JSP this is a new user
         HiddenFieldBean hf = evt.getHiddenFieldBean("newuser");
         hf.setValue("true");
 
         PortalConfigSettings settings = portalConfigService.getPortalConfigSettings();
         if (settings.getAttribute(LoginPortlet.SAVE_PASSWORDS).equals(Boolean.TRUE.toString())) {
             req.setAttribute("savePass", "true");
         }
 
         setSelectedUserRole(evt, PortletRole.USER);
         setNextState(req, DO_VIEW_USER_EDIT);
         log.debug("in doViewNewUser");
     }
 
     public void doEditUser(FormEvent evt)
             throws PortletException {
 
         PortletRequest req = evt.getPortletRequest();
 
         // indicate to edit JSP this is an existing user
         HiddenFieldBean newuserHF = evt.getHiddenFieldBean("newuser");
         newuserHF.setValue("false");
 
         // load in User values
         HiddenFieldBean userHF = evt.getHiddenFieldBean("userID");
         String userID = userHF.getValue();
 
         // get user
         User user = this.userManagerService.getUser(userID);
         if (user == null) doNewUser(evt);
         PortletGroup coreGroup = aclManagerService.getCoreGroup();
         PortletRole role = aclManagerService.getRoleInGroup(user, coreGroup);
         setSelectedUserRole(evt, role);
 
         setUserValues(evt, user);
         PortalConfigSettings settings = portalConfigService.getPortalConfigSettings();
         if (settings.getAttribute(LoginPortlet.SAVE_PASSWORDS).equals(Boolean.TRUE.toString())) {
             req.setAttribute("savePass", "true");
         }
 
         setNextState(req, DO_VIEW_USER_EDIT);
     }
 
     public void doConfirmEditUser(FormEvent evt)
             throws PortletException {
 
         PortletRequest req = evt.getPortletRequest();
         //User user = loadUser(evt);
         try {
             User user;
             HiddenFieldBean hf = evt.getHiddenFieldBean("newuser");
             String newuser = hf.getValue();
             log.debug("in doConfirmEditUser: " + newuser);
             if (newuser.equals("true")) {
                 validateUser(evt, true);
                 user = saveUser(evt, null);
                 HiddenFieldBean userHF = evt.getHiddenFieldBean("userID");
                 userHF.setValue(user.getID());
                 createSuccessMessage(evt, this.getLocalizedText(req, "USER_NEW_SUCCESS"));
             } else {
                 validateUser(evt, false);
                 // load in User values
                 HiddenFieldBean userHF = evt.getHiddenFieldBean("userID");
                 String userID = userHF.getValue();
                 User thisuser = this.userManagerService.getUser(userID);
                 user = saveUser(evt, thisuser);
                 createSuccessMessage(evt, this.getLocalizedText(req, "USER_EDIT_SUCCESS"));
             }
             req.setAttribute("user", user);
 
             PortletGroup coreGroup = aclManagerService.getCoreGroup();
             PortletRole role = aclManagerService.getRoleInGroup(user, coreGroup);
             req.setAttribute("role", role.getName());
 
             setNextState(req, "doListUsers");
         } catch (PortletException e) {
             createErrorMessage(evt, e.getMessage());
             PortalConfigSettings settings = portalConfigService.getPortalConfigSettings();
             if (settings.getAttribute(LoginPortlet.SAVE_PASSWORDS).equals(Boolean.TRUE.toString())) {
                 req.setAttribute("savePass", "true");
             }
             setNextState(req, DO_VIEW_USER_EDIT);
         }
     }
 
     public void doDeleteUser(FormEvent evt)
             throws PortletException {
  
         PortletRequest req = evt.getPortletRequest();
         HiddenFieldBean hf = evt.getHiddenFieldBean("userID");
         String userId = hf.getValue();
         User user = this.userManagerService.getUser(userId);
         if (user != null) {
             req.setAttribute("user", user);
             PortletGroup coreGroup = aclManagerService.getCoreGroup();
             PortletRole role = aclManagerService.getRoleInGroup(user, coreGroup);
             req.setAttribute("role", role.getName());
             this.userManagerService.deleteUser(user);
             createSuccessMessage(evt, this.getLocalizedText(req, "USER_DELETE_SUCCESS"));
         }
         setNextState(req, "doListUsers");
     }
 
 
     private void setUserValues(FormEvent event, User user) {
         event.getTextFieldBean("userName").setValue(user.getUserName());
         event.getTextFieldBean("familyName").setValue(user.getFamilyName());
         event.getTextFieldBean("givenName").setValue(user.getGivenName());
         event.getTextFieldBean("fullName").setValue(user.getFullName());
         event.getTextFieldBean("emailAddress").setValue(user.getEmailAddress());
         event.getTextFieldBean("organization").setValue(user.getOrganization());
         event.getPasswordBean("password").setValue("");
     }
 
     private void validateUser(FormEvent event, boolean newuser)
             throws PortletException {
         log.debug("Entering validateUser()");
         PortletRequest req = event.getPortletRequest();
         StringBuffer message = new StringBuffer();
         boolean isInvalid = false;
         // Validate user name
         String userName = event.getTextFieldBean("userName").getValue();
         if (userName.equals("")) {
             createErrorMessage(event, this.getLocalizedText(req, "USER_NAME_BLANK") + "<br />");
             isInvalid = true;
         } else if (newuser) {
             if (this.userManagerService.existsUserName(userName)) {
                 createErrorMessage(event, this.getLocalizedText(req, "USER_EXISTS") + "<br />");
                 isInvalid = true;
             }
         }
 
         // Validate family name
         /*
         String familyName = event.getTextFieldBean("familyName").getValue();
         if (familyName.equals("")) {
             message.append(this.getLocalizedText(req, "USER_FAMILYNAME_BLANK") + "<br />");
             isInvalid = true;
         }
         */
         // Validate given name
         String givenName = event.getTextFieldBean("fullName").getValue();
 
         if (givenName.equals("")) {
             createErrorMessage(event, this.getLocalizedText(req, "USER_FULLNAME_BLANK") + "<br />");
             isInvalid = true;
         }
 
         // Validate e-mail
         String eMail = event.getTextFieldBean("emailAddress").getValue();
         if (eMail.equals("")) {
             createErrorMessage(event, this.getLocalizedText(req, "USER_NEED_EMAIL") + "<br />");
             isInvalid = true;
         } else if ((eMail.indexOf("@") < 0)) {
             createErrorMessage(event, this.getLocalizedText(req, "USER_NEED_EMAIL") + "<br />");
             isInvalid = true;
         } else if ((eMail.indexOf(".") < 0)) {
             createErrorMessage(event, this.getLocalizedText(req, "USER_NEED_EMAIL") + "<br />");
             isInvalid = true;
         }
 
         PortalConfigSettings settings = portalConfigService.getPortalConfigSettings();
         if (settings.getAttribute(LoginPortlet.SAVE_PASSWORDS).equals(Boolean.TRUE.toString())) {
             if (isInvalidPassword(event, newuser)){
                 isInvalid = true;
             }
         }
         // Throw exception if error was found
         if (isInvalid) {
             throw new PortletException(message.toString());
         }
         log.debug("Exiting validateUser()");
     }
 
     private boolean isInvalidPassword(FormEvent event, boolean newuser) {
         // Validate password
         PortletRequest req = event.getPortletRequest();
         String passwordValue = event.getPasswordBean("password").getValue();
         String confirmPasswordValue = event.getPasswordBean("confirmPassword").getValue();
 
         // If user already exists and password unchanged, no problem
         if (passwordValue.length() == 0 &&
                 confirmPasswordValue.length() == 0) {
             if (newuser) {
                 createErrorMessage(event, this.getLocalizedText(req, "USER_PASSWORD_BLANK") + "<br />");
                 return true;
             }
             return false;
         }
         // Otherwise, password must match confirmation
         if (!passwordValue.equals(confirmPasswordValue)) {
             createErrorMessage(event, this.getLocalizedText(req, "USER_PASSWORD_MISMATCH") + "<br />");
             return true;
             // If they do match, then validate password with our service
         } else {
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
 
     private User saveUser(FormEvent event, User user) {
         log.debug("Entering saveUser()");
         // Account request
         SportletUser newuser;
         boolean newuserflag = false;
         // Create edit account request
         if (user == null) {
             newuser = this.userManagerService.createUser();
             newuserflag = true;
         } else {
             //System.err.println("Creating account request for existing user");
             newuser = this.userManagerService.editUser(user);
         }
 
         PortalConfigSettings settings = portalConfigService.getPortalConfigSettings();
         if (settings.getAttribute(LoginPortlet.SAVE_PASSWORDS).equals(Boolean.TRUE.toString())) {
             PasswordEditor editor = passwordManagerService.editPassword(newuser);
             String password = event.getPasswordBean("password").getValue();
             boolean isgood = this.isInvalidPassword(event, newuserflag);
             if (isgood) {
                 setNextState(event.getPortletRequest(), DO_VIEW_USER_EDIT);
                 return newuser;
             } else {
                 if (!password.equals("")) {
                     editor.setValue(password);
                     passwordManagerService.savePassword(editor);
                 }
             }
         }
 
         // Edit account attributes
         editAccountRequest(event, newuser);
 
         // Submit changes
         this.userManagerService.saveUser(newuser);
 
 
         // Save user role
         saveUserRole(event, newuser);
         log.debug("Exiting saveUser()");
         return newuser;
     }
 
     private void editAccountRequest(FormEvent event, SportletUser accountRequest) {
         log.debug("Entering editAccountRequest()");
         accountRequest.setUserName(event.getTextFieldBean("userName").getValue());
         //accountRequest.setFamilyName(event.getTextFieldBean("familyName").getValue());
         //accountRequest.setGivenName(event.getTextFieldBean("givenName").getValue());
         accountRequest.setFullName(event.getTextFieldBean("fullName").getValue());
         accountRequest.setEmailAddress(event.getTextFieldBean("emailAddress").getValue());
         accountRequest.setOrganization(event.getTextFieldBean("organization").getValue());
         if (event.getCheckBoxBean("accountCB").isSelected()) {
             accountRequest.setAttribute(User.DISABLED, "true");
         } else {
             accountRequest.setAttribute(User.DISABLED, "false");
         }
     }
 
     private void saveUserRole(FormEvent event, User user) {
         log.debug("Entering saveUserRole()");
         PortletRequest req = event.getPortletRequest();
         // Get selected role
         PortletRole selectedRole = getSelectedUserRole(event);
         req.setAttribute("role", selectedRole);
         // If super role was chosen
         if (selectedRole.equals(PortletRole.SUPER)) {
             log.debug("Granting super role");
             // Grant super role
             this.aclManagerService.grantSuperRole(user);
         } else {
             // Revoke super role (in case they had it)
             //this.aclManagerService.revokeSuperRole(user);
             // Create appropriate access request
             Set groups = portalConfigService.getPortalConfigSettings().getDefaultGroups();
             Iterator it = groups.iterator();
             GroupRequest groupRequest;
             while (it.hasNext()) {
                 PortletGroup group = (PortletGroup) it.next();
                 GroupEntry ge = aclManagerService.getGroupEntry(user, group);
                 if (ge != null) {
                     groupRequest = this.aclManagerService.editGroupEntry(ge);
                 }  else {
                     groupRequest = this.aclManagerService.createGroupEntry();
                 }
                 groupRequest.setUser(user);
                 groupRequest.setRole(selectedRole);
                 groupRequest.setGroup(group);
                 log.debug("Granting " + selectedRole + " role in gridsphere");
                 // Submit changes
                 this.aclManagerService.saveGroupEntry(groupRequest);
             }
         }
         log.debug("Exiting saveUserRole()");
     }
 
     private void setSelectedUserRole(FormEvent event, PortletRole role) {
         PortletRequest req = event.getPortletRequest();
         ListBoxBean roleListBean = event.getListBoxBean("userRole");
         roleListBean.clear();
         /*
         ListBoxItemBean userRole = new ListBoxItemBean();
         ListBoxItemBean adminRole = new ListBoxItemBean();
         ListBoxItemBean superRole = new ListBoxItemBean();
         */
         ListBoxItemBean roleItemBean;
         /*
         userRole.setValue(PortletRole.USER.getText(req.getLocale()));
         userRole.setName(PortletRole.USER.getName());
         adminRole.setValue(PortletRole.ADMIN.getText(req.getLocale()));
         adminRole.setName(PortletRole.ADMIN.getName());
         superRole.setValue(PortletRole.SUPER.getText(req.getLocale()));
         superRole.setName(PortletRole.SUPER.getName());
         */
         List roles = aclManagerService.getRoles();
         Iterator it = roles.iterator();
         while (it.hasNext()) {
             PortletRole r = (PortletRole)it.next();
             roleItemBean = new ListBoxItemBean();
             roleItemBean.setValue(r.getName());
             roleItemBean.setName(r.getName());
             if (role.getName().equalsIgnoreCase(r.getName())) {
                 roleItemBean.setSelected(true);
             }
             roleListBean.addBean(roleItemBean);
         }
         /*
         if (role.equals(PortletRole.USER)) {
             userRole.setSelected(true);
         } else if (role.equals(PortletRole.ADMIN)) {
             adminRole.setSelected(true);
         } else if (role.equals(PortletRole.SUPER)) {
             superRole.setSelected(true);
         }
         roleListBean.addBean(userRole);
         roleListBean.addBean(adminRole);
         roleListBean.addBean(superRole);
         */
     }
 
     private PortletRole getSelectedUserRole(FormEvent event) {
         // Iterate through list, return selected value
         ListBoxBean roleListBean = event.getListBoxBean("userRole");
         List userRoleList = roleListBean.getSelectedNames();
         if (userRoleList.size() == 0) {
             log.debug("No role was selected, setting to user");
             // Impossible, but if not selected return user role
             return aclManagerService.getRoleByName(PortletRole.USER.getName());
         } else {
             // Otherwise, return the first selected value
             String userRoleItem = (String) userRoleList.get(0);
             log.debug("Selected role was " + userRoleItem);
             return aclManagerService.getRoleByName(userRoleItem);
             //return PortletRole.toPortletRole(userRoleItem);
         }
     }
 
     private void createErrorMessage(FormEvent event, String msg) {
         MessageBoxBean msgBox = event.getMessageBoxBean("msg");
         msgBox.setMessageType(MessageStyle.MSG_ERROR);
         String msgOld = msgBox.getValue();
         msgBox.setValue((msgOld!=null?msgOld:"")+msg);
     }
 
     private void createSuccessMessage(FormEvent event, String msg) {
         MessageBoxBean msgBox = event.getMessageBoxBean("msg");
         msgBox.setMessageType(MessageStyle.MSG_SUCCESS);
         msgBox.setValue(msg);
     }
 
 
 }
