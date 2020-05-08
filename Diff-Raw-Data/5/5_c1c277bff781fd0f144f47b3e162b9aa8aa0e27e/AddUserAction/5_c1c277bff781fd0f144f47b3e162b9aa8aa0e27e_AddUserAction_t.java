 package com.ai.action;
 
 import com.ai.model.*;
 import com.ai.model.Role;
 import com.ai.service.BranchService;
 import com.ai.service.PermissionService;
 import com.ai.service.RoleService;
 import com.ai.service.UserService;
 import com.ai.service.impl.BranchServiceImpl;
 import com.ai.service.impl.PermissionServiceImpl;
 import com.ai.service.impl.RoleServiceImpl;
 import com.ai.service.impl.UserServiceImpl;
 import com.ai.util.*;
 import com.ai.util.Permission;
 import com.ai.validator.Validator;
 import com.ai.validator.impl.ValidatorImpl;
 import com.opensymphony.xwork2.ActionContext;
 import com.opensymphony.xwork2.ActionSupport;
 import com.opensymphony.xwork2.ModelDriven;
 import org.apache.struts2.interceptor.SessionAware;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 import java.util.*;
 
 public class AddUserAction extends ActionSupport implements ModelDriven<User>, SessionAware {
     private User user = new User();
     private Map session = ActionContext.getContext().getSession();
     private ActiveUser activeUser = new ActiveUser();
     private Logger logger = LoggerFactory.getLogger(AddUserAction.class);
     @Autowired
     private UserService userService;
     @Autowired
     private RoleService roleService;
     @Autowired
     private BranchService branchService;
     @Autowired
     private PermissionService permissionService;
     @Autowired
     private Validator validator;
 
     public Map<Long, String> branchList = new HashMap<Long, String>();
 
     private void loadBranches() {
         try {
             for (Branch branch : branchService.findAllByTenant(activeUser.getTenant())) {
                 branchList.put(branch.getBranchId(), branch.getBranchName());
             }
         } catch (Exception e) {
             logger.info("Unable to load the branches");
         }
     }
     
     private String handelValidator(Return r){
         if( r == Return.VALIDATION_FAIL){
             addActionError("Invalid request");
             return Return.INVALID_REQUEST.getReturnCode();
         } else if( r == Return.AUTHENTICATION_FAIL){
             addActionError("Please login to the system");
             return Return.INVALID_SESSION.getReturnCode();
         } else {
             addActionError("User not authorised");
             return Return.INVALID_SESSION.getReturnCode();
         }
     }
 
     //action
     public String execute() {
         try {
             activeUser = (ActiveUser) session.get("activeUser");
             loadBranches();
 
             Return r = validator.execute(activeUser, permissionService.findPermissionById(Permission.ADD_USER.getPermission()), hasActionErrors(), hasFieldErrors());
             if( r == Return.VALIDATOR_OK){
                 return Return.SUCCESS.getReturnCode();    
             } else {
                 return handelValidator(r);
             }
 
         } catch (Exception e) {
             logger.info("Exception occurred in load add user");
             addActionError("Internal error.Try again later");
             return Return.INTERNAL_ERROR.getReturnCode();
         }
     }
 
     //action
     public String addNewUser() {
         try {
             activeUser = (ActiveUser) session.get("activeUser");
             loadBranches();
             for (Map.Entry<String, String> stringStringEntry : fieldValidate().entrySet()) {
                 addFieldError(stringStringEntry.getKey(), stringStringEntry.getValue());
             }
 
             Return r = validator.execute(activeUser, permissionService.findPermissionById(Permission.ADD_USER.getPermission()), hasActionErrors(), hasFieldErrors());
             if( r == Return.VALIDATOR_OK){
                 user.getPersonalDetails().setUser(user);
                 MD5Generator md5Generator = new MD5Generator();
                 user.setPassword(md5Generator.getMD5String(this.user.getPassword()));
                 user.getPersonalDetails().setUser(user);
 
                 user = setUserRoles(user);
                 userService.saveUser(user);
                 addActionMessage("You have Successfully created new User.");
                 return Return.PARENT.getReturnCode();
             } else {
                 return handelValidator(r);
             }
 
         } catch (Exception e) {
             logger.info("Exception occurred in adding user");
             e.printStackTrace();
             addActionError("Internal error.Try again later");
             return Return.INTERNAL_ERROR.getReturnCode();
         }
     }
 
     private Map<String, String> fieldValidate() {
         Map<String, String> errors = new HashMap<String, String>();
         if (user.getUserName() == null || user.getUserName().equals("")) {
             errors.put("userName", "User name is required");
         }
         if(userService.findUserByUserName(user.getUserName()) != null){
             errors.put("userName", "User name already exist");
         }
         if (user.getPassword() == null || user.getPassword().equals("")) {
             errors.put("password", "Password is required");
         }
         if (user.getPersonalDetails().getFirstName() == null || user.getPersonalDetails().getFirstName().equals("")) {
             errors.put("personalDetails.firstName", "First name is required");
         }
         if (user.getPersonalDetails().getLastName() == null || user.getPersonalDetails().getLastName().equals("")) {
             errors.put("personalDetails.lastName", "Last name is required");
         }
         if (user.getPersonalDetails().getAge() <= 0) {
             errors.put("personalDetails.age", "Please enter valid age");
         }
         return errors;
     }
 
     private User setUserRoles(User u) {
         if (u.getUserType().equalsIgnoreCase(UserType.ADMIN.getType())) {
             UserRole ur = new UserRole(null, roleService.findRoleById(com.ai.util.Role.ADMIN.getRole()));
             RolePermission rp1 = new RolePermission(null, permissionService.findPermissionById(Permission.LOGIN.getPermission()));
             RolePermission rp2 = new RolePermission(null, permissionService.findPermissionById(Permission.ADD_BRANCH.getPermission()));
             RolePermission rp3 = new RolePermission(null, permissionService.findPermissionById(Permission.ADD_PRODUCT.getPermission()));
             RolePermission rp4 = new RolePermission(null, permissionService.findPermissionById(Permission.ADD_USER.getPermission()));
             RolePermission rp5 = new RolePermission(null, permissionService.findPermissionById(Permission.VIEW_REPORT.getPermission()));
             RolePermission rp6 = new RolePermission(null, permissionService.findPermissionById(Permission.STATISTICS.getPermission()));
             rp1.setRoleId(ur);
             rp2.setRoleId(ur);
             rp3.setRoleId(ur);
             rp4.setRoleId(ur);
             rp5.setRoleId(ur);
             rp6.setRoleId(ur);
             ur.getRolePermissions().add(rp1);
             ur.getRolePermissions().add(rp2);
             ur.getRolePermissions().add(rp3);
             ur.getRolePermissions().add(rp4);
             ur.getRolePermissions().add(rp5);
             ur.getRolePermissions().add(rp6);
 
             ur.setUser(u);
             u.getUserRoles().add(ur);
         } else if (u.getUserType().equalsIgnoreCase(UserType.SUPER_MERCHANT.getType())) {
             UserRole ur = new UserRole(null, roleService.findRoleById(com.ai.util.Role.SUPER_MERCHANT.getRole()));
             RolePermission rp1 = new RolePermission(null, permissionService.findPermissionById(Permission.LOGIN.getPermission()));
             RolePermission rp2 = new RolePermission(null, permissionService.findPermissionById(Permission.ADD_PRODUCT.getPermission()));
             RolePermission rp3 = new RolePermission(null, permissionService.findPermissionById(Permission.SELL_PRODUCT.getPermission()));
             RolePermission rp4 = new RolePermission(null, permissionService.findPermissionById(Permission.VIEW_REPORT.getPermission()));
             rp1.setRoleId(ur);
             rp2.setRoleId(ur);
             rp3.setRoleId(ur);
             rp4.setRoleId(ur);
             ur.getRolePermissions().add(rp1);
             ur.getRolePermissions().add(rp2);
             ur.getRolePermissions().add(rp3);
             ur.getRolePermissions().add(rp4);
 
             ur.setUser(u);
             u.getUserRoles().add(ur);
         } else {
             UserRole ur = new UserRole(null, roleService.findRoleById(com.ai.util.Role.MERCHANT.getRole()));
             RolePermission rp1 = new RolePermission(null, permissionService.findPermissionById(Permission.LOGIN.getPermission()));
             RolePermission rp2 = new RolePermission(null, permissionService.findPermissionById(Permission.SELL_PRODUCT.getPermission()));
            RolePermission rp4 = new RolePermission(null, permissionService.findPermissionById(Permission.VIEW_REPORT.getPermission()));
             rp1.setRoleId(ur);
             rp2.setRoleId(ur);
            rp4.setRoleId(ur);
             ur.getRolePermissions().add(rp1);
             ur.getRolePermissions().add(rp2);
            ur.getRolePermissions().add(rp4);
 
             ur.setUser(u);
             u.getUserRoles().add(ur);
         }
         return u;
     }
 
 
     public User getUser() {
         return user;
     }
 
     public void setUser(User user) {
         this.user = user;
     }
 
     @Override
     public User getModel() {
         return user;
     }
 
     @Override
     public void setSession(Map session1) {
         this.session = session1;
     }
 
     public Map<Long, String> getBranchList() {
         return branchList;
     }
 
     public void setBranchList(Map<Long, String> branchList) {
         this.branchList = branchList;
     }
 }
