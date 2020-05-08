 package com.exadel.borsch.web.controllers;
 
 import com.exadel.borsch.dao.AccessRight;
 import com.exadel.borsch.dao.User;
 import com.exadel.borsch.managers.ManagerFactory;
 import com.exadel.borsch.managers.UserManager;
 import org.hibernate.validator.constraints.NotEmpty;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.*;
 
 import java.util.*;
 
 import org.springframework.ui.Model;
 
 import javax.validation.Valid;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Size;
 
 
 /**
  *
  * @author Vlad
  */
 @Controller
 public class UsersController {
 
     private static final int HTTP_OK = 200;
     private static UserCommand userCommand = new UserCommand();
     @Autowired
     private ManagerFactory managerFactory;
 
     @RequestMapping("/users")
     public String processPageRequest(Model model) {
         UserManager userManager = managerFactory.getUserManager();
         model.addAttribute("users", userManager.getAllUsers());
         return ViewURLs.USERS_PAGE;
     }
     @RequestMapping("/edit/user/{userId}")
     public String processEditPageRequest(@PathVariable String userId, ModelMap model) {
         UserManager userManager = managerFactory.getUserManager();
         User user = userManager.getUserById(UUID.fromString(userId));
         userCommand.mapUserToUserCommand(user);
         model.addAttribute("userCommand", userCommand);
         model.addAttribute("allRights", AccessRight.getAllRightsToString().toArray(new String[]{""}));
        return "userEdit";
     }
 
     @ResponseBody
     @RequestMapping("/edit/user/{userId}/edit")
     public EditStatus  processUpdateUserRequest(@PathVariable String userId,
                      @Valid UserCommand userCommand, BindingResult result) {
         EditStatus response = new EditStatus();
         UserManager userManager = managerFactory.getUserManager();
         User user = userManager.getUserById(UUID.fromString(userId));
        if (result.hasFieldErrors("name")) {
            response.setAlertName(true);
        } else {
             user.setName(userCommand.getName());
         }
        if (result.hasFieldErrors("locale")) {
            response.setAlertLocale(true);
        } else {
             user.setLocale(new Locale(userCommand.getLocale()));
         }
        if (result.hasFieldErrors("rights") && !userCommand.isRightsNull()) {
            response.setAlertRights(true);
        } else if (!userCommand.isRightsNull()){
             user.setAccessRights(Arrays.asList(userCommand.getRights()));
         }
         user.setNeedEmailNotification(userCommand.getNeedEmailNotification());
         if (!result.hasErrors()) {
             userManager.updateUser(user);
         }
 
         return response;
     }
 
     @RequestMapping(value = "/edit/user/{userId}/remove", method = RequestMethod.POST)
     public String processRemoveUserRequest(@PathVariable String userId) {
         UserManager userManager = managerFactory.getUserManager();
         userManager.deleteUserById(UUID.fromString(userId));
         return ViewURLs.USERS_PAGE;
     }
 
     public static class EditStatus {
         private boolean alertName = false;
         private boolean alertLocale = false;
         private boolean alertRights = false;
 
         public boolean getAlertName() {
             return alertName;
         }
 
         public void setAlertName(boolean alertName) {
             this.alertName = alertName;
         }
 
         public boolean getAlertLocale() {
             return alertLocale;
         }
 
         public void setAlertLocale(boolean alertLocale) {
             this.alertLocale = alertLocale;
         }
 
         public boolean getAlertRights() {
             return alertRights;
         }
 
         public void setAlertRights(boolean alertRights) {
             this.alertRights = alertRights;
         }
     }
     public static class UserCommand {
         @Size(min = 1, max = 20)
         private String name;
         @NotEmpty
         private String locale;
         @NotNull
         private String id;
         @NotEmpty
         private String[] rights;
         private boolean needEmailNotification;
 
         public boolean getNeedEmailNotification() {
             return needEmailNotification;
         }
 
         public boolean isRightsNull() {
             return rights == null;
         }
 
         public void setNeedEmailNotification(boolean needEmailNotification) {
             this.needEmailNotification = needEmailNotification;
         }
 
         public String getId() {
             return id;
         }
 
         public void setId(String id) {
             this.id = id;
         }
 
         public String getName() {
             return name;
         }
 
         public void setName(String name) {
             this.name = name;
         }
 
         public String getLocale() {
             return locale;
         }
 
         public void setLocale(String locale) {
             this.locale = locale;
         }
 
         public void setRights(String[] newRights) {
             rights = Arrays.copyOf(newRights, newRights.length);
         }
 
         public String[] getRights() {
             return Arrays.copyOf(rights, rights.length);
         }
 
         public void mapUserToUserCommand(User user) {
             this.name = user.getName();
             this.id = user.getId().toString();
             this.locale = user.getLocale().getLanguage();
             this.needEmailNotification = user.getNeedEmailNotification();
             this.rights = user.getStringAccessRights().toArray(new String[]{""});
         }
     }
 }
