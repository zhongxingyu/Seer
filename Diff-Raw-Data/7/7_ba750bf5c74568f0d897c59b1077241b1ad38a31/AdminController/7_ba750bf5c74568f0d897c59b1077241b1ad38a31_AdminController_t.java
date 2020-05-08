 package de.enwida.web.controller;
 
 import java.io.File;
 import java.util.List;
 import java.util.Locale;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.MessageSource;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import de.enwida.web.model.Group;
 import de.enwida.web.model.Right;
 import de.enwida.web.model.Role;
 import de.enwida.web.model.User;
 import de.enwida.web.service.interfaces.IAspectService;
 import de.enwida.web.service.interfaces.IUserService;
 import de.enwida.web.validator.UserValidator;
 
 
 /**
  * Handles requests for the user service.
  * @author olcay tarazan
  *
  */
 @Controller
 @RequestMapping("/user/admin")
 public class AdminController {
 	
 	@Autowired
 	private IUserService userService;
 	
     @Autowired
     private IAspectService aspectService;
 	
 	@Autowired
 	private UserValidator userValidator;
 	
 	@Autowired
 	private MessageSource messageSource;
 	
 
     private static org.apache.log4j.Logger logger = Logger.getLogger(AdminController.class);
 	
 	
 	@RequestMapping(value="/admin_editaspect", method = RequestMethod.GET)
 	public String editAspect(HttpServletRequest request,Model model,Long roleID,Integer start,Integer max,Locale locale) {
 	    List<Right> aspectRights;
         List<Role> roles = null;
         //Dont load all data
        if(start==null){
            start=10;
        }
        if( max==null){
            max=10;
         }
         try {
             if (request.getParameterValues("all")==null){
             aspectRights = aspectService.getAllAspects(roleID,start,max);
             }else{
                 aspectRights = aspectService.getAllAspects(roleID);
             }
             roles = userService.fetchAllRoles();
             //Get all roles
             model.addAttribute("roles", roles);
             //Get all aspects status of requested role
             model.addAttribute("aspectRights", aspectRights);
         } catch (Exception e) {
             model.addAttribute("Info", messageSource.getMessage("de.enwida.userManagement.error.notAllowed", null,locale));
         }
 	    //Present the page
 		model.addAttribute("content", "admin/admin_editAspect");
 		return "user/master";
 	}
 	/**
 	 * Gets all the groups and user and present
 	 * @param model
 	 * @return
 	 */
 	@RequestMapping(value="/admin_userlist", method = RequestMethod.GET)
 	public String userList(HttpServletRequest request, Model model,Locale locale) {
 	    //Gets all the users
 	    List<User> users;
         try { 
             //Get all the groups
             List<Group> groups=userService.fetchAllGroups();
             users = userService.fetchAllUsers();
             model.addAttribute("users", users);  
             model.addAttribute("groups", groups);
         } catch (Exception e) {
             logger.info(e.getMessage());
             model.addAttribute("error", messageSource.getMessage("de.enwida.userManagement.error.notAllowed", null,locale));
         }
 
         model.addAttribute("content", "admin/admin_userList");
         return "user/master";
 	}
     /**
      * Handles editGroup page reuqests
      * @param model
      * @param action delete or add is allowed when it presents
      * @param groupID
      * @param newGroup
      * @return
      */
     @RequestMapping(value="/admin_editgroup", method = RequestMethod.GET)
     public String editGroup(HttpServletRequest request,Model model,String action,Integer groupID,String newGroup,Locale locale) {    
         try {
             if (action!=null){
                 //Check which action to be executed
                 if( action.equalsIgnoreCase("delete")){
                             userService.deleteGroup(groupID);
                 }else if (action.equalsIgnoreCase("add")){
                             Group group=new Group();
                             group.setGroupName(newGroup);            
                             userService.saveGroup(group);
                 }
                 //Print info message
                 //TODO:These messages will be localized
                 model.addAttribute("info", "OK");
             }
             //Get groups with user information attached
             List<Group> groupsWithUsers= userService.fetchAllGroups();
             model.addAttribute("groupsWithUsers", groupsWithUsers);
             //Get all the users
             List<User> users= userService.fetchAllUsers();
             model.addAttribute("users", users);
             
         } catch (Exception e) {
             logger.info(e.getMessage());
             model.addAttribute("error", messageSource.getMessage("de.enwida.userManagement.error.notAllowed", null,locale));
         }
         
         model.addAttribute("content", "admin/admin_editGroup");
         return "user/master";
     }
        
     /**
      * Handles role list page requests
      * @param model
      * @return
      */
     @RequestMapping(value="/admin_rolelist", method = RequestMethod.GET)
     public String roleList(HttpServletRequest request,Model model,Locale locale) {
         try {
             List<Role> roles= userService.fetchAllRoles();
             model.addAttribute("roles", roles);
             
             List<Role> rolesWithGroups= userService.fetchAllRoles();
             model.addAttribute("rolesWithGroups", rolesWithGroups);
             
             List<Group> groups= userService.fetchAllGroups();
             model.addAttribute("groups", groups);
         } catch (Exception e) {
             model.addAttribute("error", messageSource.getMessage("de.enwida.userManagement.error.notAllowed", null,locale));
             logger.error(e.getMessage());
         }
        
         model.addAttribute("content", "admin/admin_roleList");
         return "user/master";
     }
     
     /**
      * Default user list page is displayed
      * @param model
      * @return
      */
     @RequestMapping(value="/", method = RequestMethod.GET)
     public String admin(HttpServletRequest request,Model model,Locale locale) {
         return userList(request,model,locale);
     }
     
     /**
      * User actions log will be displayed by reading the user log file
      * @param model
      * @param user
      * @return
      */
     @RequestMapping(value="/admin_userlog", method = RequestMethod.GET)
     public String  userLog(HttpServletRequest request,Model model,String user,Locale locale) {
         File file;
         try {
             //read the user log file and display it
             file=new File(System.getenv("ENWIDA_HOME")+"/log/"+user+".log");
             model.addAttribute("userLog",FileUtils.readFileToString(file));
         } catch (Exception e) {
             model.addAttribute("error", messageSource.getMessage("de.enwida.userManagement.error.notAllowed", null,locale));
             logger.error(e.getMessage());
         } 
         model.addAttribute("content", "admin/admin_userLog");
         return "user/master";
     }
     
     
     @RequestMapping(value = "/enableDisableUser", method = RequestMethod.GET)
     @ResponseBody
     public boolean enableDisableUser(int userID,boolean enabled) {
         try {
             userService.enableDisableUser(userID,enabled); 
             return true;       
         } catch (Exception e) {   
             logger.info(e.getMessage());
             return false;      
         }
     }   
     
     @RequestMapping(value = "/enableDisableAspect", method = RequestMethod.GET)
     @ResponseBody
     public boolean enableDisableAspect(int rightID,boolean enabled) {
         try {
             userService.enableDisableAspect(rightID,enabled);
             return true;       
         } catch (Exception e) {   
             logger.info(e.getMessage());
             return false;      
         }
     }   
     
     
     
     @RequestMapping(value = "/enableAutoPass", method = RequestMethod.GET)
     @ResponseBody
     public boolean enableAutoPass(Long groupID,boolean enabled) {
         try {
             userService.enableDisableAutoPass(groupID,enabled);
             return true;       
         } catch (Exception e) {   
             logger.info(e.getMessage());
             return false;      
         }
     }   
     
     @RequestMapping(value="/admin_user", method = RequestMethod.GET)
     public String user(HttpServletRequest request,Model model,long userID,Locale locale) {
         
         User user = null;
         try {
             user = userService.fetchUser(userID);
         } catch (Exception e) {
             logger.info(e.getMessage());
         }
         if (user==null){
             model.addAttribute("Info",  messageSource.getMessage("de.enwida.userManagement.userNotFound", null,locale));
             //This shouldnt happen
             logger.info("User is not found:userID:"+userID);
             //Redirect user to main page;
             return admin(request,model,locale);            
         }
         model.addAttribute("user", user);
         model.addAttribute("content", "admin/admin_user");
         return "user/master";
     }
     
     @RequestMapping(value="/admin_user",method=RequestMethod.POST, params = "save")
     public String processForm(@ModelAttribute(value="USER")User user,long userID,HttpSession session, Model model,HttpServletRequest request,Locale locale)
     {
         User newUser = null;
         try {
             newUser = userService.fetchUser(userID);
             newUser.setFirstName(user.getFirstName());
             newUser.setLastName(user.getLastName());
             newUser.setTelephone(user.getTelephone());
             newUser.setCompanyName(user.getCompanyName());
             userService.updateUser(newUser);
         } catch (Exception e) {
             logger.info(e.getMessage());
         }
         if (user==null){
             model.addAttribute("Info", messageSource.getMessage("de.enwida.userManagement.userNotFound", null,locale));
             //This shouldnt happen
             logger.info("User is not found:userID:"+userID);
             //Redirect user to main page;
             return admin(request,model, locale);            
         }
 
         model.addAttribute("user", newUser);
         model.addAttribute("content", "admin/admin_user");
         return "user/master";
     }
 
     @RequestMapping(value="/admin_user",method=RequestMethod.POST, params = "resetPassword")
     public String reset(HttpServletRequest request,Model model,long userID,Locale locale)
     {
         System.out.println("ResetPassword");
         try {
             userService.resetPassword(userID,request.getLocale());
             model.addAttribute("info", "OK");
         } catch (Exception e) {
             model.addAttribute("error", "Error:"+e.getLocalizedMessage());
             logger.error(e.getMessage());
         }
         return user(request,model,userID,locale);
     }
     
     //deletes the user
     @RequestMapping(value="/admin_user",method=RequestMethod.POST, params = "delete")
     public String deleteUser(HttpServletRequest request,Model model,long userID,Locale locale)
     {
         System.out.println("DeleteUser");
         try {
             User user=userService.fetchUser(userID);
             userService.deleteUser(user);
             //Confirm
             user=userService.fetchUser(userID);
             if(user!=null){
                 throw new Exception("Not Allowed");
             }
             
         } catch (Exception e) {
             model.addAttribute("Info", messageSource.getMessage("de.enwida.userManagement.error.notAllowed", null,locale));
             logger.info(e.getMessage());
             return user(request,model,userID,locale);
         }
         return userList(request,model,locale);
     }
     
     @RequestMapping(value="/admin_editgroup",method=RequestMethod.POST, params = "assign")
     public String assignUserToGroup(HttpServletRequest request,Model model,long selectedUser,long selectedGroup,Locale locale)
     {
         try {
         	User user=userService.fetchUser(selectedUser);
         	Group group=userService.fetchGroupById(selectedGroup);
             userService.assignGroupToUser(user,group);
             model.addAttribute("info", "OK");       
         } catch (Exception e) {   
             logger.info(e.getMessage());
             model.addAttribute("Error", messageSource.getMessage("de.enwida.userManagement.error.notAllowed", null,locale));       
         }
         return editGroup(request,model,null,0,null,locale);
     }
     
     @RequestMapping(value="/admin_editgroup",method=RequestMethod.POST, params = "deassign")
     public String deassignUserToGroup(HttpServletRequest request,Model model,long selectedUser,long selectedGroup,Locale locale)
     {
         try {
             User user=userService.fetchUser(selectedUser);
             Group group=userService.fetchGroupById(selectedGroup);
             userService.revokeUserFromGroup(user,group); 
             model.addAttribute("info", "OK");       
         } catch (Exception e) {   
             logger.info(e.getMessage());
             model.addAttribute("Error", messageSource.getMessage("de.enwida.userManagement.error.notAllowed", null,locale));       
         }
         return editGroup(request,model,null,0,null,locale);
     }
     
     @RequestMapping(value="/admin_editgroup",method=RequestMethod.POST, params = "addGroup")
     public String addGroup(HttpServletRequest request,Model model,String newGroup,boolean autoPass,Locale locale)
     {
         Group group=new Group();
         group.setGroupName(newGroup);
         group.setAutoPass(autoPass);
         try {
             userService.saveGroup(group);
             
         } catch (Exception e) {
             model.addAttribute("error", messageSource.getMessage("de.enwida.userManagement.error.notAllowed", null,locale));
             logger.error(e.getMessage());
         }
         return editGroup(request,model,null,0,null,locale);
     }
     
     @RequestMapping(value="/admin_rolelist",method=RequestMethod.POST, params = "assign")
     public String assignRoleToGroup(HttpServletRequest request,Model model,long selectedRole,long selectedGroup,Locale locale)
     {
         try {
             Role role=userService.fetchRoleById(selectedRole);
             Group group=userService.fetchGroupById(selectedGroup);
             userService.assignRoleToGroup(role,group);     
             model.addAttribute("info", "OK");       
         } catch (Exception e) {   
             logger.info(e.getMessage());
             model.addAttribute("Error", messageSource.getMessage("de.enwida.userManagement.error.notAllowed", null,locale));       
         }
         return roleList(request,model,locale);
     }
     
     @RequestMapping(value="/admin_rolelist",method=RequestMethod.POST, params = "deassign")
     public String deassignRoleToGroup(HttpServletRequest request,Model model,long selectedRole,long selectedGroup,Locale locale)
     {
         try {
             Role role=userService.fetchRoleById(selectedRole);
             Group group=userService.fetchGroupById(selectedGroup);
             userService.revokeRoleFromGroup(role,group);  
             model.addAttribute("info", "OK");       
         } catch (Exception e) {   
             logger.info(e.getMessage());
             model.addAttribute("Error", messageSource.getMessage("de.enwida.userManagement.error.notAllowed", null,locale));       
         }
         return roleList(request,model,locale);
     }
 }
