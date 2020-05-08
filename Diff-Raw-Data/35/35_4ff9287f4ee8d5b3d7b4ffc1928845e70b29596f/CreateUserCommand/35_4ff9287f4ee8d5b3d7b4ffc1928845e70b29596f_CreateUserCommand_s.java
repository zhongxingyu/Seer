 package edu.northwestern.bioinformatics.studycalendar.web;
 
 import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
 import edu.northwestern.bioinformatics.studycalendar.domain.Role;
 import edu.northwestern.bioinformatics.studycalendar.domain.Site;
 import edu.northwestern.bioinformatics.studycalendar.domain.User;
 import edu.northwestern.bioinformatics.studycalendar.domain.UserRole;
 import edu.northwestern.bioinformatics.studycalendar.service.UserService;
 import edu.northwestern.bioinformatics.studycalendar.service.UserRoleService;
 import edu.nwu.bioinformatics.commons.spring.Validatable;
import gov.nih.nci.security.util.StringEncrypter;
 import org.apache.commons.lang.StringUtils;
 import org.springframework.validation.Errors;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 public class CreateUserCommand implements Validatable {
     private UserService userService;
     private String password;
     private String rePassword;
     private boolean passwordModified;
     private User user;
     private SiteDao siteDao;
     private Map<Site, Map<Role,RoleCell>> rolesGrid;
     private UserRoleService userRoleService;
 
     public CreateUserCommand(User user, SiteDao siteDao, UserService userService, UserRoleService userRoleService) {
         this.user = user;
         this.siteDao = siteDao;
         this.userService = userService;
         this.userRoleService = userRoleService;
         this.passwordModified = false;
         
         if(user == null) user = new User();
         buildRolesGrid(user.getUserRoles());
     }
 
     private void buildRolesGrid(Set<UserRole> userRoles) {
         boolean selected;
         rolesGrid = new HashMap<Site, Map<Role,RoleCell>>();
         
         for(Site site : siteDao.getAll()) {
             for(Role role : Role.values()) {
                 selected = false;
                 for(UserRole userRole : userRoles) {
                     if (userRole.getRole().equals(role) && (!role.isSiteSpecific() || userRole.getSites().contains(site)) ) {
                         selected = true;
                         break;
                     }
                 }
 
                 if (!rolesGrid.containsKey(site)) {
                     rolesGrid.put(site, new HashMap<Role, RoleCell>());
                 }
 
                 rolesGrid.get(site).put(role, createRoleCell(selected,role.isSiteSpecific()));
             }
         }
     }
 
     public void validate(Errors errors){
         if (user != null) {
             if (user.getName() == null || StringUtils.isEmpty(user.getName())) {
                 errors.rejectValue("user.name", "error.user.name.not.specified");
             } else {
                 if (user.getId() == null && userService.getUserByName(user.getName()) != null){
                     errors.rejectValue("user.name", "error.user.name.already.exists");
                 }
             }
             if (passwordModified) {
                 if (password == null || StringUtils.isBlank(password)){
                     errors.rejectValue("password", "error.user.password.not.specified");
                 } else {
                     if (!password.equals(rePassword)) {
                         errors.rejectValue("rePassword", "error.user.repassword.does.not.match.password");
                     }
                 }
             }
         }
 
     }
 


     public User apply() throws Exception {
         if (passwordModified || user.getId() == null) {
             userService.saveUser(user, password);
         } else {
             userService.saveUser(user);
         }
         assignUserRolesFromRolesGrid(rolesGrid);
         return user;
     }
 
 
     public void assignUserRolesFromRolesGrid(Map<Site, Map<Role,RoleCell>> rolesGrid) throws Exception {
         for(Site site : rolesGrid.keySet()) {
             for(Role role : rolesGrid.get(site).keySet()) {
                 if (role.isSiteSpecific()) {
                     if (rolesGrid.get(site).get(role).isSelected()) {
                         userRoleService.assignUserRole(user, role, site);
                     } else {
                         userRoleService.removeUserRoleAssignment(user, role, site);
                     }
                 }
             }
         }
 
         Set<Role> roleList = rolesGrid.values().iterator().next().keySet();
         for(Role role : roleList) {
             if (!role.isSiteSpecific()) {
                 int selected = 0;
                 int notSelected = 0;
                 for (Site innerSite : rolesGrid.keySet()) {
                     if (!rolesGrid.get(innerSite).get(role).isSelected()) notSelected++;
                     if (rolesGrid.get(innerSite).get(role).isSelected()) selected++;
                 }
                 if (selected == notSelected) {
                     if(UserRole.findByRole(user.getUserRoles(), role) == null) {
                         userRoleService.assignUserRole(user, role);
                     } else {
                         userRoleService.removeUserRoleAssignment(user, role);
                     }
                 } else if (selected == 1) {
                     userRoleService.assignUserRole(user, role);
                 } else if (notSelected == 1) {
                     userRoleService.removeUserRoleAssignment(user, role);
                 } else if (selected > notSelected) {
                     userRoleService.assignUserRole(user, role);
                 } else if (notSelected > selected) {
                     userRoleService.removeUserRoleAssignment(user, role);
                 }
             }
         }
     }
 
     public static class RoleCell {
         private boolean selected;
         private boolean siteSpecific;
 
         public RoleCell(boolean selected, boolean siteSpecific) {
             this.selected = selected;
             this.siteSpecific = siteSpecific;
         }
 
         public boolean isSelected() {
             return selected;
         }
 
         public boolean isSiteSpecific() {
             return siteSpecific;
         }
 
         public void setSelected(boolean selected) {
             this.selected = selected;
         }
 
         public void setSiteSpecific(boolean siteSpecific) {
             this.siteSpecific = siteSpecific;
         }
     }
 
     protected static RoleCell createRoleCell(boolean selected, boolean siteSpecific) {
         return new RoleCell(selected, siteSpecific);
     }
 
     // bean getter and setters
     public User getUser() {
         return user;
     }
 
     public String getPassword() {
         return password;
     }
 
     public void setPassword(String password) {
         this.password = password;
     }
 
     public String getRePassword() {
         return rePassword;
     }
 
     public void setRePassword(String rePassword) {
         this.rePassword = rePassword;
     }
 
     public UserService getUserService() {
         return userService;
     }
 
     public void setUserService(UserService userService) {
         this.userService = userService;
     }
 
     public void setSiteDao(SiteDao siteDao) {
         this.siteDao = siteDao;
     }
 
     public Map<Site, Map<Role, RoleCell>> getRolesGrid() {
         return rolesGrid;
     }
 
     public boolean isPasswordModified() {
         return passwordModified;
     }
 
     public void setPasswordModified(boolean passwordModified) {
         this.passwordModified = passwordModified;
     }
 }
