 package org.jks.web;
 
 import org.jks.domain.Profile;
 import org.jks.domain.Section;
 import org.jks.domain.User;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.MessageSource;
 import org.springframework.context.support.ResourceBundleMessageSource;
 import org.springframework.http.ResponseEntity;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.*;
 import org.springframework.web.client.RestClientException;
 import org.springframework.web.client.RestTemplate;
 
 import javax.validation.Valid;
 
 import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Locale;
 import java.util.Map;
 
 /**
  * @author juancarrillo
  */
 @Controller
 @RequestMapping("users")
 public class UserController {
 
     private static final Logger logger = LoggerFactory.getLogger(UserController.class);
 
     @Autowired
     private RestTemplate restTemplate;
 
     @Autowired
     MessageSource messageSource;
 
     @RequestMapping(value="/", method = RequestMethod.GET)
     public String showUsers(Model model) {
     	addUsersToModel(model);
         return "users";
     }
 
     @RequestMapping(value = "/signin", method = RequestMethod.GET)
     public String login(Model model) {
 
         return "login";
     }
 
     @RequestMapping(value="/signfailed", method = RequestMethod.GET)
     public String loginerror(Model model) {
 
         model.addAttribute("error", "true");
         return "login";
     }
 
     @RequestMapping(value="/signout", method = RequestMethod.GET)
     public String logout(Model model) {
 
         return "login";
     }
 
     @RequestMapping(value="/register", method = RequestMethod.GET)
     public String register(Model model) {
 
         return "register";
     }
 
     @RequestMapping(value = "/register", method= RequestMethod.POST)
     @ResponseBody
     public String registerUser(@Valid @RequestBody User user, Locale locale) {
 
 
         user.setProfile(Profile.NORMAL.toString());
         user.setProfileid(Profile.NORMAL.getValue());
         boolean userCreated = modifyUser(user);
 
         String returnText = "";
 
         if (userCreated) {
             returnText = messageSource.getMessage("success-created-user", null, locale);
         } else {
             returnText = messageSource.getMessage("system-error", null, locale);
         }
 
         return returnText;
     }
 
     @RequestMapping(value = "/create", method= RequestMethod.POST)
     @ResponseBody
     public String createUser(@Valid @RequestBody User user, Locale locale) {
 
         boolean userCreated = modifyUser(user );
 
         String returnText = "";
 
         if (userCreated) {
             returnText = messageSource.getMessage("success-created-user", null, locale);
         } else {
             returnText = messageSource.getMessage("system-error", null, locale);
         }
 
         return returnText;
     }
 
     @RequestMapping(value = "/edit", method = RequestMethod.POST)
     @ResponseBody
     public String editUser(@Valid @RequestBody User user, Locale locale) {
 
         boolean userEdited = true;
 
         try {
             user.setPassword(User.sha1(user.getPassword()));
 
             restTemplate.put("http://localhost:8080/service/user/update", user);
 
         } catch (RestClientException e) {
             userEdited = false;
             logger.error(e.getMessage(), e);
             e.printStackTrace();
         } catch (NoSuchAlgorithmException e) {
             userEdited = false;
             logger.error(e.getMessage(), e);
         } catch (Exception e) {
             userEdited = false;
             logger.error(e.getMessage(), e);
         }
 
         String returnText = "";
 
         if (userEdited) {
             returnText = messageSource.getMessage("success-edited-user", null, locale);
         } else {
             returnText = messageSource.getMessage("system-error", null, locale);
         }
 
         return returnText;
     }
 
     private boolean modifyUser(User user) {
         boolean userModified = true;
 
         try {
             user.setPassword(User.sha1(user.getPassword()));
 
             restTemplate.postForLocation("http://localhost:8080/service/user/create", user);
 
         } catch (RestClientException e) {
             userModified = false;
             logger.error(e.getMessage(), e);
             e.printStackTrace();
         } catch (NoSuchAlgorithmException e) {
             userModified = false;
             logger.error(e.getMessage(), e);
         } catch (Exception e) {
             userModified = false;
             logger.error(e.getMessage(), e);
         }
 
         return userModified;
     }
 
     @RequestMapping(value="new", method = RequestMethod.GET)
     public String newUser(Model model) {
 
         assignProfilesToModel(model);
         model.addAttribute("action", "new");
         return "edit_user";
     }
 
     @RequestMapping(value="/edit/{userId}", method = RequestMethod.GET)
    public String editUser(@PathVariable long userId,Model model, Locale locale) {
 
         try {
             User user = restTemplate.getForObject("http://localhost:8080/service/user/id/"+userId, User.class);
             model.addAttribute("user", user);
 
         } catch (RestClientException e) {
            logger.error("The user with userId "+ userId + " could not be found");
            model.addAttribute("error", messageSource.getMessage("user-not-found-id", new Long[]{userId},locale));
         }
 
         model.addAttribute("action", "edit");
         assignProfilesToModel(model);
 
         return "edit_user";
     }
 
     private void assignProfilesToModel(Model model) {
         Map<String, Byte> profiles = new HashMap<String, Byte>();
         profiles.put(Profile.NORMAL.toString(), Profile.NORMAL.getValue());
         profiles.put(Profile.CONTENT_CREATOR.toString(), Profile.CONTENT_CREATOR.getValue());
         profiles.put(Profile.ADMINISTRATOR.toString(), Profile.ADMINISTRATOR.getValue());
         model.addAttribute("profiles", profiles);
     }
     
     
     private void addUsersToModel(Model model){
     	 try {
              User[] users = restTemplate.getForObject("http://localhost:8080/service/user/all", User[].class);
              model.addAttribute("usersList",  users);
          } 
     	 catch (RestClientException e) {
              logger.error("Users could not be retrieved", e);
          }
     }
 }
