 package org.synyx.minos.umt.web;
 
 import org.springframework.beans.BeanUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import org.springframework.stereotype.Controller;
 
 import org.springframework.ui.Model;
 
 import org.springframework.validation.DataBinder;
 import org.springframework.validation.Errors;
 import org.springframework.validation.Validator;
 
 import org.springframework.web.bind.annotation.InitBinder;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
 import static org.springframework.web.bind.annotation.RequestMethod.GET;
 import static org.springframework.web.bind.annotation.RequestMethod.POST;
 import static org.springframework.web.bind.annotation.RequestMethod.PUT;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.SessionAttributes;
 import org.springframework.web.bind.support.SessionStatus;
 
 import org.synyx.hades.domain.Page;
 import org.synyx.hades.domain.Pageable;
 
 import org.synyx.minos.core.Core;
 import org.synyx.minos.core.domain.Role;
 import org.synyx.minos.core.domain.User;
 import org.synyx.minos.core.security.AuthenticationService;
 import org.synyx.minos.core.web.CurrentUser;
 import org.synyx.minos.core.web.Message;
 import org.synyx.minos.core.web.PageWrapper;
 import org.synyx.minos.core.web.UrlUtils;
 import org.synyx.minos.core.web.ValidationSupport;
 import static org.synyx.minos.umt.UmtPermissions.UMT_ADMIN;
 import org.synyx.minos.umt.service.UserManagement;
 import org.synyx.minos.umt.service.UserNotFoundException;
 import static org.synyx.minos.umt.web.UmtUrls.MODULE;
 import static org.synyx.minos.umt.web.UmtUrls.ROLE;
 import static org.synyx.minos.umt.web.UmtUrls.ROLES;
 import static org.synyx.minos.umt.web.UmtUrls.ROLE_DELETE;
 import static org.synyx.minos.umt.web.UmtUrls.ROLE_DELETE_QUESTION;
 import static org.synyx.minos.umt.web.UmtUrls.ROLE_FORM;
 import static org.synyx.minos.umt.web.UmtUrls.USER;
 import static org.synyx.minos.umt.web.UmtUrls.USERS;
 import static org.synyx.minos.umt.web.UmtUrls.USER_DELETE;
 import static org.synyx.minos.umt.web.UmtUrls.USER_DELETE_QUESTION;
 import static org.synyx.minos.umt.web.UmtUrls.USER_FORM;
 import org.synyx.minos.util.Assert;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import javax.annotation.security.RolesAllowed;
 
 
 /**
  * Web controller for the user management providing access to most of the user management functionality.
  *
  * @author  Oliver Gierke - gierke@synyx.de
  */
 @Controller
 @SessionAttributes({ UmtController.ROLE_KEY, UmtController.USER_KEY })
 @RolesAllowed(UMT_ADMIN)
 public class UmtController extends ValidationSupport<UserForm> {
 
     public static final String USER_KEY = "userForm";
     public static final String ROLE_KEY = "role";
     public static final String USERS_KEY = "users";
 
     private final UserManagement userManagement;
     private final AuthenticationService authenticationService;
 
     private Validator roleValidator;
 
     private Comparator<String> permissionComparator;
 
     protected UmtController() {
 
         this(null, null);
     }
 
 
     @Autowired
     public UmtController(UserManagement userManagement, AuthenticationService authenticationService) {
 
         super();
         this.userManagement = userManagement;
         this.authenticationService = authenticationService;
     }
 
     @InitBinder
     public void initBinder(DataBinder binder) {
 
         binder.registerCustomEditor(Role.class, new RolePropertyEditor(userManagement));
     }
 
 
     @RequestMapping(MODULE)
     public String index() {
 
         return UrlUtils.redirect(USERS);
     }
 
 
     @RequestMapping(value = USERS, method = GET)
     public String getUsers(Pageable pageable, Model model,
         @RequestParam(value = "role", required = false) Role role, @CurrentUser User user) {
 
         model.addAttribute("currentUser", user);
 
         Page<User> page = null;
 
         if (role == null) {
             page = userManagement.getUsers(pageable);
         } else {
             page = userManagement.getUsersByRole(role, pageable);
         }
 
         model.addAttribute(USERS_KEY, PageWrapper.wrap(page));
         model.addAttribute("roles", userManagement.getRoles());
 
         return USERS;
     }
 
 
     @RequestMapping(value = USER, method = DELETE)
     public String deleteUser(@PathVariable("id") User user, Model model, @CurrentUser User currentUser) {
 
         String targetView = UrlUtils.redirect(USERS);
 
         if (null == user) {
             model.addAttribute(Core.MESSAGE, Message.error("umt.user.delete.usernamerequired"));
 
             return targetView;
         }
 
         try {
             boolean currentUserGiven = null != currentUser;
 
             if (currentUserGiven && currentUser.equals(user)) {
                 model.addAttribute(Core.MESSAGE, Message.error("umt.user.delete.cannotdeleteherself"));
 
                 return targetView;
             }
 
             userManagement.delete(user);
 
             model.addAttribute(Core.MESSAGE, Message.success("umt.user.delete.success", user.getUsername()));
         } catch (UserNotFoundException e) {
             model.addAttribute(Core.MESSAGE, Message.error("user.delete.invalidusername", user.getId()));
         }
 
         return targetView;
     }
 
 
     @RequestMapping(value = USER_DELETE, method = GET)
     public String deleteUserQuestion(@PathVariable("id") User user, Model model, @CurrentUser User currentUser) {
 
         if (null == user) {
             model.addAttribute(Core.MESSAGE, Message.error("umt.user.delete.usernamerequired"));
 
             return UrlUtils.redirect(USERS);
         }
 
         boolean currentUserGiven = null != currentUser;
 
         if (currentUserGiven && currentUser.equals(user)) {
             model.addAttribute(Core.MESSAGE, Message.error("umt.user.delete.cannotdeleteherself"));
 
             return UrlUtils.redirect(USERS);
         }
 
         model.addAttribute(USER_KEY, new UserForm(user));
 
         return USER_DELETE_QUESTION;
     }
 
 
     @RequestMapping(value = USER, method = GET)
     public String showUser(@PathVariable("id") User user, Model model) {
 
         if (null == user) {
             model.addAttribute(Core.MESSAGE, Message.error("umt.user.invalid"));
 
             return UrlUtils.redirect(USERS);
         }
 
         return populateFormModel(new UserForm(user), model);
     }
 
 
     @RequestMapping(value = USER_FORM, method = GET)
     public String showEmptyFormForNewUser(Model model) {
 
         return populateFormModel(BeanUtils.instantiateClass(UserForm.class), model);
     }
 
 
     private String populateFormModel(UserForm userForm, Model model) {
 
         model.addAttribute(USER_KEY, userForm);
 
         List<Role> roles = new ArrayList<Role>(userManagement.getRoles());
         Collections.sort(roles);
         model.addAttribute("roles", roles);
 
         return "/umt/user";
     }
 
 
     @RequestMapping(value = USERS, method = POST)
     public String saveNewUser(@ModelAttribute(USER_KEY) UserForm userForm, Errors errors, SessionStatus conversation,
         Model model) {
 
         return saveUser(userForm, USERS, errors, conversation, model);
     }
 
 
     @RequestMapping(value = USER, method = PUT)
     public String saveExistingUser(@ModelAttribute(USER_KEY) UserForm userForm, Errors errors,
         SessionStatus conversation, Model model) {
 
         return saveUser(userForm, USERS, errors, conversation, model);
     }
 
 
     String saveUser(UserForm userForm, String redirectUrl, Errors errors, SessionStatus conversation, Model model) {
 
         if (!isValid(userForm, errors)) {
             model.addAttribute("roles", userManagement.getRoles());
 
             return "/umt/user";
         }
 
         userManagement.save(userForm.getDomainObject());
         conversation.setComplete();
 
         model.addAttribute(Core.MESSAGE, Message.success("umt.user.save.success", userForm.getUsername()));
 
         return UrlUtils.redirect(redirectUrl);
     }
 
 
     @RequestMapping(value = ROLES, method = GET)
     public String getRoles(Model model) {
 
         model.addAttribute("roles", userManagement.getRoles());
 
         return ROLES;
     }
 
 
     @RequestMapping(value = ROLE, method = GET)
     public String showRole(@PathVariable("id") Role role, Model model) {
 
         return prepareRoleForm(role, model);
     }
 
 
     @RequestMapping(value = ROLE_FORM, method = GET)
     public String showEmptyFormForNewRole(Model model) {
 
         return prepareRoleForm(new Role(), model);
     }
 
 
     private String prepareRoleForm(Role role, Model model) {
 
         Assert.notNull(role);
 
         model.addAttribute(ROLE_KEY, role);
 
         List<PermissionHolder> permissions = determinePermissions(role);
         model.addAttribute("permissions", permissions);
 
        model.addAttribute("users", userManagement.getUsersByRole(role));
 
         return "/umt/role";
     }
 
 
     List<PermissionHolder> determinePermissions(Role role) {
 
         Collection<String> perm = authenticationService.getPermissions();
         List<String> permissionNames = new ArrayList<String>();
         permissionNames.addAll(perm);
         Collections.sort(permissionNames, permissionComparator);
 
         List<PermissionHolder> permissions = new ArrayList<PermissionHolder>();
 
         for (String permission : permissionNames) {
             boolean present = false;
 
             if (role.getPermissions().contains(permission)) {
                 present = true;
             }
 
             permissions.add(new PermissionHolder(permission, present));
         }
 
         return permissions;
     }
 
 
     @RequestMapping(value = ROLES, method = POST)
     public String saveNewRole(@ModelAttribute(ROLE_KEY) Role role, Errors errors, Model model) {
 
         return saveRole(role, errors, model);
     }
 
 
     String saveRole(Role role, Errors errors, Model model) {
 
         if (!isRoleValid(role, errors)) {
             return prepareRoleForm(role, model);
         }
 
         userManagement.save(role);
 
         model.addAttribute(Core.MESSAGE, Message.success("umt.role.save.success", role.getName()));
 
         return UrlUtils.redirect(ROLES);
     }
 
 
     private boolean isRoleValid(Role role, Errors errors) {
 
         if (roleValidator != null) {
             roleValidator.validate(role, errors);
 
             return !errors.hasErrors();
         } else {
             return true;
         }
     }
 
 
     @RequestMapping(value = ROLE, method = PUT)
     public String saveExistingRole(@ModelAttribute(ROLE_KEY) Role role, Errors errors, Model model) {
 
         return saveRole(role, errors, model);
     }
 
 
     @RequestMapping(value = ROLE, method = DELETE)
     public String deleteRole(@PathVariable("id") Role role, Model model) {
 
         userManagement.deleteRole(role);
 
         model.addAttribute(Core.MESSAGE, Message.success("umt.role.delete.success", role.getName()));
 
         return UrlUtils.redirect(ROLES);
     }
 
 
     @RequestMapping(value = ROLE_DELETE, method = GET)
     public String deleteRoleQuestion(@PathVariable("id") Role role, Model model, @CurrentUser User currentUser) {
 
         if (null == role) {
             return UrlUtils.redirect(ROLES);
         }
 
         List<User> users = userManagement.getUsersByRole(role);
 
         model.addAttribute(ROLE_KEY, role);
         model.addAttribute("deletable", users.isEmpty());
         model.addAttribute("users", users);
 
         return ROLE_DELETE_QUESTION;
     }
 
 
     public void setPermissionComparator(Comparator<String> permissionComparator) {
 
         this.permissionComparator = permissionComparator;
     }
 
 
     public void setRoleValidator(Validator roleValidator) {
 
         this.roleValidator = roleValidator;
     }
 }
