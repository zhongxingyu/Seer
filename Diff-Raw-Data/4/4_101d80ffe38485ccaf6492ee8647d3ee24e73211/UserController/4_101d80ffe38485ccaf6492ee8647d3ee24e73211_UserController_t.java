 package sample.controllers;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 import sample.service.UserStore;
 
 import java.util.Hashtable;
 
 @Controller
 public class UserController {
     UserStore userStore;
 
     @Autowired
     public UserController(UserStore UserStore) {
         this.userStore = UserStore;
     }
 
     @RequestMapping(value = "/register", method = RequestMethod.POST)
     @ResponseBody
    Hashtable<String, String> registerJson(@RequestParam String username, @RequestParam String email, @RequestParam String password){
        Hashtable hs = userStore.addUser(username, email, password);
         return hs;
     }
 
     @RequestMapping(value = "/update_password", method = RequestMethod.POST)
     @ResponseBody
     Hashtable<String, String> updateUserPassword(@RequestParam String userID,
                                          @RequestParam(required = false) String old_password,
                                          @RequestParam(required = false) String new_password){
 
         Hashtable hs = userStore.updateUserPassword(userID, old_password, new_password);
         return hs;
     }
 
     @RequestMapping(value = "/update_account", method = RequestMethod.POST)
     @ResponseBody
     Hashtable<String, String> updateUserAccount(@RequestParam String userID,
                                                  @RequestParam(required = false) String username,
                                                  @RequestParam(required = false) String email){
 
         Hashtable hs = userStore.updateUserAccount(userID, username, email);
         return hs;
     }
 
     @RequestMapping(value = "/update_profile", method = RequestMethod.POST)
     @ResponseBody
     Hashtable<String, String> updateUserProfile(@RequestParam String userID,
                                                  @RequestParam(required = false) String name,
                                                  @RequestParam(required = false) String description){
 
         Hashtable hs = userStore.updateUserProfile(userID, name, description);
         return hs;
     }
 }
