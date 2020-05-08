 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.digt.web;
 
 import com.digt.web.model.ExtUserForm;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.apache.rave.model.User;
 import org.apache.rave.portal.service.PageService;
 import org.apache.rave.portal.service.UserService;
 import org.apache.rave.portal.web.controller.ProfileController;
import org.apache.rave.portal.web.model.UserForm;
 import org.apache.rave.portal.web.util.ModelKeys;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 /**
  *
  * @author wasa
  */
 @Controller
 public class ExtProfileController extends ProfileController {
 

     private final UserService userService;
     
     private static final Logger LOG = Logger.getLogger(ExtProfileController.class.getName());
     
     @Autowired
     public ExtProfileController(UserService userService, PageService pageService) {
         super(userService, pageService);
         this.userService = userService;
     }

   
    @RequestMapping(value="/ext", method = RequestMethod.POST)
     public @ResponseBody HashMap updateProfile(ModelMap model,
                                 @RequestBody ExtUserForm updatedUser) {
         
         User user = userService.getAuthenticatedUser();
         LOG.log(Level.FINE, "Updating {0} profile information", user.getUsername());
 
         user.setGivenName(updatedUser.getGivenName());
         user.setFamilyName(updatedUser.getFamilyName());
         user.setDisplayName(updatedUser.getDisplayName());
         user.setAboutMe(updatedUser.getAboutMe());
         user.setStatus(updatedUser.getStatus());
         user.setEmail(updatedUser.getEmail());
         user.setProperties(new ArrayList(updatedUser.getProperties()));
 
         userService.updateUserProfile(user);
 
         model.addAttribute(ModelKeys.USER_PROFILE, user);
         
         HashMap result = new HashMap();
         result.put("success", true);
         return result;
     }
 }
